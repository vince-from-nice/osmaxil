package org.openstreetmap.osmaxil.dao;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;
import org.openstreetmap.osmaxil.model.AbstractElement;
import org.openstreetmap.osmaxil.model.misc.ElementType;
import org.openstreetmap.osmaxil.model.xml.osm.OsmXmlRoot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class OsmStandardApi {
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Instance attributes
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private String changesetSourceLabel;
    
    private String changesetComment;
    
    private long currentChangesetID;
    
    private int counterForChangeset;
    
    private int counterForReadSuccess;
    
    private int counterForReadFailure;
    
    private int counterForWriteSuccess;
    
    private int counterForWriteFailure;
    
    private int counterForChangesetOpen;
    
    private int counterForChangesetClose;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${osmApi.url}")
    private String url = "http://api06.dev.openstreetmap.org/api/0.6/";
    
    @Value("${osmApi.maxUpdatesByChangeset}")
    private int MAX_UPDATES_BY_CHANGESET;
    
    static private final Logger LOGGER = Logger.getLogger(Application.class);
    
    static private final Logger LOGGER_FOR_STATS = Logger.getLogger("LoggerForStats");
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void initForWriting(String changesetSourceLabel, String changesetComment) throws RestClientException {
        this.counterForChangeset = 0;
        this.currentChangesetID = 0;
        this.changesetComment = changesetComment; 
        this.changesetSourceLabel = changesetSourceLabel;
    }
    
    @PreDestroy
    public void close() {
        LOGGER_FOR_STATS.info("=== Closing OSM API service ===");
        if (this.currentChangesetID > 0) {
            this.closeChangeset(this.currentChangesetID);
        }
        LOGGER_FOR_STATS.info("Total of read operations: success=" + this.counterForReadSuccess + " failure=" + this.counterForReadFailure);
        LOGGER_FOR_STATS.info("Total of write operations: success=" + this.counterForWriteSuccess + " failure=" + this.counterForWriteFailure);
        LOGGER_FOR_STATS.info("Total of changeset operations: open=" + this.counterForChangesetOpen + " close=" + this.counterForChangesetClose);
    }
    
    public OsmXmlRoot readElement(long id, ElementType type) {
        OsmXmlRoot result = null;
        LOGGER.info("Read element from OSM API with id=" + id + " : ");
        try {
            // Fetch a basic string 
            //String str = this.restTemplate.getForObject(this.url + "way/" + id, String.class);
            
            // WTF it doesn't work anymore : request is ok (400) but result is null !!
            //result = this.restTemplate.getForObject(this.url + "way/" + id, OsmApiRoot.class);
            
            // But it still works with the generic exchange() method
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept-Encoding", "");
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            ResponseEntity<OsmXmlRoot> responseEntity = this.restTemplate.exchange(this.url + type.getName() + "/" + id, HttpMethod.GET, requestEntity, OsmXmlRoot.class);
            result = responseEntity.getBody();
            
            this.counterForReadSuccess++;
        } catch (Exception e) {
            LOGGER.error("Unable to read element with id=" + id + " (" + e.getMessage() + ")");
            this.counterForReadFailure++;
        }
        return result;
    }
    
    public boolean writeElement(AbstractElement element, ElementType type) {
        LOGGER.info("Write element to OSM API with id=" + element.getOsmId() + " : ");
        this.checkCurrentChangeset();
        element.updateChangeset(this.currentChangesetID);
//      StringWriter out = new StringWriter();
//      this.marshaller.marshal(b.getData(), new StreamResult(out));
//      String xml = out.toString();
        try {
            this.restTemplate.put(this.url + type.getName() + "/" + element.getOsmId(), decorateData(element.getApiData()));
            this.counterForChangeset++;
            this.counterForWriteSuccess++;
        } catch (Exception e) {
            LOGGER.error("Unable to write element with id=" + element.getOsmId() + " (" + e.getMessage() + ")");
            this.counterForChangeset++; // Need to increment changeset counter even in case of failure 
            this.counterForWriteFailure++;
            return false;
        } 
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private OsmXmlRoot decorateData(OsmXmlRoot root) {
        root.version = 0.6f;
        root.generator = Application.NAME;
        return root;
    }
    
    private void checkCurrentChangeset() {
        // Check there's a current changeset
        if (this.currentChangesetID == 0) {
            this.currentChangesetID = this.createChangeset();
            LOGGER.info("Current changeset ID is now " + this.currentChangesetID);
        }
        // Check that if current changeset has reached the limit 
        if (this.counterForChangeset >= MAX_UPDATES_BY_CHANGESET) {
            LOGGER.info("Changeset " + this.currentChangesetID + " has reached " + this.counterForChangeset);
            this.closeChangeset(this.currentChangesetID);
            this.currentChangesetID = this.createChangeset();
            this.counterForChangeset = 0;
            LOGGER.info("Current changeset ID is now " + this.currentChangesetID);
        }
    }
    
    private long createChangeset() throws RestClientException {
        long result = 0;
        try {
            StringBuffer sb = new StringBuffer("<osm><changeset>");
            sb.append("<tag k=\"created_by\" v=\"" + Application.NAME + "\"/>");
            sb.append("<tag k=\"bot\" v=\"yes\"/>");
            sb.append("<tag k=\"comment\" v=\"" + this.changesetComment + "\"/>");
            sb.append("<tag k=\"source\" v=\"" + this.changesetSourceLabel + "\"/>");
            sb.append("</changeset></osm>");
            // RestTemplate.put() returns void, using RestTemplate.exchange() instead
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept-Encoding", "");
            headers.add("Content-Type", "application/xml");
            headers.add("Accept", "*/*");
            HttpEntity<String> requestEntity = new HttpEntity<String>(sb.toString(), headers);
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(this.url + "changeset/create", HttpMethod.PUT, requestEntity, String.class);
            //this.restTemplate.put(this.url + "changeset/create", sb.toString());
            result = Long.parseLong(responseEntity.getBody().toString());
            this.counterForChangesetOpen++;
        } catch (RestClientException e) {
            LOGGER.error("Unable to create a new changeset (" + e.getMessage() + ")");
            throw e;
        } 
        LOGGER.info("Changeset with id=" + result + " has been created");
        return result;
    }
    
    private void closeChangeset(long id) {
        try {
            this.restTemplate.put(this.url +  "changeset/" + this.currentChangesetID + "/close", null);
            this.counterForChangesetClose++;
        } catch (Exception e) {
            LOGGER.error("Unable to close the current changeset (" + e.getMessage() + ")");
        } 
        LOGGER.info("Changeset with id=" + id + " has been closed");
    }

}

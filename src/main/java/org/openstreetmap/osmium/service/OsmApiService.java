package org.openstreetmap.osmium.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.openstreetmap.osmium.Application;
import org.openstreetmap.osmium.data.AbstractElement;
import org.openstreetmap.osmium.data.api.OsmApiRoot;
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
public class OsmApiService {
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Instance attributes
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private long currentChangesetID;
    
    private int currentChangesetCounter;
    
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
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostConstruct
    public void init() throws RestClientException {
        this.currentChangesetCounter = 0;
        this.currentChangesetID = this.createChangeset();
        LOGGER.info("Current changeset ID is " + this.currentChangesetID);
    }
    
    @PreDestroy
    public void close() {
        LOGGER.info("Closing OSM API service");
        if (this.currentChangesetID > 0) {
            this.closeChangeset(this.currentChangesetID);
        }
        LOGGER.info("Total of read operations: success=" + this.counterForReadSuccess + " failure=" + this.counterForReadFailure);
        LOGGER.info("Total of write operations: success=" + this.counterForWriteSuccess + " failure=" + this.counterForWriteFailure);
        LOGGER.info("Total of changeset operations: open=" + this.counterForChangesetOpen + " close=" + this.counterForChangesetClose);
    }
    
    public OsmApiRoot readElement(long id) {
        OsmApiRoot result = null;
        LOGGER.info("Read element from OSM API with id=" + id + " : ");
        try {
            //String str = this.restTemplate.getForObject(this.url + "way/" + id, String.class);
            result = this.restTemplate.getForObject(this.url + "way/" + id, OsmApiRoot.class);
            this.counterForReadSuccess++;
        } catch (Exception e) {
            LOGGER.error("Unable to read element with id=" + id + " (" + e.getMessage() + ")");
            this.counterForReadFailure++;
        }
        return result;
    }
    
    public boolean writeElement(AbstractElement element) {
        LOGGER.info("Write element to OSM API with id=" + element.getOsmId() + " : ");
        if (this.currentChangesetCounter >= MAX_UPDATES_BY_CHANGESET) {
            this.closeChangeset(this.currentChangesetID);
            this.currentChangesetID = this.createChangeset();
        }
        element.updateChangeset(this.currentChangesetID);
//      StringWriter out = new StringWriter();
//      this.marshaller.marshal(b.getData(), new StreamResult(out));
//      String xml = out.toString();
        try {
            this.restTemplate.put(this.url + "way/" + element.getOsmId(), element.getApiData());
            this.currentChangesetCounter++;
            this.counterForWriteSuccess++;
        } catch (Exception e) {
            LOGGER.error("Unable to write element with id=" + element.getOsmId() + " (" + e.getMessage() + ")");
            this.counterForWriteFailure++;
        } 
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private long createChangeset() throws RestClientException {
        long result = 0;
        LOGGER.info("Create a new changeset");
        try {
            StringBuffer sb = new StringBuffer("<osm><changeset>");
            sb.append("<tag k=\"created_by\" v=\"Osmium\"/>");
            sb.append("<tag k=\"comment\" v=\"Updating height and levels of buildings\"/>");
            sb.append("</changeset></osm>");
            // RestTemplate.put() returns void, using RestTemplate.exchange() instead
            HttpHeaders headers = new HttpHeaders();
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
        LOGGER.info("Close changeset with id=" + id);
        try {
            this.restTemplate.put(this.url +  "changeset/" + this.currentChangesetID + "/close", null);
            this.counterForChangesetClose++;
        } catch (Exception e) {
            LOGGER.error("Unable to close the current changeset (" + e.getMessage() + ")");
        } 
        LOGGER.info("Changeset with id=" + id + " has been closed");
    }

}

package org.openstreetmap.osmaxil.plugin.common.matcher;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmaxil.model.TreeImport;
import org.openstreetmap.osmaxil.model.misc.MatchingElementId;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;

@Component
public class TreeMatcher extends AbstractMatcher<TreeImport> {

    private double matchingAreaRadius;

    private boolean matchClosestOnly;

    private GeometryFactory gf = new GeometryFactory();

    private WKTWriter ww = new WKTWriter();

    @Override
    public List<MatchingElementId> findMatchingElements(TreeImport tree, int srid) {
        List<MatchingElementId> results = new ArrayList<>();
        String query = "SELECT osm_id, ST_Distance(way, ";
        Point point = gf.createPoint(new Coordinate(tree.getLongitude(), tree.getLatitude()));
        String wkt = ww.write(point);
        String treeGeometry = "ST_GeomFromText('" + wkt + "', " + this.osmPostgis.getSrid() + ")";
        // Transform coordinates of the tree if it's needed
        if (srid != this.osmPostgis.getSrid()) {
            // ST_Transform(ST_GeomFromText('POINT(7.2670199 43.6950068)', 4326), 900913)
            treeGeometry = "ST_Transform(ST_GeomFromText('" + wkt + "', " + srid + "), " + this.osmPostgis.getSrid()
                    + ")";
        }
        query += treeGeometry + ") AS distance ";
        query += "FROM planet_osm_point n WHERE n.natural = 'tree' AND way && ";
        String boxGeometry = "St_Buffer(ST_Transform(ST_GeomFromText('" + wkt + "', " + srid + "), "
                + this.osmPostgis.getSrid() + "), " + this.matchingAreaRadius + ")";
        query += boxGeometry;
        query += "ORDER BY distance;";
        Long[] oldTreeIds = this.osmPostgis.findElementIdsByQuery(query);
        if (oldTreeIds.length > 0) {
            if (matchClosestOnly) {
                MatchingElementId matchingElementId = new MatchingElementId();
                matchingElementId.setOsmId(oldTreeIds[0]);
                results.add(matchingElementId);
            } else {
                for (Long id : oldTreeIds) {
                    MatchingElementId matchingElementId = new MatchingElementId();
                    matchingElementId.setOsmId(id);
                    results.add(matchingElementId);
                }
            }
        }
        return results;
    }

    @Override
    public float computeMatchingImportScore(TreeImport tree) {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMatchingAreaRadius() {
        return matchingAreaRadius;
    }

    public void setMatchingAreaRadius(double matchingAreaWidth) {
        this.matchingAreaRadius = matchingAreaWidth;
    }

    public boolean isMatchClosestOnly() {
        return matchClosestOnly;
    }

    public void setMatchClosestOnly(boolean matchClosestOnly) {
        this.matchClosestOnly = matchClosestOnly;
    }

}

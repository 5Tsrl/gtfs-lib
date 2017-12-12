package com.conveyal.gtfs.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mapdb.Fun;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Represents a collection of GTFS shape points. Never saved in MapDB but constructed on the fly.
 */
public class Shape {
    /** The shape itself */
    public LineString geometry;

    /** shape_dist_traveled for each point in the geometry. TODO how to handle shape dist traveled not specified, or not specified on all stops? */
    public double[] shape_dist_traveled;

    public Shape (GTFSFeed feed, String shape_id) {
        Map<Fun.Tuple2<String, Integer>, ShapePoint> points =
                feed.shape_points.subMap(new Fun.Tuple2(shape_id, null), new Fun.Tuple2(shape_id, Fun.HI));

        Coordinate[] coords = points.values().stream()
                .map(point -> new Coordinate(point.shape_pt_lon, point.shape_pt_lat))
                .toArray(i -> new Coordinate[i]);
        geometry = Util.geometryFactory.createLineString(coords);
        shape_dist_traveled = points.values().stream().mapToDouble(point -> point.shape_dist_traveled).toArray();
    }
    
    public Shape (Iterator<ShapePoint> shapePointIterator) {
    	ShapePoint shapePoint;
    	List<Coordinate> coords = new ArrayList<Coordinate>();
    	while(shapePointIterator.hasNext()) {
    		shapePoint = shapePointIterator.next();
    		Coordinate coord = new Coordinate(shapePoint.shape_pt_lon, shapePoint.shape_pt_lat);
    		coords.add(coord);
    	}

        geometry = Util.geometryFactory.createLineString(coords.toArray(new Coordinate[coords.size()]));

    }
}

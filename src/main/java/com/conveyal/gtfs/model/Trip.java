package com.conveyal.gtfs.model;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.loader.DateField;
import com.conveyal.gtfs.loader.Table;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Iterator;

public class Trip extends Entity {

    private static final long serialVersionUID = -4869384750974542712L;
    public String route_id;
    public String service_id;
    public String trip_id;
    public String trip_headsign;
    public String trip_short_name;
    public int direction_id;
    public String block_id;
    public String shape_id;
    public int bikes_allowed;
    public int wheelchair_accessible;
    public String feed_id;
    public int official_length;
    public int contributed;
    public int seat;
    public int stand;
    public LocalDate start_date;
    public LocalDate end_date;
    public int confirmation_trip;
    public int trip_type;


    @Override
    public String getId() {
        return trip_id;
    }

    /**
     * Sets the parameters for a prepared statement following the parameter order defined in
     * {@link com.conveyal.gtfs.loader.Table#TRIPS}. JDBC prepared statement parameters use a one-based index.
     */
    @Override
    public void setStatementParameters(PreparedStatement statement, boolean setDefaultId) throws SQLException {
        int oneBasedIndex = 1;
        if (!setDefaultId) statement.setInt(oneBasedIndex++, id);
        DateField startDateField = (DateField) Table.CALENDAR.getFieldForName("start_date");
        DateField endDateField = ((DateField) Table.CALENDAR.getFieldForName("end_date"));
        statement.setString(oneBasedIndex++, trip_id);
        statement.setString(oneBasedIndex++, route_id);
        statement.setString(oneBasedIndex++, service_id);
        statement.setString(oneBasedIndex++, trip_headsign);
        statement.setString(oneBasedIndex++, trip_short_name);
        setIntParameter(statement, oneBasedIndex++, direction_id);
        statement.setString(oneBasedIndex++, block_id);
        statement.setString(oneBasedIndex++, shape_id);
        setIntParameter(statement, oneBasedIndex++, wheelchair_accessible);
        setIntParameter(statement, oneBasedIndex++, bikes_allowed);
        setIntParameter(statement, oneBasedIndex++, official_length);
        setIntParameter(statement, oneBasedIndex++, contributed);
        setIntParameter(statement, oneBasedIndex++, seat);
        setIntParameter(statement, oneBasedIndex++, stand);
        startDateField.setParameter(statement, oneBasedIndex++, start_date);
        endDateField.setParameter(statement, oneBasedIndex++, end_date);
        setIntParameter(statement, oneBasedIndex++, confirmation_trip);
        setIntParameter(statement, oneBasedIndex++, trip_type);

        // Editor-specific field? pattern_id
        statement.setString(oneBasedIndex++, null);
    }

    public static class Loader extends Entity.Loader<Trip> {

        public Loader(GTFSFeed feed) {
            super(feed, "trips");
        }

        @Override
        protected boolean isRequired() {
            return true;
        }

        @Override
        public void loadOneRow() throws IOException {
            Trip t = new Trip();

            t.id = row + 1; // offset line number by 1 to account for 0-based row index
            t.route_id        = getStringField("route_id", true);
            t.service_id      = getStringField("service_id", true);
            t.trip_id         = getStringField("trip_id", true);
            t.trip_headsign   = getStringField("trip_headsign", false);
            t.trip_short_name = getStringField("trip_short_name", false);
            t.direction_id    = getIntField("direction_id", false, 0, 1);
            t.block_id        = getStringField("block_id", false); // make a blocks multimap
            t.shape_id        = getStringField("shape_id", false);
            t.bikes_allowed   = getIntField("bikes_allowed", false, 0, 2);
            t.wheelchair_accessible = getIntField("wheelchair_accessible", false, 0, 2);
            t.official_length = getIntField("official_length", false, 0, Integer.MAX_VALUE);
            t.contributed     = getIntField("contributed", false, 0, 1);
            t.seat            = getIntField("seat", false, 0, 1000);
            t.stand           = getIntField("stand", false, 0, 1000);
            t.start_date = getDateField("start_date", true);
            t.end_date = getDateField("end_date", true);
            t.feed = feed;
            t.feed_id = feed.feedId;
            t.confirmation_trip     = getIntField("confirmation_trip", false, 0, 1);
            t.trip_type     = getIntField("trip_type", false, 0, 10);
            // Attempting to put a null key or value will cause an NPE in BTreeMap
            if (t.trip_id != null) feed.trips.put(t.trip_id, t);

            /*
              Check referential integrity without storing references. Trip cannot directly reference Services or
              Routes because they would be serialized into the MapDB.
             */
            // TODO confirm existence of shape ID
            getRefField("service_id", true, feed.services);
            getRefField("route_id", true, feed.routes);
        }

    }

    public static class Writer extends Entity.Writer<Trip> {
        public Writer (GTFSFeed feed) {
            super(feed, "trips");
        }

        @Override
        protected void writeHeaders() throws IOException {
            // TODO: export shapes
            writer.writeRecord(new String[] {"route_id", "trip_id", "trip_headsign", "trip_short_name", "direction_id", "block_id",
                    "shape_id", "bikes_allowed", "wheelchair_accessible", "service_id", "official_length", "contributed", "seat", 
                    "stand", "start_date", "end_date", "confirmation_trip", "trip_type"});
        }

        @Override
        protected void writeOneRow(Trip t) throws IOException {
            writeStringField(t.route_id);
            writeStringField(t.trip_id);
            writeStringField(t.trip_headsign);
            writeStringField(t.trip_short_name);
            writeIntField(t.direction_id);
            writeStringField(t.block_id);
            writeStringField(t.shape_id);
            writeIntField(t.bikes_allowed);
            writeIntField(t.wheelchair_accessible);
            writeStringField(t.service_id);
            writeIntField(t.official_length);
            writeIntField(t.contributed);
            writeIntField(t.seat);
            writeIntField(t.stand);
            writeDateField(t.start_date);
            writeDateField(t.end_date);
            writeIntField(t.confirmation_trip);
            writeIntField(t.trip_type);

            endRecord();
        }

        @Override
        protected Iterator<Trip> iterator() {
            return feed.trips.values().iterator();
        }


    }

}

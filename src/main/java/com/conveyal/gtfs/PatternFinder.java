
package com.conveyal.gtfs;

import com.conveyal.gtfs.error.NewGTFSError;
import com.conveyal.gtfs.error.NewGTFSErrorType;
import com.conveyal.gtfs.error.SQLErrorStorage;
import com.conveyal.gtfs.model.Pattern;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.conveyal.gtfs.util.Util.human;

/**
 * This abstracts out the logic for finding stop sequences ("journey patterns" in Transmodel parlance) based on trips.
 * Placing this logic in a separate class allows us to use it on GTFS data from multiple sources.
 * Our two specific use cases are finding patterns in stop_times that have already been loaded into an RDBMS, and
 * finding patterns while loading Java objects directly into a MapDB database.
 *
 * Created by abyrd on 2017-10-08
 */
public class PatternFinder {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFinder.class);

    // A multi-map that groups trips together by their sequence of stops
    // 5t private Multimap<TripPatternKey, Trip> tripsForPattern = LinkedHashMultimap.create();
    private Multimap<TripPatternKey, Trip> tripsForPattern = HashMultimap.create();

    private int nTripsProcessed = 0;

    public void processTrip(Trip trip, Iterable<StopTime> orderedStopTimes) {
        if (++nTripsProcessed % 100000 == 0) {
            LOG.info("trip {}", human(nTripsProcessed));
        }
        // No need to scope the route ID here, patterns are built within the context of a single feed.
        // Create a key that might already be in the map (by semantic equality)
        TripPatternKey key = new TripPatternKey(trip.route_id);
        for (StopTime st : orderedStopTimes) {
            key.addStopTime(st);
        }
        // Add the current trip to the map, possibly extending an existing list of trips on this pattern.
        tripsForPattern.put(key, trip);
    }

    /**
     * Once all trips have been processed, call this method to produce the final Pattern objects representing all the
     * unique sequences of stops encountered. Returns map of patterns to their keys so that downstream functions can
     * make use of trip pattern keys for constructing pattern stops or other derivative objects.
     *
     * There is no viable relationship between patterns that are loaded from a feed (patternsFromFeed) and patterns
     * generated here. Process ordering is used to update the pattern id and name if patterns from a feed are available.
     * E.g. The first pattern loaded from a feed will be used to updated the first pattern created here.
     */
    public Map<TripPatternKey, Pattern> createPatternObjects(
        Map<String, Stop> stopById,
        List<Pattern> patternsFromFeed,
        SQLErrorStorage errorStorage
    ) {
        // Make pattern ID one-based to avoid any JS type confusion between an ID of zero vs. null value.
        int nextPatternId = 1;
        int patternsFromFeedIndex = 0;
        boolean usePatternsFromFeed = canUsePatternsFromFeed(patternsFromFeed);
        // Create an in-memory list of Patterns because we will later rename them before inserting them into storage.
        // 5t ordiniamo i pattern raggrupparti per route_id
        // Map<TripPatternKey, Pattern> patterns = new LinkedHashMap<>();
        Map<TripPatternKey, Pattern> patterns = new TreeMap<>();
        // TODO assign patterns sequential small integer IDs (may include route)
        for (TripPatternKey key : tripsForPattern.keySet()) {
            Collection<Trip> trips = tripsForPattern.get(key);
            Pattern pattern = new Pattern(key.stops, trips, null);
            if (usePatternsFromFeed) {
                pattern.pattern_id = patternsFromFeed.get(patternsFromFeedIndex).pattern_id;
                pattern.name = patternsFromFeed.get(patternsFromFeedIndex).name;
            } else {
                // Overwrite long UUID with sequential integer pattern ID
                pattern.pattern_id = Integer.toString(nextPatternId++);
            }
            // FIXME: Should associated shapes be a single entry?
            pattern.associatedShapes = new HashSet<>();
            trips.stream().forEach(trip -> pattern.associatedShapes.add(trip.shape_id));
            /* 5t disabilitiamo temporaneamente
            if (pattern.associatedShapes.size() > 1 && errorStorage != null) {
                // Store an error if there is more than one shape per pattern. Note: error storage is null if called via
                // MapDB implementation.
                // TODO: Should shape ID be added to trip pattern key?
                errorStorage.storeError(NewGTFSError.forEntity(
                        pattern,
                        NewGTFSErrorType.MULTIPLE_SHAPES_FOR_PATTERN)
                            .setBadValue(pattern.associatedShapes.toString()));
            }*/
            patterns.put(key, pattern);
            patternsFromFeedIndex++;
        }
        if (!usePatternsFromFeed) {
            // Name patterns before storing in SQL database if they have not already been provided with a feed.
            renamePatterns(patterns.values(), stopById);
        }
        LOG.info("Total patterns: {}", tripsForPattern.keySet().size());
        return patterns;
    }

    /**
     * If there is a difference in the number of patterns provided by a feed and the number of patterns generated here,
     * the patterns provided by the feed are rejected.
     */
    public boolean canUsePatternsFromFeed(List<Pattern> patternsFromFeed) {
        boolean usePatternsFromFeed = patternsFromFeed.size() == tripsForPattern.keySet().size();
        LOG.info("Using patterns from feed: {}",  usePatternsFromFeed);
        return usePatternsFromFeed;
    }

    /**
     * Destructively rename the supplied collection of patterns.
     * This process requires access to all the stops in the feed.
     * Some validators already cache a map of all the stops. There's probably a cleaner way to do this.
     */
    private static void renamePatterns(Collection<Pattern> patterns, Map<String, Stop> stopById) {
        LOG.info("Generating unique names for patterns");

        Map<String, PatternNamingInfo> namingInfoForRoute = new HashMap<>();

        for (Pattern pattern : patterns) {
            if (pattern.associatedTrips.isEmpty() || pattern.orderedStops.isEmpty()) continue;

            // Each pattern within a route has a unique name (within that route, not across the entire feed)

            PatternNamingInfo namingInfo = namingInfoForRoute.get(pattern.route_id);
            if (namingInfo == null) {
                namingInfo = new PatternNamingInfo();
                namingInfoForRoute.put(pattern.route_id, namingInfo);
            }

            // Pattern names are built using stop names rather than stop IDs.
            // Stop names, unlike IDs, are not guaranteed to be unique.
            // Therefore we must track used names carefully to avoid duplicates.

            String fromName = stopById.get(pattern.orderedStops.get(0)).stop_name;
            String toName = stopById.get(pattern.orderedStops.get(pattern.orderedStops.size() - 1)).stop_name;

            namingInfo.fromStops.put(fromName, pattern);
            namingInfo.toStops.put(toName, pattern);

            for (String stopId : pattern.orderedStops) {
                Stop stop = stopById.get(stopId);
                if (fromName.equals(stop.stop_name) || toName.equals(stop.stop_name)) continue;
                namingInfo.vias.put(stop.stop_name, pattern);
            }
            namingInfo.patternsOnRoute.add(pattern);
        }

        // name the patterns on each route
        for (PatternNamingInfo info : namingInfoForRoute.values()) {
            for (Pattern pattern : info.patternsOnRoute) {
                pattern.name = null; // clear this now so we don't get confused later on
                String fromName = stopById.get(pattern.orderedStops.get(0)).stop_name;
                String toName = stopById.get(pattern.orderedStops.get(pattern.orderedStops.size() - 1)).stop_name;

                // check if combination from, to is unique
                Set<Pattern> intersection = new HashSet<>(info.fromStops.get(fromName));
                intersection.retainAll(info.toStops.get(toName));

                if (intersection.size() == 1) {
                    pattern.name = String.format(Locale.US, "%s >> %s", fromName, toName);
                    continue;
                }

                // check for unique via stop
                /* 5t moved below
                pattern.orderedStops.stream().map(stopById::get).forEach(stop -> {
                    Set<Pattern> viaIntersection = new HashSet<>(intersection);
                    viaIntersection.retainAll(info.vias.get(stop.stop_name));

                    if (viaIntersection.size() == 1) {
                        pattern.name = String.format(Locale.US, "%s >> %s via %s", fromName, toName, stop.stop_name);
                    }
                });
                */

                if (pattern.name == null) {
                    // no unique via, one pattern is subset of other.
                    if (intersection.size() == 2) {
                        Iterator<Pattern> it = intersection.iterator();
                        Pattern p0 = it.next();
                        Pattern p1 = it.next();
                        if (p0.orderedStops.size() > p1.orderedStops.size()) {
                            p1.name = String.format(Locale.US, "%s >> %s express", fromName, toName);
                            p0.name = String.format(Locale.US, "%s >> %s local", fromName, toName);
                        } else if (p1.orderedStops.size() > p0.orderedStops.size()){
                            p0.name = String.format(Locale.US, "%s >> %s express", fromName, toName);
                            p1.name = String.format(Locale.US, "%s >> %s local", fromName, toName);
                        }
                    }
                }

                // check for unique via stop
                for (String stopId : pattern.orderedStops) {
                    Set<Pattern> viaIntersection = new HashSet<>(intersection);
                    Stop stop = stopById.get(stopId);
                    viaIntersection.retainAll(info.vias.get(stop.stop_name));

                    if (viaIntersection.size() == 1) {
                        if(pattern.name == null)
                            pattern.name = String.format(Locale.US, "%s >> %s via %s", fromName, toName, stop.stop_name);
                    }
                }

                if (pattern.name == null) {
                    // give up
                    pattern.name = String.format(Locale.US, "%s >> %s like trip %s", fromName, toName, pattern.associatedTrips.get(0));
                }
            }

            // 5t removed trips and stops count from pattern name
            // attach a stop and trip count to each
            /*for (Pattern pattern : info.patternsOnRoute) {
                pattern.name = String.format(Locale.US, "%s stops %s (%s trips)",
                        pattern.orderedStops.size(), pattern.name, pattern.associatedTrips.size());
            }*/
        }
    }

    /**
     * Holds information about all pattern names on a particular route,
     * modeled on https://github.com/opentripplanner/OpenTripPlanner/blob/master/src/main/java/org/opentripplanner/routing/edgetype/TripPattern.java#L379
     */
    private static class PatternNamingInfo {
        // These are all maps from ?
        // FIXME For type safety and clarity maybe we should have a parameterized ID type, i.e. EntityId<Stop> stopId.
        Multimap<String, Pattern> fromStops = HashMultimap.create();
        Multimap<String, Pattern> toStops = HashMultimap.create();
        Multimap<String, Pattern> vias = HashMultimap.create();
        List<Pattern> patternsOnRoute = new ArrayList<>();
    }

}

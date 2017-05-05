package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class Communicator
{

    private static Connection connection = null;
    private static final String DELETE_PROCESSED_POINTS_FROM_INTERIM = "DELETE FROM interim.matching";
    private static final String SELECT_POINT_WRKT_INTO_TABLE =
            "INSERT INTO interim.matching (id, dt, geom, distance_offset, duration_offset, buffer) SELECT id, dt, geom, distance_offset, duration_offset, st_buffer(geom, 0.00006) FROM production.point_fl WHERE wrkt_id = %d";
    private static final String CREATE_NETWORK_VIEW = "CREATE OR REPLACE VIEW interim.network AS SELECT mi.id, mi.geom, mi.buffer, mi.source, mi.target, mi.length FROM network.miami_dade mi WHERE st_intersects(mi.buffer, st_geomfromtext('%s', 4326));";
    private static final String CALC_EXTENT =
            "SELECT st_astext(st_extent(buffer)) FROM interim.matching";
    private static final String PAIR_SEGMENTS_WITH_POINTS =
            "SELECT m.id as point_id,  n.id as segment_id, m.dt, m.distance_offset, m.duration_offset, n.source, n.target FROM interim.matching m JOIN interim.network n ON st_intersects(n.buffer, m.buffer) ORDER BY point_id;";
    private static final String GET_WRKT_DETAIL = "SELECT duration, distance, start_at, athlete_id, timezone FROM production.workout WHERE id = ?";
    static WorkoutDetail currentWorkout;
    private static final String CALC_ROUTE = "SELECT path_seq, edge, mia.source, mia.target, cost FROM pgr_dijkstra('SELECT id, source, target, st_length(st_transform(geom,26917)) as cost FROM interim.network', ?, ?, directed:=false) as di JOIN interim.network mia ON di.edge = mia.id ORDER BY path_seq;";
    private static final String INSERT_PROBABLE_ROUTE =
            "INSERT INTO interim.restored_route (id, func_class, speed_cat, geom, source, target, length, athlete_id, dt, wrkt_id, in_digit_dir, seq, dead_end, p_loop)\n" +
                    "SELECT mia.id, mia.func_class, mia.speed_cat, mia.geom, mia.source, mia.target, mia.length, ?, '%s', ?, ?, ?, ?, ?\n" +
                    "FROM network.miami_dade mia\n" +
                    "WHERE mia.id = ?;";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ");


    static boolean initialize()
    {
        boolean success;
        try
        {
            DbConnector.connectToDb();
            connection = DbConnector.getConnection();
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    static boolean terminate()
    {
        boolean success;

        try
        {
            DbConnector.closeConnection();
            success = true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    static boolean fillInWorkoutDetail(int wrktId)
    {
        boolean fullSuccess;
        currentWorkout = new WorkoutDetail(wrktId);
        PreparedStatement statement;
        ResultSet resultSet = null;
        String timezone = "-05:00";
        try
        {
            statement = connection.prepareStatement(GET_WRKT_DETAIL);
            statement.setInt(1, wrktId);
            resultSet = statement.executeQuery();
            resultSet.next();
            timezone = resultSet.getString("timezone");
            currentWorkout.setUserId(resultSet.getInt("athlete_id"));
            currentWorkout.setDuration(resultSet.getDouble("duration"));
            currentWorkout.setDistance(resultSet.getDouble("distance"));
            String time =
                    resultSet.getTimestamp("start_at").toString().substring(0,20) + "000"
                            + timezone;
            currentWorkout.setStartAt(FORMATTER.parseDateTime(time));

            fullSuccess = true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fullSuccess = false;
        }
        finally
        {
            currentWorkout.setTimeZone(DateTimeZone.forID(timezone));
            try {
                assert resultSet != null;
                resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fullSuccess;
    }

    static boolean selectWrktPointsIntoTable(int wrktId)
    {

        boolean created;
        Statement statement = null;
        String query = String.format(SELECT_POINT_WRKT_INTO_TABLE, wrktId);
        try
        {
            statement = connection.createStatement();
            statement.execute(DELETE_PROCESSED_POINTS_FROM_INTERIM);
            statement.execute(query);
            created = true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            created = false;
        }
        finally
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return created;
    }

    static String calculateExtent()
    {
        Statement statement = null;
        String extent = null;
        try
        {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(CALC_EXTENT);
            resultSet.next();
            extent = resultSet.getString(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                assert statement != null;
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return extent;
    }

    static boolean createNetworkViewForExtent(String extent)
    {
        boolean created;
        Statement statement = null;
        String query = String.format(CREATE_NETWORK_VIEW, extent);
        try
        {
            statement = connection.createStatement();
            statement.execute(query);
            created = true;
        } catch (SQLException e) {
            e.printStackTrace();
            created = false;
        }
        finally
        {
            try
            {
                assert statement != null;
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return created;
    }

    static ArrayList<SegmentPairedWithPoint> intersectBuffers()
    {

        ArrayList<SegmentPairedWithPoint> segments = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet = null;

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(PAIR_SEGMENTS_WITH_POINTS);
            while (resultSet.next())
            {
                SegmentPairedWithPoint s = new SegmentPairedWithPoint();
                s.pointId = resultSet.getInt("point_id");
                s.segmentId = resultSet.getInt("segment_id");

                String time =
                        resultSet.getTimestamp("dt").toString().substring(0,20) + "000"
                                + currentWorkout.getTimeZone().getID();
                s.dt = FORMATTER.parseDateTime(time);
                s.distance_offset = resultSet.getDouble("distance_offset");
                s.duration_offset = resultSet.getDouble("duration_offset");
                s.source = resultSet.getInt("source");
                s.target = resultSet.getInt("target");
                s.complete = true;
                segments.add(s);
            }

        }
        catch (SQLException e)
        {
            segments = null;
            e.printStackTrace();
        }
        finally
        {
            try
            {
                assert statement != null;
                statement.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                assert resultSet != null;
                resultSet.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }


        return segments;
    }

    static Route restoreTrip(ArrayList<SegmentPairedWithPoint> probableSegments)
    {
        Route workoutRoute = new Route();
        List<SegmentPairedWithPoint> probableRoute = new LinkedList<>();
        SegmentPairedWithPoint firstInPair, secondInPair;
        List<Integer> indicesOfDistinctPairs = new LinkedList<>();

        boolean startFound = false, endFound = false;

        if (probableSegments.get(0).pointId > 1)
        {
            //TODO: get id of the closest segment
            /*
            * SELECT r.id, r.source, r.target, r.length
            FROM interim.network r, interim.matching m
              WHERE m.id = -1
            ORDER BY ST_Distance(r.geom, m.geom) ASC
            LIMIT 1;
*/
        }

        // if second pair has another point_id, than first pair is distinct and is the start
        if (probableSegments.get(0).pointId != probableSegments.get(1).pointId)
        {
            indicesOfDistinctPairs.add(0);
            startFound = true;
        }

        int lastAddedSegmentId = probableSegments.get(0).segmentId; // may be was not really added
        // choose all further distinct pairs
        for (int i = 1; i < probableSegments.size() - 1; i++)
        {
            if (probableSegments.get(i).pointId != probableSegments.get(i - 1).pointId &&
                    probableSegments.get(i).pointId != probableSegments.get(i + 1).pointId &&
                    probableSegments.get(i).segmentId != lastAddedSegmentId)
            {
                indicesOfDistinctPairs.add(i);
                lastAddedSegmentId = probableSegments.get(i).segmentId;
            }
        }

        if (probableSegments.get(probableSegments.size() - 1).pointId !=
                probableSegments.get(probableSegments.size() - 2).pointId ||
                probableSegments.get(probableSegments.size() - 1).segmentId ==
                        probableSegments.get(indicesOfDistinctPairs.get(indicesOfDistinctPairs.size() - 1)).segmentId)
        {
            if (probableSegments.get(probableSegments.size() - 1).segmentId ==
                    probableSegments.get(indicesOfDistinctPairs.get(indicesOfDistinctPairs.size() - 1)).segmentId)
            {
                indicesOfDistinctPairs.remove(indicesOfDistinctPairs.size() - 1);
            }
            indicesOfDistinctPairs.add(probableSegments.size() - 1);

            endFound = true;
        }

        probableRoute.add(probableSegments.get(indicesOfDistinctPairs.get(0)));

        for (int i = 1; i < indicesOfDistinctPairs.size(); i++)
        {
            firstInPair = probableSegments.get(indicesOfDistinctPairs.get(i-1));
            secondInPair = probableSegments.get(indicesOfDistinctPairs.get(i));

            if (firstInPair.segmentId != secondInPair.segmentId)
            {
                if (determineDirectionsOfSegments(firstInPair,
                        secondInPair) == DirectionsInPair.NOT_ADJ)
                {
                    Route route = calculateRoute(firstInPair.target, secondInPair.source);
                    if (route != null)
                    {
                        if (route.roadSegments.size() == 0)
                        {
                            System.out.println("We have an issue with route calculation! " +
                                    "Segments not adjacent, but route has length 0");
                        }
                        else
                        {
                            findCandidateStatistics(route, indicesOfDistinctPairs.get(i-1),
                                    indicesOfDistinctPairs.get(i), probableSegments);
                            for (SegmentPairedWithPoint s: route.roadSegments)
                            {
                                probableRoute.add(s);
                            }
                        }

                    }
                }
                probableRoute.add(secondInPair);
            }
        }

        if (!startFound)
        {
            SegmentPairedWithPoint known = probableRoute.get(0);
            SegmentPairedWithPoint current;
            int idOfInitialPoint = probableSegments.get(0).pointId;
            for (int i = 0; i < indicesOfDistinctPairs.get(0); i++)
            {
                current = probableSegments.get(i);
                if (current.pointId == idOfInitialPoint
                        && current.segmentId != known.segmentId)
                {
                    DirectionsInPair directions = determineDirectionsOfSegments(current, known);
                    if (directions != DirectionsInPair.NOT_ADJ)
                    {
                        probableRoute.add(0, current);
                        startFound = true;
                        break;
                    }
                }
            }
            if (!startFound) // there are segments between the start and the first definitely known segment
            {
                // for all segment-point pairs before first distinct take such a pair and
                // calculate route from it to the first distinct segment
                SegmentPairedWithPoint probableStart;
                int i = 0;
                while (probableSegments.get(i).pointId == probableSegments.get(0).pointId)
                {
                    probableStart = probableSegments.get(i);
                    Route route = calculateRoute(probableStart.target, known.source);
                    //this route can contain start and/or end segment or just segments between them,
                    // depending on the direction of digitization
                    if (route.roadSegments != null)
                    {
                        double distanceFromPoints, distanceFromCost = 0;
                        for (SegmentPairedWithPoint s: route.roadSegments)
                        {
                            distanceFromCost += s.cost;
                        }
                        distanceFromPoints = (probableRoute.get(0).distance_offset
                                                - probableSegments.get(i).distance_offset) * 1000;
                        // 0.3 for now is a magic number, which needs to be tested
                        if ( distanceFromCost < distanceFromPoints || (distanceFromCost - distanceFromPoints) < distanceFromPoints * 0.3)
                        {
                            findCandidateStatistics(route, 0, indicesOfDistinctPairs.get(0), probableSegments);
                            for (SegmentPairedWithPoint s: route.roadSegments)
                            {
                                probableRoute.add(0, s);
                            }

                            if (probableRoute.get(0).segmentId != probableSegments.get(i).segmentId)
                            {
                                probableRoute.add(0, probableSegments.get(i));
                            }
                            break;
                        }
                    }
                    i++;
                }
            }

        }
        if (!endFound)
        {
            SegmentPairedWithPoint lastKnown = probableRoute.get(probableRoute.size() - 1);
            SegmentPairedWithPoint possibleNext;
            int idOfLastKnownPoint = probableSegments.get(probableSegments.size()-1).pointId;
            for (int i = probableSegments.size() - 1;
                 i > indicesOfDistinctPairs.get(indicesOfDistinctPairs.size()-1);
                 i--)
            {
                possibleNext = probableSegments.get(i);
                if (possibleNext.pointId == idOfLastKnownPoint && possibleNext.segmentId != lastKnown.segmentId)
                {
                    if (determineDirectionsOfSegments(lastKnown, possibleNext) != DirectionsInPair.NOT_ADJ)
                    {
                        probableRoute.add(0, possibleNext);
                        endFound = true;
                        break;
                    }
                }
            }
            if (!endFound)
            {
                SegmentPairedWithPoint probableEnd;
                int i = probableSegments.size() - 1;
                while (probableSegments.get(i).pointId == probableSegments.get(probableSegments.size() - 1).pointId)
                {
                    probableEnd = probableSegments.get(i);
                    Route route =
                            calculateRoute(lastKnown.target, probableEnd.source);
                    if (route.roadSegments != null)
                    {
                        double distanceFromCost = 0;
                        for (SegmentPairedWithPoint s: route.roadSegments)
                        {
                            distanceFromCost += s.cost;
                        }
                        double distanceFromPoints = Math.abs(( probableSegments.get(i).distance_offset -
                                probableRoute.get(probableRoute.size()-1).distance_offset ) * 1000);
                        // 0.3 for now is a magic number, which needs to be tested
                        if (Math.abs(distanceFromCost - distanceFromPoints) < distanceFromPoints * 0.3)
                        {
                            probableRoute.add(probableSegments.get(i));
                            findCandidateStatistics(route, indicesOfDistinctPairs.get(indicesOfDistinctPairs.size()-1),
                                                    i, probableSegments);
                            break;
                        }
                    }
                    i--;
                }
            }
        }


        if (probableRoute.get(0).dt == null)
        {
            probableRoute.get(0).dt = currentWorkout.getStartAt();
        }

        if (probableRoute.get(probableRoute.size()-1).dt == null)
        {
            long end = currentWorkout.getStartAt().getMillis() + (long)(currentWorkout.getDuration()*1000);
            probableRoute.get(probableRoute.size()-1).dt = new DateTime(end, currentWorkout.getStartAt().getZone());
        }

        if (probableRoute.get(probableRoute.size()-1).distance_offset == 0)
        {
            probableRoute.get(probableRoute.size()-1).distance_offset = currentWorkout.getDistance();
        }

        int leftKnownIndex;
        int rightKnownIndex;

        int i = 1;
        int j;
        while (i < probableRoute.size())
        {
            if (probableRoute.get(i).dt == null)
            {
                leftKnownIndex = i - 1;
                j = i + 1;
                while (j < probableRoute.size())
                {
                    if (probableRoute.get(j).dt != null)
                    {
                        rightKnownIndex = j;
                        for (int m = leftKnownIndex + 1; m < rightKnownIndex; m++)
                        {
                            probableRoute.get(m).dt =  calculateMeanDateTime(probableRoute.get(leftKnownIndex),
                                    probableRoute.get(rightKnownIndex));

                            probableRoute.get(m).distance_offset =  calculateMeanDateDistance
                                    (probableRoute.get(leftKnownIndex), probableRoute.get(rightKnownIndex));
                        }
                        i = j + 1;
                        break;
                    }
                    j++;
                }
            }
            else
            {
                i++;
            }
        }


        for (i = 1; i < probableRoute.size(); i++ )
        {
            if (determineDirectionsOfSegments(probableRoute.get(i-1), probableRoute.get(i)) == DirectionsInPair.NOT_ADJ)
            {
                workoutRoute.dubious = true;
            }
        }

        workoutRoute.roadSegments = probableRoute;
        return  workoutRoute;
    }

    private static void findCandidateStatistics(Route route, int startIndex, int endIndex,
                                                List<SegmentPairedWithPoint> probableSegments)
    {
        for (SegmentPairedWithPoint s: route.roadSegments)
        {
            for (int j = startIndex; j < endIndex; j++)
            {
                SegmentPairedWithPoint candidate = probableSegments.get(j);
                if (s.segmentId == candidate.segmentId)
                {
                    s.pointId = candidate.pointId;
                    s.distance_offset = candidate.distance_offset;
                    s.dt = candidate.dt;
                    s.duration_offset = candidate.duration_offset;
                    s.complete = true;
                    break;
                }
            }
        }
    }

    static void insertRouteIntoDb(Route probableRoute, int wrkt_id, int athlete_id)
    {
        PreparedStatement statement = null;

        try
        {
            connection.setAutoCommit(false);
            int i = 0;
            int insertedCount = 0;
            for (SegmentPairedWithPoint s: probableRoute.roadSegments)
            {
                String query = String.format(INSERT_PROBABLE_ROUTE, s.dt.toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
                statement = connection.prepareStatement(query);
                statement.setInt(1, athlete_id);
                statement.setInt(2, wrkt_id);
                try {
                    statement.setBoolean(3, s.activityInDigitizationDirection);
                } catch (Exception e) {
                    statement.setNull(3, Types.BOOLEAN);
                }
                statement.setInt(4, i);
                try {
                    statement.setBoolean(5, probableRoute.deadEnd);
                } catch (Exception e) {
                    statement.setNull(5, Types.BOOLEAN);
                }
                try {
                    statement.setBoolean(6, probableRoute.loop);
                } catch (Exception e) {
                    statement.setNull(6, Types.BOOLEAN);
                }
                statement.setInt(7, s.segmentId);
                i++;
                insertedCount += statement.executeUpdate();
                statement.clearParameters();

            }
            connection.commit();
            System.out.println(insertedCount + " inserted");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (statement != null)
            {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static Route calculateRoute(int source, int target)
    {
        Route route = null;
        ArrayList<SegmentPairedWithPoint> segments = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(CALC_ROUTE);
            statement.setInt(1, source);
            statement.setInt(2, target);
            resultSet = statement.executeQuery();
            segments = new ArrayList<>();
            SegmentPairedWithPoint segment;
            while (resultSet.next())
            {
                segment = new SegmentPairedWithPoint();
                segment.segmentId = resultSet.getInt("edge");
                segment.source = resultSet.getInt("source");
                segment.target = resultSet.getInt("target");
                segment.cost = resultSet.getDouble("cost");
                segments.add(segment);
            }
            for (int i = 1; i < segments.size(); i++)
            {
                determineDirectionsOfSegments(segments.get(i-1), segments.get(i));
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (statement != null)
            {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (resultSet != null)
            {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (segments != null)
        {
            route = new Route();
            route.roadSegments = segments;
        }
        return route;
    }

    private static DirectionsInPair determineDirectionsOfSegments
            (SegmentPairedWithPoint firstInPair, SegmentPairedWithPoint secondInPair)
    {
        DirectionsInPair directions = DirectionsInPair.NOT_ADJ;

        if (firstInPair.target == secondInPair.source)
        {
            directions = DirectionsInPair.IDD_IDD;
            firstInPair.activityInDigitizationDirection = true;
            secondInPair.activityInDigitizationDirection = true;
        }
        else if (firstInPair.target == secondInPair.target)
        {
            directions = DirectionsInPair.IDD_ADD;
            firstInPair.activityInDigitizationDirection = true;
            secondInPair.activityInDigitizationDirection = false;
        }
        else if (firstInPair.source == secondInPair.source)
        {
            directions = DirectionsInPair.ADD_IDD;
            firstInPair.activityInDigitizationDirection = false;
            secondInPair.activityInDigitizationDirection = true;
        }
        else if (firstInPair.source == secondInPair.target)
        {
            directions = DirectionsInPair.ADD_ADD;
            firstInPair.activityInDigitizationDirection = false;
            secondInPair.activityInDigitizationDirection = false;
        }
        return directions;
    }

    private static DateTime calculateMeanDateTime(SegmentPairedWithPoint left,
                               SegmentPairedWithPoint right)
    {
        long millis = left.dt.getMillis() + ( right.dt.getMillis() - left.dt.getMillis() )/ 2;
        return new DateTime(millis, currentWorkout.getTimeZone());

    }

    private static double calculateMeanDateDistance(SegmentPairedWithPoint left,
                                                  SegmentPairedWithPoint right)
    {
        return left.distance_offset + (right.distance_offset - left.distance_offset) / 2;
    }

                       /* long millis = probableRoute.get(leftKnownIndex).dt.getMillis() + (
                                    probableRoute.get(rightKnownIndex).dt.getMillis() -
                                    probableRoute.get(leftKnownIndex).dt.getMillis()
                            )/ 2;
                    probableRoute.get(i).dt = new DateTime(millis, currentWorkout.getTimeZone());
                    probableRoute.get(i).distance_offset = probableRoute.get(leftKnownIndex).distance_offset +
                            ( probableRoute.get(rightKnownIndex).distance_offset -
                              probableRoute.get(leftKnownIndex).distance_offset )/ 2;*/

}























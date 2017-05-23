package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Project: Endomondo
 * Class: Communicator
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
            "INSERT INTO interim.matching (id, dt, geom, distance_offset, duration_offset, buffer) SELECT id, dt, geom, distance_offset, duration_offset, st_buffer(geom, 0.00006) FROM production.point_mia WHERE wrkt_id = %d";
    private static final String COUNT_POINTS = "SELECT count(*) FROM interim.matching;";
    private static final String CREATE_NETWORK_VIEW = "CREATE OR REPLACE VIEW interim.network AS SELECT mi.id, mi.geom, mi.buffer, mi.big_buffer, mi.source, mi.target, mi.cost FROM network.miami_dade mi WHERE st_intersects(mi.big_buffer, st_geomfromtext('%s', 4326));";
    private static final String CALC_EXTENT =
            "SELECT st_astext(st_extent(buffer)) FROM interim.matching";
    /*private static final String PAIR_SEGMENTS_WITH_POINT_BUFFERS =
            "SELECT m.id as point_id,  n.id as segment_id, m.dt, m.distance_offset, m.duration_offset, n.source, n.target FROM interim.matching m JOIN interim.network n ON st_intersects(n.geom, m.buffer) ORDER BY point_id;";*/
    private static final String PAIR_SEGMENTS_WITH_POINTS =
            "SELECT m.id as point_id,  n.id as segment_id, m.dt, m.distance_offset, m.duration_offset, n.source, n.target, n.cost FROM interim.matching m JOIN network.miami_dade n ON st_intersects(n.buffer, m.buffer) ORDER BY point_id;";
    private static final String PAIR_SEGMENTS_WITH_POINTS_BIG_BUFFER =
            "SELECT m.id as point_id,  n.id as segment_id, m.dt, m.distance_offset, m.duration_offset, n.source, n.target, n.cost FROM interim.matching m JOIN network.miami_dade n ON st_intersects(n.big_buffer, m.buffer) ORDER BY point_id;";

    private static final String GET_WRKT_DETAIL = "SELECT duration, distance, start_at, athlete_id, timezone FROM production.workout WHERE id = ?";
    static WorkoutDetail currentWorkout;
    private static final String CALC_ROUTE = "SELECT path_seq, edge, mia.source, mia.target, mia.cost FROM pgr_dijkstra('SELECT id, source, target, st_length(st_transform(geom,26917)) as cost FROM network.miami_dade', ?, ?, directed:=false) as di JOIN network.miami_dade mia ON di.edge = mia.id ORDER BY path_seq;";
    private static final String INSERT_ROUTE_SEGMENTS =
            "INSERT INTO production.restored_route (id, func_class, geom, source, target, length, athlete_id, dt, wrkt_id, in_digit_dir, seq, point_id)\n" +
                    "SELECT mia.id, mia.func_class, mia.geom, mia.source, mia.target, mia.cost, ?, '%s', ?, ?, ?, ?\n" +
                    "FROM network.miami_dade mia\n" +
                    "WHERE mia.id = ?;";
    private static final String INSERT_PROBABLE_ROUTE = "INSERT INTO interim.restored_route_detail (id, dead_end, loop, dbs, segment_cnt) VALUES (?, ?, ?, ?, ?)";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
    private static final String GET_CLOSEST_SEGMENT = "SELECT r.id, m.dt, r.source, r.target, r.cost, r.cost FROM network.miami_dade r, interim.matching m WHERE m.id = ? ORDER BY ST_Distance(r.geom, m.geom) ASC LIMIT 1;";
    private static final String GET_IDS = "SELECT distinct wrkt_id FROM production.ctr_wrkt_ath_detail ORDER BY wrkt_id;";
    private static final String GET_PROCESSED_IDS = "SELECT distinct id FROM interim.restored_route_detail ORDER BY id;";

    static int pointCount = 0;

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
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
            success = false;
        }
        return success;
    }

    static ArrayList<Integer> getWrktIds()
    {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<Integer> processedIds = new ArrayList<>();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(GET_IDS);
            while (resultSet.next())
            {
                ids.add(resultSet.getInt(1));
            }
            resultSet.close();
            resultSet = statement.executeQuery(GET_PROCESSED_IDS);
            while (resultSet.next())
            {
                processedIds.add(resultSet.getInt(1));
            }
        } catch (SQLException e)
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
                 System.out.println(e.getMessage());
             }
         }
         if (resultSet != null)
         {
             try {
                 resultSet.close();
             } catch (SQLException e) {
                 System.out.println(e.getMessage());
             }
         }
        }
        for (int i = 0; i < ids.size(); i++)
        {
            boolean removed = false;
            for (int j = 0; j < processedIds.size(); j++)
            {
                if (processedIds.get(j).intValue() == ids.get(i).intValue())
                {
                    processedIds.remove(j);
                    ids.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed)
            {
                i--;
            }
        }
        return ids;
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
            try {
                currentWorkout.setStartAt(FORMATTER.parseDateTime(time));
            }
            catch (Exception e)
            {
                time =
                        resultSet.getTimestamp("start_at").toString().substring(0,20) + "000"
                                + "-05:00";
                currentWorkout.setStartAt(FORMATTER.parseDateTime(time));
                timezone = "-05:00";
            }

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
                System.out.println(e.getMessage());
            }
        }
        return fullSuccess;
    }

    static boolean selectWrktPointsIntoTable(int wrktId)
    {

        boolean created;
        Statement statement = null;
        String query = String.format(SELECT_POINT_WRKT_INTO_TABLE, wrktId);
        ResultSet count = null;
        try
        {
            statement = connection.createStatement();
            statement.execute(DELETE_PROCESSED_POINTS_FROM_INTERIM);
            statement.execute(query);
            count = statement.executeQuery(COUNT_POINTS);
            count.next();
            pointCount = count.getInt(1);
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
                    System.out.println(e.getMessage());
                }
            }
            if (count != null)
            {
                try
                {
                    count.close();
                }
                catch (SQLException e)
                {
                    System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
            }
        }

        return created;
    }

    static LinkedList<SegmentPairedWithPoint> intersectBuffers()
    {
        LinkedList<SegmentPairedWithPoint> standardPairs = new LinkedList<>();
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
                s.cost = resultSet.getDouble("cost");
                s.source = resultSet.getInt("source");
                s.target = resultSet.getInt("target");
                s.complete = true;
                standardPairs.add(s);
            }
            try
            {
                resultSet.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            Map<Integer, List<SegmentPairedWithPoint>> standardPairMap = standardPairs.stream()
                    .collect(Collectors.groupingBy(SegmentPairedWithPoint::getPointId));
            int countDistinctPairs = 0;

            for (Integer key: standardPairMap.keySet())
            {
                if (standardPairMap.get(key).size() == 1)
                {
                    countDistinctPairs++;
                }
            }

            if (countDistinctPairs > 0 && countDistinctPairs < pointCount / 3)
            {
                ArrayList<SegmentPairedWithPoint> bigBufferPairs = new ArrayList<>();
                try
                {
                    resultSet = statement.executeQuery(PAIR_SEGMENTS_WITH_POINTS_BIG_BUFFER);
                    while (resultSet.next())
                    {
                        SegmentPairedWithPoint s = new SegmentPairedWithPoint(resultSet.getInt("segment_id"));
                        s.pointId = resultSet.getInt("point_id");
                        String time =
                                resultSet.getTimestamp("dt").toString().substring(0,20) + "000"
                                        + currentWorkout.getTimeZone().getID();
                        s.dt = FORMATTER.parseDateTime(time);

                        s.distance_offset = resultSet.getDouble("distance_offset");
                        s.duration_offset = resultSet.getDouble("duration_offset");
                        s.cost = resultSet.getDouble("cost");
                        s.source = resultSet.getInt("source");
                        s.target = resultSet.getInt("target");
                        s.complete = true;
                        bigBufferPairs.add(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Map<Integer, List<SegmentPairedWithPoint>> bigBufferPairMap = bigBufferPairs.stream()
                        .collect(Collectors.groupingBy(SegmentPairedWithPoint::getPointId));

                if (standardPairMap.keySet().size() < bigBufferPairMap.keySet().size())
                {
                    unite(0, 0, standardPairs, bigBufferPairs);
                }
            }
        }
        catch (SQLException e)
        {
            standardPairs = null;
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
                System.out.println(e.getMessage());
            }

            try
            {
                assert resultSet != null;
                resultSet.close();
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
            }
        }

        return standardPairs;
    }

    private static void unite(int i, int j, List<SegmentPairedWithPoint> standardPairs,
                              List<SegmentPairedWithPoint> bigBufferPairs)
    {
        int bigIndex = i;
        int standardIndex = j;

        while (bigIndex < bigBufferPairs.size() && standardIndex < standardPairs.size()
                && bigBufferPairs.get(bigIndex).pointId < standardPairs.get(standardIndex).pointId)
        {
            standardPairs.add(standardIndex, bigBufferPairs.get(bigIndex));
            bigIndex++;
            standardIndex++;
        }
        while (bigIndex < bigBufferPairs.size() && standardIndex < standardPairs.size()
                && bigBufferPairs.get(bigIndex).pointId == standardPairs.get(standardIndex).pointId) {
            bigIndex++;
        }
        while( bigIndex < bigBufferPairs.size() && standardIndex < standardPairs.size()
                && bigBufferPairs.get(bigIndex).pointId > standardPairs.get(standardIndex).pointId)
        {
            standardIndex++;
        }
        if(bigIndex < bigBufferPairs.size())
        {
            if (standardIndex < standardPairs.size())
            {
                unite(bigIndex, standardIndex, standardPairs, bigBufferPairs);
            }
            else
            {
                while (bigIndex < bigBufferPairs.size())
                {
                    standardPairs.add(bigBufferPairs.get(bigIndex));
                }
            }
        }
    }

    static Route restoreTrip(LinkedList<SegmentPairedWithPoint> probableSegments)
    {
        Route workoutRoute = null;
        if (probableSegments != null && probableSegments.size() > 0)
        {
            workoutRoute = new Route();
            workoutRoute.id = currentWorkout.getId();
            List<SegmentPairedWithPoint> probableRoute = new LinkedList<>();
            SegmentPairedWithPoint firstInPair, secondInPair;
            List<Integer> indicesOfDistinctPairs = new LinkedList<>();
            int deadEndCount = 0;

            boolean firstProbableDistinct = false, lastProbableDistinct = false;
            int lastAddedSegmentId = -1;

            // if second pair has another point_id, than first pair is distinct and is the start
            if (probableSegments.size() > 1 && probableSegments.get(0).pointId != probableSegments.get(1).pointId)
            {
                indicesOfDistinctPairs.add(0);
                firstProbableDistinct = true;
                lastAddedSegmentId = probableSegments.get(0).segmentId;
            }
            else
            {
                for (int x = 0; x < 5; x++)
                {
                    SegmentPairedWithPoint mayBeFirst = getClosestSegment(x);
                    if (mayBeFirst != null)
                    {
                        if (mayBeFirst.segmentId != probableSegments.get(0).segmentId)
                        {
                            probableSegments.add(0, mayBeFirst);
                        }
                        indicesOfDistinctPairs.add(0, 0);
                        lastAddedSegmentId = mayBeFirst.segmentId;
                        break;
                    }
                }
            }

            // choose further distinct pairs (last and first probable segments are not added to distinct in this loops)
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

            // add last distinct segment to the distinct indices
            // if two last probable pairs have different point id and segment id
            int lastProbable = probableSegments.size() - 1;
            int oneBeforeLastProbable = probableSegments.size() - 2;
            if (lastProbable > 0 &&
                    (probableSegments.get(lastProbable).pointId != probableSegments.get(oneBeforeLastProbable).pointId))
            {
                if (probableSegments.get(lastProbable).segmentId ==
                        probableSegments.get(indicesOfDistinctPairs.get(indicesOfDistinctPairs.size() - 1)).segmentId)
                {
                    indicesOfDistinctPairs.remove(indicesOfDistinctPairs.size() - 1);
                }
                indicesOfDistinctPairs.add(lastProbable);
                lastProbableDistinct = true;
            }


            if (indicesOfDistinctPairs.size() > 0 && probableSegments.size() > 0)
            {
                probableRoute.add(probableSegments.get(indicesOfDistinctPairs.get(0)));
            }

            if (probableRoute.size() > 0)
            {
                for (int i = 1; i < indicesOfDistinctPairs.size(); i++)
                {
                    System.out.println(i);
                    firstInPair = probableRoute.get(probableRoute.size()-1);
                    secondInPair = probableSegments.get(indicesOfDistinctPairs.get(i));

                    if (firstInPair.segmentId != secondInPair.segmentId)
                    {
                        // if 2 distinct segments are not adjacent
                        if (determineDirectionsOfSegments(firstInPair,
                                secondInPair) == DirectionsInPair.NOT_ADJ)
                        {   // calculate route between them
                            Route route = calculateRoute(firstInPair, secondInPair);
                            if (route != null)
                            {
                                // if it is empty, there is a break in network
                                if (route.roadSegments.size() == 0)
                                {
                                    System.out.println("Break in network: segments not adjacent, but route has length 0");
                                }
                                else
                                {
                                    try
                                    {   // check whether first restored segment is the same as last known
                                        if (route.roadSegments.get(0).segmentId == firstInPair.segmentId)
                                        {
                                            // if so and this is not the last segment of the road where a turn back took place
                                            //(in this case previous and next segment is the same segment)
                                            if (route.roadSegments.get(1).segmentId !=
                                                    probableRoute.get(probableRoute.size()-2).segmentId)
                                            {
                                                // and if the previous and the next segments are not adjacent
                                                if (determineDirectionsOfSegments(route.roadSegments.get(1),
                                                        probableRoute.get(probableRoute.size()-2)) == DirectionsInPair.NOT_ADJ)
                                                {
                                                    // it's a duplicate and has to be removed
                                                    route.roadSegments.remove(0);
                                                }
                                            }
                                        }
                                        // if one but last known segment is adjacent to the first restored segment, then
                                        // the last known segment between them must be covered in both directions or deleted

                                        if (probableRoute.size()-2 > 0 &&
                                                determineDirectionsOfSegments(probableRoute.get(probableRoute.size()-2),
                                                route.roadSegments.get(0)) != DirectionsInPair.NOT_ADJ)
                                        {
                                            //look for the last offset distance associated with the last but one known segment
                                            double distancePrevious = probableRoute.get(probableRoute.size()-2).distance_offset;
                                            for (int x = indicesOfDistinctPairs.get(i-1)-1;
                                                 x > indicesOfDistinctPairs.get(i-2); x--)
                                            {
                                                if (probableSegments.get(x).segmentId ==
                                                        probableRoute.get(probableRoute.size()-2).segmentId)
                                                {
                                                    if (distancePrevious < probableSegments.get(x).distance_offset)
                                                    {
                                                        distancePrevious = probableSegments.get(x).distance_offset;
                                                    }
                                                    break;
                                                }
                                            }
                                            double distanceNext = secondInPair.distance_offset;
                                            for (int x = indicesOfDistinctPairs.get(i)+1;
                                                 x > indicesOfDistinctPairs.get(i+1); x++)
                                            {
                                                if (probableSegments.get(x).segmentId == secondInPair.segmentId)
                                                {
                                                    if (distanceNext > probableSegments.get(x).distance_offset)
                                                    {
                                                        distanceNext = probableSegments.get(x).distance_offset;
                                                    }
                                                    break;
                                                }
                                            }
                                            // if the length of the dubious segment is not more than twice the distance between
                                            // known points, this segment has to be added to the one more time (to show movement
                                            // in both directions)
                                            if (firstInPair.cost < (distanceNext - distancePrevious))
                                            {
                                                route.roadSegments.add(0, new SegmentPairedWithPoint(firstInPair));
                                                route.roadSegments.get(0).activityInDigitizationDirection =
                                                        !firstInPair.activityInDigitizationDirection;
                                            }
                                            else // this is a faulty segment that has to be removed
                                            {
                                                probableRoute.remove(probableRoute.size() - 1);
                                                System.out.println("Probable route: " + probableRoute.size() + " segments");
                                            }
                                        }
                                    }
                            // if number of elements in current sub-route or previously restored route may be not sufficient
                                    catch (Exception e)
                                    {
                                        System.out.println(e.getMessage());
                                    }

                                    findCandidateStatistics(route, indicesOfDistinctPairs.get(i-1),
                                            indicesOfDistinctPairs.get(i), probableSegments);
                                    determineDirectionsOfSegments(probableRoute.get(probableRoute.size() - 1),
                                                route.roadSegments.get(0));
                                    if (route.roadSegments.size() > 1)
                                    {
                                        determineDirectionsOfSegments(
                                                route.roadSegments.get(route.roadSegments.size() - 1), secondInPair);
                                    }
                                    for (SegmentPairedWithPoint s: route.roadSegments)
                                    {
                                        probableRoute.add(s);
                                        System.out.println("Probable route: " + probableRoute.size() + " segments");
                                    }
                                }

                            }
                        }
                    }
                    probableRoute.add(secondInPair);
                }

                if (probableRoute.get(0).dt == null) {
                    probableRoute.get(0).dt = currentWorkout.getStartAt();
                }

                if (probableRoute.get(probableRoute.size() - 1).dt == null) {
                    long end = currentWorkout.getStartAt().getMillis() + (long) (currentWorkout.getDuration() * 1000);
                    probableRoute.get(probableRoute.size() - 1).dt = new DateTime(end, currentWorkout.getStartAt().getZone());
                }

                if (probableRoute.get(probableRoute.size() - 1).distance_offset == 0) {
                    probableRoute.get(probableRoute.size() - 1).distance_offset = currentWorkout.getDistance();
                }
            }
            int leftKnownIndex;
            int rightKnownIndex;

            int i = 1;

            while (i < probableRoute.size())
            {
                if (probableRoute.get(i).segmentId == probableRoute.get(i-1).segmentId)
                {
                    if (probableRoute.get(i).activityInDigitizationDirection ==
                            probableRoute.get(i-1).activityInDigitizationDirection)
                    {
                        probableRoute.remove(i);
                        i--;
                    }
                    else
                    {
                       deadEndCount++;
                    }
                }
                i++;
            }

            i = 1;


            while (i < probableRoute.size())
            {
                leftKnownIndex = i - 1;
                rightKnownIndex = i + 1;
                if (rightKnownIndex < probableRoute.size())
                {
                    if (determineDirectionsOfSegments(probableRoute.get(leftKnownIndex), probableRoute.get(rightKnownIndex))
                        != DirectionsInPair.NOT_ADJ)
                    {
                        double distancePrevious = probableRoute.get(leftKnownIndex).distance_offset;
                        double distanceNext = probableRoute.get(rightKnownIndex).distance_offset;
                        if (probableRoute.get(i).cost * 2.5 < (distanceNext - distancePrevious))
                        {
                            probableRoute.add(i+1, new SegmentPairedWithPoint(probableRoute.get(i)));
                            i++;
                            //don't change digitalization direction as it will be done at the end
                        }
                        else // this is a faulty segment that has to be removed
                        {
                            probableRoute.remove(i);
                            System.out.println("Probable route: " + probableRoute.size() + " segments");
                            i--;
                        }
                    }
                }
                i++;
            }

            i = 1;

            while (i < probableRoute.size())
            {
                if (probableRoute.get(i).dt == null)
                {
                    leftKnownIndex = i - 1;
                    rightKnownIndex = i + 1;
                    while (rightKnownIndex < probableRoute.size())
                    {
                        if (probableRoute.get(rightKnownIndex).dt != null)
                        {
                            DateTime dt = calculateMeanDateTime(probableRoute.get(leftKnownIndex),
                                    probableRoute.get(rightKnownIndex));
                            double dist = calculateMeanDistance
                                    (probableRoute.get(leftKnownIndex), probableRoute.get(rightKnownIndex));
                            double dur = calculateMeanDuration(probableRoute.get(leftKnownIndex),
                                    probableRoute.get(rightKnownIndex));
                            for (int m = i; m < rightKnownIndex; m++)
                            {
                                probableRoute.get(m).pointId = -1;
                                probableRoute.get(m).dt = dt;
                                probableRoute.get(m).distance_offset = dist;
                                probableRoute.get(m).duration_offset = dur;
                            }
                            i = rightKnownIndex + 1;
                            break;
                        }
                        rightKnownIndex++;
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
                    break;
                }
            }

            if (probableRoute.size() > 10)
            {
                determineLoops(probableRoute, workoutRoute);
            }
            else
            {
                workoutRoute.roadSegments = probableRoute;
            }
            workoutRoute.firstDistinct = firstProbableDistinct;
            workoutRoute.lastDistinct = lastProbableDistinct;
            workoutRoute.deadEnds = deadEndCount;
        }
        return  workoutRoute;
    }

    static void determineLoops(List<SegmentPairedWithPoint> probableRoute, Route resulting)
    {
        System.out.println("Determining loops");
        // determine loops
        if (probableRoute.size() > 0 && resulting != null)
        {
            int loopCount = 0;
            LinkedList<SegmentPairedWithPoint> resultingRoute = new LinkedList<>();
            resultingRoute.add(probableRoute.get(0));
            if (probableRoute.size() > 1) {
                resultingRoute.add(probableRoute.get(1));
                int index = 2;
                int loopSegmentCount,
                        duplicateIndex,
                        lowerLimit = 0;
                while (index < probableRoute.size()) {
                    //from last segment that must stay in the result to the end of the current resulting route
                    for (int j = lowerLimit; j < resultingRoute.size(); j++) {
                        // compare with the next segment in probable route
                        if (probableRoute.get(index).segmentId == resultingRoute.get(j).segmentId &&
                                probableRoute.get(index).activityInDigitizationDirection ==
                                        resultingRoute.get(j).activityInDigitizationDirection) {
                            duplicateIndex = j;
                            index++;
                            int m = 1;
                            while (index + m < probableRoute.size() && duplicateIndex + m < resultingRoute.size()) {
                                if (probableRoute.get(index + m).segmentId ==
                                        resultingRoute.get(duplicateIndex + m).segmentId) {
                                    duplicateIndex++;
                                    index++;
                                    m++;
                                } else {
                                    break; //loop closed, last repeated segment found
                                }
                            }
                            loopSegmentCount = resultingRoute.size() - duplicateIndex;
                            if (loopSegmentCount < 5) // usually false loops contain 3-4 segments
                            {
                                for (int n = 1; n < loopSegmentCount; n++) // delete lsc - 1 last segments
                                {
                                    resultingRoute.remove(resultingRoute.size() - 1);
                                    System.out.println("Resulting route: " + resultingRoute.size() + " segments");
                                }
                            } else {
                                loopCount++;
                            }
                            lowerLimit = resultingRoute.size() - 1;
                        }
                    }
                    if (index < probableRoute.size()) {
                        resultingRoute.add(probableRoute.get(index));
                    }
                    if (resultingRoute.size() - lowerLimit > 10) // search inly within 10 adjacent consequent segments
                    {
                        lowerLimit++;
                    }
                    index++;

                }
                System.out.println("Loop count: " + loopCount);
                resulting.loops = loopCount;
                resulting.roadSegments = resultingRoute;
            }
        }
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

    static void insertRouteIntoDb(Route probableRoute)
    {
        if (probableRoute != null)
        {
            PreparedStatement statement = null;

            try
            {
                statement = connection.prepareStatement(INSERT_PROBABLE_ROUTE);
                statement.setInt(1, probableRoute.id);
                statement.setInt(2, probableRoute.deadEnds);
                statement.setInt(3, probableRoute.loops);
                statement.setBoolean(4, probableRoute.dubious);
                if (probableRoute.roadSegments != null) {
                    statement.setInt(5, probableRoute.roadSegments.size());
                }
                else
                {
                    statement.setInt(5, 0);
                }
                statement.executeUpdate();
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
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    static void insertRouteSegmentsIntoDb(Route probableRoute, int athleteId)
    {
        if (probableRoute != null && probableRoute.roadSegments != null) {
            PreparedStatement statement = null;

            try
            {
                connection.setAutoCommit(false);
                int i = 0;
                int insertedCount = 0;
                for (SegmentPairedWithPoint s: probableRoute.roadSegments)
                {
                    String query = String.format(INSERT_ROUTE_SEGMENTS, s.dt.toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
                    statement = connection.prepareStatement(query);
                    statement.setInt(1, athleteId);
                    statement.setInt(2, probableRoute.id);
                    try {
                        statement.setBoolean(3, s.activityInDigitizationDirection);
                    } catch (Exception e) {
                        statement.setNull(3, Types.BOOLEAN);
                    }
                    statement.setInt(4, i);
                    statement.setInt(5, s.pointId);
                    statement.setInt(6, s.segmentId);
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
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    private static SegmentPairedWithPoint getClosestSegment(int pointId)
    {
        SegmentPairedWithPoint segment = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            statement = connection.prepareStatement(GET_CLOSEST_SEGMENT);
            statement.setInt(1, pointId);
            resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                segment = new SegmentPairedWithPoint();
                segment.segmentId = resultSet.getInt("id");
                segment.source = resultSet.getInt("source");
                segment.target = resultSet.getInt("target");
                segment.cost = resultSet.getDouble("cost");
                segment.pointId = pointId;
                String time =
                        resultSet.getTimestamp("dt").toString().substring(0,20) + "000"
                                + currentWorkout.getTimeZone().getID();
                segment.dt = FORMATTER.parseDateTime(time);
            }
        }
        catch (Exception e)
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
                    System.out.println(e.getMessage());
                }
            }
            if (resultSet != null)
            {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }


        return segment;
    }


    private static Route calculateRoute(SegmentPairedWithPoint first, SegmentPairedWithPoint second)
    {
        Route route = null;
        ArrayList<SegmentPairedWithPoint> segments = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try
        {
            statement = connection.prepareStatement(CALC_ROUTE);
            statement.setInt(1, first.target);
            statement.setInt(2, second.source);
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
            if (segments.size() > 0)
            {
                if (segments.get(segments.size() - 1).segmentId == second.segmentId)
                {
                    segments.remove(segments.size() - 1);
                }
            }
            if (segments.size()>1)
            {
                for (int i = 1; i < segments.size(); i++)
                {
                    determineDirectionsOfSegments(segments.get(i-1), segments.get(i));
                }
            }


        }
        catch (Exception e)
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
                    System.out.println(e.getMessage());
                }
            }
            if (resultSet != null)
            {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
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

    private static double calculateMeanDistance(SegmentPairedWithPoint left,
                                                SegmentPairedWithPoint right)
    {
        return left.distance_offset + (right.distance_offset - left.distance_offset) / 2;
    }

    private static double calculateMeanDuration(SegmentPairedWithPoint left,
                                                    SegmentPairedWithPoint right)
    {
        return left.duration_offset + (right.duration_offset - left.duration_offset) / 2;
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























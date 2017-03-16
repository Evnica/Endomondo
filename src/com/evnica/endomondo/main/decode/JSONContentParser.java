package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.model.*;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class: JSONContentParser
 * Version: 0.1
 * Created on 23.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class JSONContentParser
{
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(JSONContentParser.class.getName());


    public static WorkoutJSON parseWorkoutUrl( String jsonContent, int workoutId, TargetGeometry targetGeometry)
    {
        WorkoutJSON result = null;

        try
        {
            JSONObject workoutObject = new JSONObject( jsonContent );
            String date = workoutObject.getString( "local_start_time" );
            date = date.substring( 0, 19 );
            DateTime offset = formatter.parseDateTime( date );
            int userId;
            try {
                userId = workoutObject.getJSONObject("author").getInt("id");
            } catch (JSONException e) {
                System.out.println("No user id for workout " + workoutId);
                userId = -1;
            }

            if (targetGeometry.equals( TargetGeometry.LAPS ) || targetGeometry.equals( TargetGeometry.BOTH ))
            {
                // make sure to retrieve points too
                if (targetGeometry.equals( TargetGeometry.BOTH )) targetGeometry = TargetGeometry.POINTS;

                try
                {   // try to get metric laps; "laps" are not always present in workout JSON
                    JSONArray metricLaps = workoutObject.getJSONObject( "laps" ).getJSONArray( "metric" );
                    if (metricLaps.length() > 0)
                    {
                        result = new WorkoutJSON();
                        result.setUserId(userId);
                        // polyline is not always present in JSON; without polyline we rely on points in {catch}
                        ((JSONObject) metricLaps.get(0)).getString( "small_encoded_polyline");
                        // no exception - polylines present in laps
                        double beginLatitude, beginLongitude, endLatitude, endLongitude;
                        int duration;
                        for (int i = 0; i < metricLaps.length(); i++)
                        {
                            String lapGeometryEncoded = ((JSONObject) metricLaps.get(i))
                                    .getString( "small_encoded_polyline");
                            Polyline lapGeometry = GooglePolylineDecoder.decode( lapGeometryEncoded );
                            beginLatitude = ((JSONObject) metricLaps.get(i)).getDouble("begin_latitude");
                            beginLongitude = ((JSONObject) metricLaps.get(i)).getDouble("begin_longitude");
                            endLatitude = ((JSONObject) metricLaps.get(i)).getDouble("end_latitude");
                            endLongitude = ((JSONObject) metricLaps.get(i)).getDouble("end_longitude");

                            Lap lap = new Lap( beginLatitude, beginLongitude, endLatitude, endLongitude );
                            lap.setSmallPolyline( lapGeometry );
                            lap.setWorkoutId( workoutId );
                            lap.setId( i );
                            lap.setOffset( offset );

                            try
                            {
                                duration =  ((JSONObject) metricLaps.get(i)).getInt("duration");
                                lap.setDuration( duration );
                                offset = offset.plusMillis( duration );
                                // add a lap only if time is known; geometry without time of no interest?
                                result.addLap( lap );
                            }
                            catch ( Exception e )
                            {
                                // have no idea why I have catch here... if you know, drop me a line
                                e.printStackTrace();
                            }
                        }
                        int diff = metricLaps.length() - result.getLaps().size();
                        if (diff != 0)
                        {
                            System.out.println(workoutId + " workout: " + diff + " laps lost");
                        }

                    }
                }
                catch ( Exception e ) // expecting JSONException, but theoretically one can also occur during polyline decoding
                {
                    // retrieve point geometry if laps failed
                    targetGeometry = TargetGeometry.POINTS;
                }
            }

            if ( targetGeometry.equals( TargetGeometry.POINTS  ) )
            {
                try
                {
                    JSONArray pointsJSONArray = workoutObject.getJSONObject( "points" ).getJSONArray( "points" );
                    if (pointsJSONArray.length() > 0)
                    {
                        if (result == null)
                        {
                            result = new WorkoutJSON();
                            result.setUserId(userId);
                        }
                        // sometimes points have no coordinates
                        pointsJSONArray.getJSONObject( 0 ).getDouble( "latitude" );
                        // if no exception occurred
                        double lat, lon, distance;
                        JSONObject pointJSONObject;
                        String timestampString;

                        for ( int i = 0; i < pointsJSONArray.length(); i++ )
                        {
                            pointJSONObject = pointsJSONArray.getJSONObject( i );
                            lat = pointJSONObject.getDouble( "latitude" );
                            lon = pointJSONObject.getDouble( "longitude" );
                            distance = pointJSONObject.getDouble( "distance" );

                            timestampString = pointJSONObject.getString( "time" );
                            try
                            {
                                timestampString = timestampString.substring( 0, 19 );
                                DateTime timeCaptured = formatter.parseDateTime( timestampString );
                                Point point = new Point( lat, lon );
                                point.setTimeCaptured( timeCaptured );
                                point.setDistanceFromPrevious( distance );
                                point.setOrder(i);
                                result.addPoint( point );
                            }
                            catch ( Exception ex )
                            {
                                System.out.println(workoutId + " workout: time in point ("
                                        + lat + ", " + lon + ") not parsed");
                            }
                        }
                        if (result.getPoints().size() > 0)
                        {
                            int diff = pointsJSONArray.length() - result.getPoints().size();
                            if (diff != 0)
                            {
                                System.out.println(workoutId + " workout: " + diff + " points lost");
                            }
                        }
                    }
                    else
                    {
                        System.out.println(workoutId + " workout: no points");
                    }
                }
                catch ( Exception e )
                {
                    System.out.println(workoutId + " workout: no point coordinates");
                }
            }
        }
        catch ( JSONException e )
        {
            System.out.println("Non-JSON content: " + e);
        }

        return result;
    }

    public static WorkoutDetail parseWorkoutDetailUrl( String jsonContent, int workoutId)
    {
        WorkoutDetail workoutDetail = new WorkoutDetail();
        boolean validPoints = false,
                validLaps = false,
                pointsAbsent = false,
                lapsAbsent = false;
        workoutDetail.setId(workoutId);
        DateTime offset = null;
        try
        {
            JSONObject workoutObject = new JSONObject( jsonContent );
            try
            {
                String date = workoutObject.getString( "local_start_time" );
                date = date.substring( 0, 19 );
                offset = formatter.parseDateTime( date );
                workoutDetail.setStartAt(offset);
            } catch (Exception e) {
                LOGGER.error("Can't get offset for id " + workoutId, e);
                System.out.println("Can't get offset for id " + workoutId + ", " + e);
            }
            try {
                workoutDetail.setDistance(workoutObject.getDouble("distance"));
            } catch (JSONException e) {
                LOGGER.error("Can't get distance for id " + workoutId);
                System.out.println("Can't get distance for id " + workoutId);
            }

            try {
                workoutDetail.setDuration(workoutObject.getDouble("duration"));
            } catch (JSONException e) {
                LOGGER.error("Can't get duration for id " + workoutId);
                System.out.println("Can't get duration for id " + workoutId);
            }
            try {
                workoutDetail.setWeather(workoutObject.getJSONObject("weather").getInt("type"));
            } catch (JSONException e) {
                workoutDetail.setWeather(-1);
                LOGGER.error("No weather, workout is likely to be empty: " + workoutId);
                System.out.print("workout is likely to be empty, ");
            }
            try {
                workoutDetail.setUserId(workoutObject.getJSONObject("author").getInt("id"));
            } catch (JSONException e) {
                workoutDetail.setUserId(-1);
                LOGGER.error("Can't get user id for workout id " + workoutId);
                System.out.print("no user: workout is likely to be useless, ");
            }
            try {
                workoutDetail.setShowMap(workoutObject.getInt("show_map"));
            } catch (JSONException e) {
                LOGGER.error("Can't get show_map for id " + workoutId);
                System.out.println("has no show_map, ");
                workoutDetail.setShowMap(-1);
            }
            // try to get laps if present
            try
            {
                JSONArray metricLaps = workoutObject.getJSONObject( "laps" ).getJSONArray( "metric" );
                if (metricLaps.length() > 0)
                {
                    double beginLatitude, beginLongitude, endLatitude, endLongitude;
                    long duration;
                    // polyline is not always present in JSON; without polyline we rely on points
                    try {
                        ((JSONObject) metricLaps.get(0)).getString( "small_encoded_polyline");
                        validLaps = true;
                    } catch (JSONException e) {
                        validLaps = false;
                    }
                    for (int i = 0; i < metricLaps.length(); i++)
                    {
                        try
                        {
                            beginLatitude = ((JSONObject) metricLaps.get(i)).getDouble("begin_latitude");
                            beginLongitude = ((JSONObject) metricLaps.get(i)).getDouble("begin_longitude");
                            endLatitude = ((JSONObject) metricLaps.get(i)).getDouble("end_latitude");
                            endLongitude = ((JSONObject) metricLaps.get(i)).getDouble("end_longitude");
                            Lap lap = new Lap( beginLatitude, beginLongitude, endLatitude, endLongitude );

                            if (validLaps)
                            {
                                try {
                                    String lapGeometryEncoded = ((JSONObject) metricLaps.get(i))
                                            .getString( "small_encoded_polyline");
                                    Polyline lapGeometry = GooglePolylineDecoder.decode( lapGeometryEncoded );
                                    lap.setSmallPolyline( lapGeometry );
                                } catch (JSONException e) {
                                    LOGGER.error("Lap " + i + " in workout " + workoutId + " has no geometry");
                                    System.out.println("Lap " + i + " in workout " + workoutId + " has no geometry");
                                }
                            }
                            lap.setWorkoutId( workoutId );
                            lap.setId( i );
                            if (offset != null) {
                                lap.setOffset( offset );
                            }

                            try
                            {
                                duration =  ((JSONObject) metricLaps.get(i)).getLong("duration");
                                lap.setDuration( duration );
                                if (offset != null) {
                                    offset = offset.plusMillis( (int) duration );
                                }
                            }
                            catch ( Exception e )
                            {
                                lap.setDuration(-1L);
                            }
                            workoutDetail.addLap( lap );
                        } catch (JSONException e) {
                            LOGGER.error("Invalid lap " + i + ", workout " + workoutId);
                        }
                    }
                    int diff = metricLaps.length() - workoutDetail.getLaps().size();
                    if (diff != 0)
                    {
                        System.out.println(workoutId + " workout: " + diff + " laps invalid");
                        LOGGER.info(workoutId + " workout: " + diff + " laps invalid");
                    }
                }
            } catch (JSONException e) {
                lapsAbsent = true;
            }
            try
            {
                JSONArray pointsJSONArray = workoutObject.getJSONObject( "points" ).getJSONArray( "points" );
                if (pointsJSONArray.length() > 1) // 1 point is not a lap
                {
                    try
                    {
                        int startIndex = 0;
                        try // test if first point contains coordinates
                        {
                            pointsJSONArray.getJSONObject( 0 ).getDouble( "latitude" );
                            validPoints = true;
                        }
                        // test if second point contains coordinates; if not - points are invalid
                        catch (JSONException e)
                        {
                            pointsJSONArray.getJSONObject( 1 ).getDouble( "latitude" );
                            validPoints = true;
                            startIndex = 1;
                        }

                        double lat, lon, distance;
                        int duration;
                        JSONObject pointJSONObject;
                        String timestampString;

                        Point knownValidPoint = null;
                        boolean validPointFound = false;
                        do {
                            try
                            {
                                pointJSONObject = pointsJSONArray.getJSONObject( startIndex );
                                knownValidPoint = new Point( pointJSONObject.getDouble( "latitude" ),
                                        pointJSONObject.getDouble( "longitude" ) );
                                knownValidPoint.setOrder(startIndex);
                                timestampString = pointJSONObject.getString( "time" );
                                timestampString = timestampString.substring( 0, 19 );
                                DateTime timeCaptured = formatter.parseDateTime( timestampString );
                                knownValidPoint.setTimeCaptured( timeCaptured );
                                knownValidPoint.setDistanceFromPrevious( pointJSONObject.getDouble( "distance" ) );
                                knownValidPoint.setDurationFromPrevious(pointJSONObject.getInt( "duration" ));
                                knownValidPoint.setDistanceFromOffset(knownValidPoint.getDistanceFromPrevious());
                                knownValidPoint.setDurationFromOffset(knownValidPoint.getDurationFromPrevious());
                                workoutDetail.addPoint(knownValidPoint);
                                validPointFound = true;
                            } catch (JSONException e) {
                                LOGGER.error("Invalid point " + startIndex + ", workout " + workoutId);
                            }
                            startIndex++;
                        }
                        while (!validPointFound && startIndex < pointsJSONArray.length());
                        if (startIndex >= pointsJSONArray.length()) validPoints = false;
                        if (knownValidPoint != null) {
                            for ( int i = startIndex; i < pointsJSONArray.length(); i++ )
                            {
                                pointJSONObject = pointsJSONArray.getJSONObject( i );
                                try
                                {
                                    lat = pointJSONObject.getDouble( "latitude" );
                                    lon = pointJSONObject.getDouble( "longitude" );
                                    distance = pointJSONObject.getDouble( "distance" );
                                    duration = pointJSONObject.getInt( "duration" );
                                    timestampString = pointJSONObject.getString( "time" );
                                    timestampString = timestampString.substring( 0, 19 );
                                    DateTime timeCaptured = formatter.parseDateTime( timestampString );
                                    Point point = new Point( lat, lon );
                                    point.setOrder(i);
                                    point.setTimeCaptured( timeCaptured );
                                    point.setDistanceFromOffset(distance);
                                    point.setDurationFromOffset(duration);
                                    point.setDistanceFromPrevious(distance - knownValidPoint.getDistanceFromOffset());
                                    point.setDurationFromPrevious(duration - knownValidPoint.getDurationFromOffset());
                                    workoutDetail.addPoint( point );
                                    knownValidPoint = point;
                                } catch (JSONException e) {
                                    LOGGER.error("Invalid point " + i + ", workout " + workoutId);
                                }
                            }
                            if (workoutDetail.getPoints().size() > 1)
                            {
                                int diff = pointsJSONArray.length() - workoutDetail.getPoints().size();
                                if (diff != 0)
                                {
                                    System.out.println(workoutId + " workout: " + diff + " points invalid");
                                    LOGGER.info(workoutId + " workout: " + diff + " points invalid");
                                }
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        validPoints = false;
                    }
                }
            } catch (JSONException e) {
                pointsAbsent = true;
            }
            if (pointsAbsent)
            {
                if (lapsAbsent) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.APOINTS_ALAPS);
                else if (validLaps) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.APOINTS_VLAPS);
                else workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.APOINTS_ILAPS);
            }
            else if (validPoints)
            {
                if (lapsAbsent) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.VPOINTS_ALAPS);
                else if (validLaps) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.VPOINTS_VLAPS);
                else workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.VPOINTS_ILAPS);
            }
            else
            {
                if (lapsAbsent) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.IPOINTS_ALAPS);
                else if (validLaps) workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.IPOINTS_VLAPS);
                else workoutDetail.setWorkoutGeometryType(WorkoutGeometryType.IPOINTS_ILAPS);
            }
        } catch (JSONException e) {
            LOGGER.error(workoutId + " is not parsable");
        }

        return workoutDetail;
    }


    public static User parseUser( String urlContent)
    {
        User user = null;
        JSONObject userObject = new JSONObject( urlContent );
        try
        {
            int id = userObject.getInt( "id" );
            int gender = userObject.getInt( "gender" );
            if (userObject.getInt( "workout_count" ) > 0)
            {
                user = new User();
                JSONArray summaryBySport = userObject.getJSONArray( "summary_by_sport" );
                JSONObject individualSummary;
                int sportId;
                for (int i = 0; i < summaryBySport.length(); i++)
                {
                    individualSummary = summaryBySport.getJSONObject( i );
                    sportId = individualSummary.getInt( "sport" );
                    if (sportId == 1)
                        user.setCyclingTransportCount( individualSummary.getInt( "count" ) );
                    else if (sportId == 2)
                        user.setCyclingSportCount( individualSummary.getInt( "count" ) );
                    else if (sportId == 3)
                        user.setMountainBikingCount( individualSummary.getInt( "count" ) );
                }
                if (user.getCyclingSportCount() > 0 || user.getMountainBikingCount() > 0 ||
                        user.getCyclingTransportCount() > 0)
                {
                    user.setId( id );
                    user.setGender( gender );
                    String date = userObject.getString( "created_date" );
                    date = date.substring( 0, 19 );
                    user.setDateCreated( formatter.parseDateTime( date ) );
                }
                else
                {
                    user = null;
                }
            }

        }
        catch ( JSONException e )
        {
            System.out.println("Except: " + e);
        }
        return user;
    }
}

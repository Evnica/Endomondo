package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.model.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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



    public static WorkoutJSON parseWorkoutUrl( String jsonContent, Workout workout, TargetGeometry targetGeometry)
    {
        WorkoutJSON result = null;
        List<Lap> laps;

        try
        {
            JSONObject workoutObject = new JSONObject( jsonContent );
            DateTime offset = workout.getLocalStartTime();
            if (targetGeometry.equals( TargetGeometry.LAPS ) || targetGeometry.equals( TargetGeometry.BOTH ))
            {
                // make sure to retrieve points too
                if (targetGeometry.equals( TargetGeometry.BOTH )) targetGeometry = TargetGeometry.POINTS;

                try
                {   // try to get metric laps; "laps" are not always present in workout JSON
                    JSONArray metricLaps = workoutObject.getJSONObject( "laps" ).getJSONArray( "metric" );
                    if (metricLaps.length() > 0)
                    {
                        // polyline is not always present in JSON; without polyline we rely on points in {catch}
                        ((JSONObject) metricLaps.get(0)).getString( "small_encoded_polyline");
                        // no exception - polylines present in laps
                        laps = new ArrayList<>();
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
                            lap.setWorkoutId( workout.getId() );
                            lap.setId( i );
                            lap.setOffset( offset );

                            try
                            {
                                duration =  ((JSONObject) metricLaps.get(i)).getInt("duration");
                                lap.setDuration( duration );
                                offset = offset.plusMillis( duration );
                                // add a lap only if time is known; geometry without time of no interest?
                                laps.add( lap );
                            }
                            catch ( Exception e )
                            {
                                // have no idea why I have catch here... if you know, drop me a line
                                e.printStackTrace();
                            }
                        }
                        int diff = metricLaps.length() - laps.size();
                        if (diff != 0)
                        {
                            System.out.println(workout.getId() + " workout: " + diff + " laps lost");
                        }
                        result = new WorkoutJSON( laps );
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
                        // sometimes points have no coordinates
                        pointsJSONArray.getJSONObject( 0 ).getDouble( "latitude" );
                        // if no exception occurred
                        List<Point> points = new ArrayList<>(  );
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
                                point.setDistance( distance );
                                points.add( point );
                            }
                            catch ( Exception ex )
                            {
                                System.out.println(workout.getId() + " workout: time in point ("
                                        + lat + ", " + lon + ") not parsed");
                            }
                        }
                        if (points.size() > 0)
                        {
                            if (result == null) result = new WorkoutJSON( null );
                            result.setPoints( points );
                            int diff = pointsJSONArray.length() - points.size();
                            if (diff != 0)
                            {
                                System.out.println(workout.getId() + " workout: " + diff + " points lost");
                            }
                        }
                    }
                    else
                    {
                        System.out.println(workout.getId() + " workout: no points");
                    }
                }
                catch ( Exception e )
                {
                    System.out.println(workout.getId() + " workout: no point coordinates");
                }
            }

            if (result != null)
            {
                try
                {
                    result.setUserGender(workoutObject.getJSONObject( "author" ).getInt( "gender" ));
                    result.setId( workout.getId() );
                } catch ( JSONException e )
                {
                    System.out.println("Gender not set: " + e);
                }
            }
        }
        catch ( JSONException e )
        {
            System.out.println("Non-JSON content: " + e);
        }

        return result;
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

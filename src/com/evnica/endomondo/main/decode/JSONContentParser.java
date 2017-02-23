package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.model.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

    public static WorkoutJSON parseWorkoutUrl( String jsonContent, Workout workout)
    {
        WorkoutJSON result = null;
        List<Lap> laps = null;
        try
        {
            JSONObject workoutObject = new JSONObject( jsonContent );
            try
            {
                // try to get metric laps; "laps" are not always present in workout JSON
                JSONArray metricLaps = workoutObject.getJSONObject( "laps" ).getJSONArray( "metric" );
                if (metricLaps.length() > 0)
                {
                    try
                    {
                        // polyline is not always present in JSON
                        ((JSONObject) metricLaps.get(0)).getString( "small_encoded_polyline");

                        double beginLatitude, beginLongitude, endLatitude, endLongitude;
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
                            lap.setId( workout.getId()*1000 + i );
                        }


                    }
                    catch ( JSONException e )
                    {
                        e.printStackTrace();
                    }

                }

            } catch ( JSONException e )
            {
                e.printStackTrace();
            }
        } catch ( JSONException e )
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
            System.out.println(e);
        }
        return user;
    }
}

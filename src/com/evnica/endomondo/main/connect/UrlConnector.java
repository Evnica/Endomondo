package com.evnica.endomondo.main.connect;

import com.evnica.endomondo.main.model.User;
import com.evnica.endomondo.main.model.Workout;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalInstantException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class: UrlConnector
 * Version: 0.1
 * Created on 20.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class UrlConnector
{
    private static String url;
    private static final String USER_REST_URL = "https://www.endomondo.com/rest/v1/users/";
    private static final String START_DATE = "2016-01-01T00%3A00%3A00.000Z";
    private static final String END_DATE = "2016-04-01T00%3A00%3A00.000";
    private static String userRestIntervalUrl =
            "https://www.endomondo.com/rest/v1/users/%s/workouts?before=%s&after=%s";
    private static String workoutUrl = "https://www.endomondo.com/rest/v1/users/%s/workouts/%s";
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static void setUrl( String url )
    {
        UrlConnector.url = url;
    }

    public static void setUrlUser( int id )
    {
        url = USER_REST_URL + id;
    }

    public static String getUserUrlContent(int id) throws IOException
    {
        setUrlUser( id );
        return getUrlContent();
    }

    public static String getUrlContent() throws IOException
    {
        return getUrlContent( url );
    }

    private static String getUrlContent(String url) throws IOException
    {
        InputStream inputStream = new URL(url).openStream();
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
    }

    public static String getWorkoutsUrlContent(int userId) throws IOException
    {
        InputStream inputStream = new URL(String.format(userRestIntervalUrl, userId, END_DATE, START_DATE)).openStream();
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next(); //ISO8859-1
    }

    public static List<Workout> parseWorkoutsInInterval(int userId, String urlContent)
    {
        List<Workout> workouts;
        try
        {
            JSONArray workoutsArray = new JSONArray( urlContent );
            workouts = new ArrayList<>();
            Workout workout;
            JSONObject workoutObject;
            int workoutId;
            int sport;
            String localStartTime;

            for (int i = 0; i < workoutsArray.length(); i++)
            {
                workoutObject = workoutsArray.getJSONObject( i );
                try
                {
                    sport = workoutObject.getInt( "sport" );
                    if (sport == 3 || sport == 1 || sport == 2)
                    {
                        workoutId = workoutObject.getInt( "id" );
                        localStartTime = workoutObject.getString( "local_start_time" );
                        localStartTime = localStartTime.substring( 0, 19 );
                        DateTime workoutStart;
                        try
                        {
                            workoutStart = formatter.parseDateTime( localStartTime );
                        }
                        catch ( IllegalInstantException e )
                        {
                            workoutStart = formatter.withZone( DateTimeZone.UTC).parseDateTime(localStartTime);
                        }
                        workout = new Workout( workoutId, sport, workoutStart, userId );
                    }
                    else
                    {
                        workout = null;
                    }
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                    workout = null;
                }
                if (workout != null)
                {
                    workouts.add( workout );
                }
            }
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
            workouts = null;
        }


        return workouts;
    }


    public static String getWorkoutJsonUrlContent(Workout workout) throws IOException
    {
        setUrl( String.format( workoutUrl, workout.getUserId(), workout.getId() ) );
        return getUrlContent();

    }

}

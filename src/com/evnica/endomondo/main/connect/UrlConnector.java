package com.evnica.endomondo.main.connect;

import com.evnica.endomondo.main.model.User;
import com.evnica.endomondo.main.model.Workout;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    public static String getUrl()
    {
        return url;
    }

    public static void setUrl( String url )
    {
        UrlConnector.url = url;
    }

    public static void setUrlUser( int id )
    {
        url = USER_REST_URL + id;
    }

    public static String getUrlContent() throws IOException
    {
        InputStream inputStream = new URL(url).openStream();
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next(); //ISO8859-1
    }

    public static String getWorkoutsUrlContent(int userId) throws IOException
    {
        InputStream inputStream = new URL(String.format(userRestIntervalUrl, userId, END_DATE, START_DATE)).openStream();
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next(); //ISO8859-1
    }

    public static User parseUser(String urlContent)
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
                        workout = new Workout( workoutId, sport, formatter.parseDateTime( localStartTime ), userId );
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



}

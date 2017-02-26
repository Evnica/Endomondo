package com.evnica.endomondo.test;

import com.sun.deploy.net.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class: Demo
 * Version: 0.1
 * Created on 21.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class Demo
{
    private static int userId = 0, workoutId = 0;
    private static final String START_DATE = "2016-01-01T00%3A00%3A00.000Z";
    private static final String END_DATE = "2016-04-01T00%3A00%3A00.000";
    private static String userRestIntervalUrl =
            String.format("https://www.endomondo.com/rest/v1/users/%s/workouts?before=%s&after=%s", userId, END_DATE, START_DATE );
    private static String workoutUrl = String.format("https://www.endomondo.com/rest/v1/users/%s/workouts/%s", userId, workoutId);

    public static void main( String[] args )
    {
        /*SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );
        Date workoutDate = dateFormat.parse( "24.10.2015 10:01" );
        System.out.println(workoutDate.getTime()/1000);*/

        /*System.out.println(userRestIntervalUrl);
        System.out.println(workoutUrl);*/

        /*try
        {
            URL url = new URL( "https://www.endomondo.com/workouts/663525262" );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            int response = connection.getResponseCode();
            if (response == 302)
            {
                String redirected = connection.getHeaderField( "Location" );
                // replace "https://www.endomondo.com/users/#/workouts/#" with
                         // https://www.endomondo.com/rest/v1/users/#/workouts/#
                redirected = redirected.replace( "m/", "m/rest/v1/" );
                System.out.println(redirected);
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }*/



    }
}

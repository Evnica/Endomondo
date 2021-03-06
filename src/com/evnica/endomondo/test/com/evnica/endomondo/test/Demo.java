package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.AthleteRepository;
import com.evnica.endomondo.main.model.WorkoutRepository;
import com.sun.deploy.net.HttpResponse;
import com.sun.jmx.snmp.SnmpPduBulk;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public static void main( String[] args ) throws Exception
    {
        /*Map<String, Integer[]> logPointByRegion = new HashMap<>(); // region - [wrkt_count, pt_cnt]
        logPointByRegion.put("ar", new Integer[] {1, 100});
        for (Map.Entry<String, Integer[]> entry: logPointByRegion.entrySet())
        {
            System.out.println(entry.getKey() + ": " + entry.getValue()[0] + ", " + entry.getValue()[1]);
        }
        System.out.println("--------------");

        for (String region: new String[]{"ar", "br"})
        {
            if (logPointByRegion.containsKey(region))
            {
                Integer[] oldValues = logPointByRegion.get(region);
                oldValues[0] += 1;
                oldValues[1] += 200;
            }
            else
            {
                logPointByRegion.put(region, new Integer[]{1, 10});
            }
        }

        for (Map.Entry<String, Integer[]> entry: logPointByRegion.entrySet())
        {
            System.out.println(entry.getKey() + ": " + entry.getValue()[0] + ", " + entry.getValue()[1]);
        }*/

        /*int[] ids = {21942994, 25370429, 25429794, 25429928, 26334291, 26494291};
        DbConnector.connectToDb();
        AthleteRepository.setConnection(DbConnector.getConnection());
        for (int id: ids)
        {
            AthleteRepository.insertInvalidity(id);
        }
        DbConnector.closeConnection();
        System.out.println("Done");*/

       /* try
        {
            DbConnector.connectToDb();
            System.out.println("Connected");
            WorkoutRepository.setConnection(DbConnector.getConnection());
            WorkoutRepository.toCsv("interimTables/workout-user");
            System.out.println("Done");
            DbConnector.closeConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/



        /*SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm" );
        Date workoutDate = dateFormat.parse( "24.10.2015 10:01" );
        System.out.println(workoutDate.getStartAt()/1000);*/

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

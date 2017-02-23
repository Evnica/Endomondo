package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.UserRepository;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutRepository;
import org.joda.time.DateTime;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class: WorkoutTableFilling
 * Version: 0.1
 * Created on 22.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class WorkoutTableFilling
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println("Start: " + new DateTime( ));
        List<Integer> rejectedIds = new ArrayList<>();
        List<Workout> notInsertedWorkouts = new ArrayList<>();
        DbConnector.connectToDb();
        UserRepository userRepository = new UserRepository( DbConnector.getConnection() );
        Integer[] nums = {26410434, 26410437, 26410447, 26410451, 26410465, 26410480, 26410484, 26410491, 26410496, 26410497, 26410528, 26410530, 26410541, 26410567, 26410573, 26410576, 26410601, 26410603, 26410608, 26410622, 26410630, 26410632, 26410633, 26410644, 26410651, 26410654, 26410665, 26410671, 26410698, 26410705, 26410707, 26410717, 26410748, 26410770, 26410773, 26410783, 26410795, 26410797, 26410799, 26410804, 26410827, 26410832, 26410841, 26410842, 26410860, 26410869, 26410889, 26410890, 26410901, 26410909, 26410919, 26410921, 26410922, 26410923, 26410929, 26410952, 26410962, 26410971, 26410975, 26410981, 26410998, 26411001, 26411008, 26411022, 26411035, 26411036, 26411038, 26411049, 26411053, 26411062, 26411092, 26411100, 26411114, 26411116, 26411119, 26411121, 26411135, 26411140, 26411143, 26411148, 26411151, 26411154, 26411174, 26411184, 26411202, 26411206, 26411214, 26411220, 26411234, 26411254, 26411255, 26411262, 26411274, 26411279, 26411280, 26411288, 26411295, 26411297, 26411363, 26411365, 26411367, 26411374, 26411383, 26411386, 26411387, 26411401, 26411403, 26411434, 26411447, 26411448, 26411459, 26411461, 26411465, 26411467, 26411471, 26411483, 26411512, 26411532, 26411575, 26411579, 26411598, 26411608, 26411609, 26411623, 26411631, 26411637, 26411641, 26411642, 26411657, 26411671, 26411679, 26411687, 26411694, 26411706, 26411733, 26411735, 26411755, 26411781, 26411787, 26411790, 26411794, 26411803, 26411812, 26411819, 26411833, 26411834, 26411835, 26411849, 26411857, 26411878, 26411880, 26411902, 26411916, 26411924, 26411928, 26411934, 26411947, 26411961, 26411966, 26411985, 26412011, 26412017, 26412022, 26412043, 26412055, 26412085, 26412086, 26412125, 26412139, 26412141, 26412150, 26412172, 26412199, 26412218};
        List<Integer> userIds = Arrays.asList(nums); /*= new ArrayList<>(  ); // = userRepository.getUserIdsFromDB(); */
        //userIds.add( 26405811 );
        WorkoutRepository.setConnection( DbConnector.getConnection() );
        int i = 0;
        for (int id: userIds)
        {
            try
            {
                String workoutsUrlContent = UrlConnector.getWorkoutsUrlContent( id );
                List<Workout> workouts = UrlConnector.parseWorkoutsInInterval( id, workoutsUrlContent );
                if (workouts != null && workouts.size() > 0)
                {
                    for (Workout w: workouts)
                    {
                        try
                        {
                            WorkoutRepository.insert( w );
                        } catch ( SQLException e )
                        {
                            e.printStackTrace();
                            notInsertedWorkouts.add( w );
                        }
                    }
                }
            }
            catch ( IOException e )
            {
                if (e.getMessage().contains( "429" ))
                {
                    System.out.println("Rejected due to multiple requests on ID " + i );
                    rejectedIds.add( i );
                    Thread.sleep( 20000 );
                }
                else
                {
                    System.out.println(i + ": " + e);
                }
            }
            System.out.println("Processed "+ id);
            if (i%25 == 0)
            {
                Thread.sleep( 15000 );
            }

            i++;
        }

        i = 0;

        if (rejectedIds.size() > 0)
        {
            System.out.println("Processing rejected: ");
            for (int id: rejectedIds)
            {
                try
                {
                    String workoutsUrlContent = UrlConnector.getWorkoutsUrlContent( id );
                    List<Workout> workouts = UrlConnector.parseWorkoutsInInterval( id, workoutsUrlContent );
                    if (workouts != null && workouts.size() > 0)
                    {
                        for (Workout w: workouts)
                        {
                            WorkoutRepository.insert( w );
                        }
                    }
                }
                catch ( IOException e )
                {
                   System.out.println(i + ": " + e);
                }
                System.out.println("Processed "+ id);
                if (i%25 == 0)
                {
                    Thread.sleep( 15000 );
                }

                i++;
            }
        }

        if (notInsertedWorkouts.size() > 0)
        {
            System.out.println("Processing not inserted...");
            for (Workout w: notInsertedWorkouts)
            {
                try
                {
                    WorkoutRepository.insert( w );
                }
                catch ( SQLException e )
                {
                    System.out.println(w.getId() + " " + e);
                }
            }
        }
        System.out.println("End: " + new DateTime( ));
        DbConnector.closeConnection();
    }
}

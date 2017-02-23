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

       // List<Integer> userIds /*= new ArrayList<>(  );  = userRepository.getUserIdsFromDB(); */
        //userIds.add( 26405811 );
        WorkoutRepository.setConnection( DbConnector.getConnection() );
        //for (int id: userIds)
        for (int j = 26396186; j > 26300099; j--)
        {
            int id = j;
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
                    System.out.println("Rejected due to multiple requests on ID " + id );
                    rejectedIds.add( id );
                    Thread.sleep( 20000 );
                }
                else
                {
                    System.out.println(id + ": " + e);
                }
            }
            System.out.println("Processed "+ id);
            if (id%99 == 0)
            {
                Thread.sleep( 10000 );
            }
        }

        int i = 0;

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
                   System.out.println(id + ": " + e);
                }
                System.out.println("Processed "+ id);
                if (i%25 == 0)
                {
                    Thread.sleep( 10000 );
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

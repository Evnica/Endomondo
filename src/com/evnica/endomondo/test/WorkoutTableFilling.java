package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutRepository;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(WorkoutTableFilling.class.getName());

    public static void main( String[] args )
    {
        try
        {

            int invalidUserCount, rejectedIdCount, addedUserCount, addedWorkoutCount;
            int workoutsBefore = 195941;
            DateTime end;
            double duration;
            List<Integer> rejectedIds = new ArrayList<>();
            List<Workout> notInsertedWorkouts = new ArrayList<>();
            DbConnector.connectToDb();
            //UserRepository userRepository = new UserRepository( DbConnector.getConnection() );
            // List<Integer> userIds /*= new ArrayList<>(  );  = userRepository.getUserIdsFromDB(); */
            //userIds.add( 26405811 );
            WorkoutRepository.setConnection( DbConnector.getConnection() );
            //for (int id: userIds)
            int iterationSize = 5000;
            int startId = 25349998; //25360506; //25370506;
            int endId = startId - iterationSize;
            int cycle = 0;
            DateTime start;

            while ( cycle < 20 )
            {
                start = new DateTime( );
                System.out.println("Iteration " + cycle + ". Start: " + start);
                invalidUserCount = 0;
                addedUserCount = 0;
                rejectedIdCount = 0;
                addedWorkoutCount = 0;
                System.out.println("Decreasing from "+ startId + " to " + endId + "(" + iterationSize + " users)");
                System.out.println("Number of workouts before processing: " + workoutsBefore);
                for (int id = startId; id > endId; id--)
                {
                    try
                    {
                        String workoutsUrlContent = UrlConnector.getWorkoutsUrlContent( id );
                        List<Workout> workouts = UrlConnector.parseWorkoutsInInterval( id, workoutsUrlContent );
                        if (workouts != null && workouts.size() > 0)
                        {
                            boolean addUser = false;
                            for (Workout w: workouts)
                            {
                                try
                                {
                                    WorkoutRepository.insert( w );
                                    addedWorkoutCount++;
                                    addUser = true;
                                }
                                catch ( SQLException e )
                                {
                                    e.printStackTrace();
                                    notInsertedWorkouts.add( w );
                                }
                            }
                            if (addUser) addedUserCount++;
                        }
                    }
                    catch ( IOException e )
                    {
                        if (e.getMessage().contains( "429" ))
                        {
                            rejectedIdCount++;
                            System.out.println(id + " rejected (429). Retry in 12.5\"" );
                            Thread.sleep( 12500 );
                            try
                            {
                                String workoutsUrlContent = UrlConnector.getWorkoutsUrlContent( id );
                                List<Workout> workouts = UrlConnector.parseWorkoutsInInterval( id, workoutsUrlContent );
                                if (workouts != null && workouts.size() > 0)
                                {
                                    addedUserCount++;
                                    for (Workout w: workouts)
                                    {
                                        WorkoutRepository.insert( w );
                                        addedWorkoutCount++;
                                    }
                                }
                            }
                            catch ( Exception ex )
                            {
                                System.out.println("Rejected: " + e);
                                rejectedIds.add( id );
                            }
                        }
                        else
                        {
                            invalidUserCount++;
                            if (e.getMessage().contains( "500" )) System.out.println(id + " invalid (500)");
                            else System.out.println(e + "");
                        }
                    }
                    if (id%499 == 0)
                    {
                        System.out.println("Processed " + id + ", 5\" break");
                        Thread.sleep( 5000 );
                    }
                    if (id%1000 == 0)
                    {
                        end = new DateTime( );
                        System.out.println((startId - id) + " users processed: " + end);
                        duration = (end.getMillis() - start.getMillis()) / 1000; // in seconds
                        LOGGER.debug( "Processed: " + (startId - id ) + "ids, elapsed from iteration start " + duration);
                        LOGGER.debug( "Users added from iteration start: " + addedUserCount + ", rejected: " + rejectedIdCount + ", invalid: " + invalidUserCount );
                        LOGGER.debug( "Workouts added from iteration start: " + addedWorkoutCount );
                    }
                }
                System.out.println("Last processed id: " + (endId - 1));
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
                                    addedWorkoutCount++;
                                }
                                addedUserCount++;
                            }
                        }
                        catch ( IOException e )
                        {
                           System.out.println(id + ": " + e);
                            invalidUserCount++;
                        }
                        System.out.println("Processed "+ id);
                        if (i%25 == 0)
                        {
                            Thread.sleep( 5000 );
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
                            addedWorkoutCount++;
                        }
                        catch ( SQLException e )
                        {
                            System.out.println(w.getId() + " " + e);
                        }
                    }
                }
                end = new DateTime( );
                System.out.println("Iteration End: " + end);
                duration = (end.getMillis() - start.getMillis()) / 1000; // in seconds
                int hour, min, sec;
                hour = (int) (duration / (60 * 60));
                min = (int) ((duration - hour * 60) / 60);
                sec = (int) (duration - min * 60);
                System.out.println("Duration: " + hour + ":" + min + ":" + sec);
                System.out.println("Users with cycling activities: " + addedUserCount);
                System.out.println("Workouts added: " + addedWorkoutCount);
                System.out.println("No user with such ID: " + invalidUserCount);
                System.out.println("Rejected due to multiple requests: " + rejectedIdCount);
                LOGGER.debug( "ITERATION ENDED. Duration: " + hour + ":" + min + ":" + sec);
                LOGGER.debug( "Users added from iteration start: " + addedUserCount + ", rejected: " + rejectedIdCount + ", invalid: " + invalidUserCount );
                LOGGER.debug( "Workouts added from iteration start: " + addedWorkoutCount );

                workoutsBefore += addedWorkoutCount;
                startId = endId;
                endId = startId - iterationSize;
                cycle++;
            }

            DbConnector.closeConnection();
        }
        catch ( ClassNotFoundException e )
        {
            LOGGER.error( "Driver not found, no connection to DB: ", e );
            e.printStackTrace();
            try
            {
                if (DbConnector.getConnection() != null) DbConnector.closeConnection();
            }
            catch ( SQLException e1 )
            {
                e1.printStackTrace();
            }
        }
        catch ( SQLException e )
        {
            LOGGER.error( "Faulty DB credentials or problem with insertion ", e );
            e.printStackTrace();
            try
            {
                if (DbConnector.getConnection() != null) DbConnector.closeConnection();
            }
            catch ( SQLException e1 )
            {
                e1.printStackTrace();
            }
        }
        catch ( InterruptedException e )
        {
            LOGGER.error( "Thread interrupted ", e );
            e.printStackTrace();
            try
            {
                if (DbConnector.getConnection() != null) DbConnector.closeConnection();
            }
            catch ( SQLException e1 )
            {
                e1.printStackTrace();
            }
        }
    }
}

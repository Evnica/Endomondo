package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutRepository;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
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
    private static int iterationSize = 5000;
    private static int startId = 25239950;
    private static int workoutsBefore = 197601;
    private static int endId = 25238995;//startId - iterationSize;
    private static int cycle = 0;
    private static DateTime start = new DateTime(  ), end;
    private static int invalidUserCount = 0, rejectedIdCount = 0, addedUserCount = 0, addedWorkoutCount = 0;
    private static double duration;
    private static List<Integer> rejectedIds;
    private static List<Workout> notInsertedWorkouts = new ArrayList<>();

    public static void main( String[] args )
    {
        int idOffset = startId;
        try
        {
            DbConnector.connectToDb();
            WorkoutRepository.setConnection( DbConnector.getConnection() );

            while ( cycle < 1 )
            {
                //start = new DateTime( );
                System.out.println("Iteration " + cycle /*+ ". Start: " + start*/);
                rejectedIds = new ArrayList<>(  );
/*                invalidUserCount = 0;
                addedUserCount = 0;
                rejectedIdCount = 0;
                addedWorkoutCount = 0;*/
                System.out.println("Decreasing from "+ startId + " to " + endId + "(" + (startId - endId) /*iterationSize*/ + " users)");
                //System.out.println("Number of workouts before processing: " + workoutsBefore);
                for (int id = startId; id > endId; id--)
                {
                    retrieveWorkoutsForUser( id );
                    if (id%499 == 0)
                    {
                        System.out.println("Processed user #" + id + ", 5\" break");
                        Thread.sleep( 5000 );
                    }
                    if (id%1000 == 0)
                    {
                        end = new DateTime( );
                        System.out.println((startId - id) + " processed user count: " + end);
                        duration = (end.getMillis() - start.getMillis()) / 1000; // in seconds
                        /*LOGGER.debug( "Processed: " + (startId - id ) + "ids, elapsed from iteration start " + duration);
                        LOGGER.debug( "Users added from iteration start: " + addedUserCount + ", rejected: " + rejectedIdCount + ", invalid: " + invalidUserCount );
                        LOGGER.debug( "Workouts added from iteration start: " + addedWorkoutCount );*/
                        LOGGER.debug( ( idOffset - id) +";" + duration + ";" + addedUserCount + ";" +
                                rejectedIdCount + ";" + invalidUserCount + ";" + addedWorkoutCount );
                    }
                }
                int lastProcessed = endId + 1;
                System.out.println("Last processed id: " + lastProcessed);

                if (rejectedIds.size() > 0)
                {
                    System.out.println("Processing rejected: ");
                    rejectedIds.forEach( WorkoutTableFilling::processRejectedUser );
                }

                /*if (notInsertedWorkouts.size() > 0)
                {
                    System.out.println("Processing not inserted...");
                    processNotInserted();
                }*/

                printStatistics();
                workoutsBefore += addedWorkoutCount;
                startId = endId;
                endId -= iterationSize;
                cycle++;
            }
            DbConnector.closeConnection();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Exception: ", e );
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

    private static void retrieveWorkoutsForUser(int id)
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
                        System.out.println("" + e);
                        //notInsertedWorkouts.add( w );
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
                try
                {
                    Thread.sleep( 12500 );
                } catch ( InterruptedException e1 )
                {
                    LOGGER.error( "Can't sleep on id " + id );
                }
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
            else if (e.getMessage().contains( "500" ))
            {
                invalidUserCount++;
                 System.out.println(id + " invalid (500)");
            }
            else if (e instanceof UnknownHostException || e instanceof ConnectException)
            {
                rejectedIds.add( id );
                try
                {
                    Thread.sleep( 30000 );
                } catch ( InterruptedException e1 )
                {
                    LOGGER.error( "AAAAAAAAA!!! Connection lost! Tried to sleep but failed =( " + id );
                }
            }
            else
            {
                rejectedIds.add( id );
                LOGGER.error( "X3 what is that: ", e );
            }
        }
    }

    private static void processNotInserted()
    {
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
        notInsertedWorkouts = new ArrayList<>(  );
    }

    private static void processRejectedUser(int id)
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
                        addedWorkoutCount++;
                    } catch ( SQLException e )
                    {
                        System.out.println("DB Exception: " + e);
                    }
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
    }

    private static void printStatistics()
    {
        end = new DateTime( );
        System.out.println("ITERATION ENDED: " + end);
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
                /*LOGGER.debug( "ITERATION ENDED. Duration: " + hour + ":" + min + ":" + sec);
                LOGGER.debug( "Users added from iteration start: " + addedUserCount + ", rejected: " + rejectedIdCount + ", invalid: " + invalidUserCount );
                LOGGER.debug( "Workouts added from iteration start: " + addedWorkoutCount );*/
    }
}

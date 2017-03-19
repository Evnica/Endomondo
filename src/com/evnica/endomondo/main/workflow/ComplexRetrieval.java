package com.evnica.endomondo.main.workflow;

import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.Workout;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 3/14/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class ComplexRetrieval
{
    private static int startUserId = 0;
    private static int iterationSize = 500;
    private static int numOfIterations = 1;
    private static DateTime startTime;
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path RETRIEVED_PAIRS_PATH = Paths.get( "./jsonResult/workout-user_pairs.txt" );
    private static final Path INVALID_USER_PATH = Paths.get( "./jsonResult/invalid_user.txt" );
    private static final Path REJECTED_USER_PATH = Paths.get( "./jsonResult/rejected_user.txt" );
    private static final Path REJECTED_WRKT_PATH = Paths.get( "./jsonResult/rejected_wrkt.txt" );
    private static final Path STAT_PATH = Paths.get( "./jsonResult/retrieval_statistics.txt" );
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ComplexRetrieval.class.getName());
    // first 4 count errors when processing a known user-workout pair, the last one - when retrieving such a pair
    private static List<Integer> rejectedUserIds = new ArrayList<>(),
                                 invalidUserIds = new ArrayList<>(),
                                 rejectedWorkoutIds = new ArrayList<>(),
                                 invalidWorkoutIds = new ArrayList<>(),
                                 rejectedUsersForPairs = new ArrayList<>();
    private static int sentRequests = 0,
                       rejectedRequests = 0;

    public static void main(String[] args) {
        readParameters();
        process();
    }

    private static void readParameters()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter start user id: ");
        try {
            startUserId = Integer.parseInt(reader.readLine());
            System.out.println("Interim statistics are saved after each iteration. Enter iteration size: ");
            iterationSize = Integer.parseInt(reader.readLine());
            System.out.println("Enter number of iterations: ");
            numOfIterations = Integer.parseInt(reader.readLine());
            startTime = new DateTime();
            System.out.println("Start at " + startTime.toString(FORMATTER));
            System.out.println("Start id: " + startUserId +
                                ", end id: " + (startUserId + numOfIterations * iterationSize));
        } catch (Exception e) {
            System.out.println("Invalid input. Terminate.");
            LOGGER.error(e);
            System.exit(-1);
        }
    }

    private static void process()
    {
        int endUserId;

        for (int j = 0; j < numOfIterations; j++)
        {
            DateTime iterationStart = new DateTime();
            endUserId = startUserId + iterationSize;
            System.out.println("Processing " + startUserId + " to " + endUserId );

            for (int i = startUserId; i < endUserId; i++)
            {
                System.out.println(i);
                processOneId(i);
            }

            DataRetrieval.jsonLog(startUserId, endUserId, rejectedUserIds, rejectedWorkoutIds);
            //time, iteration#, startUserId, endUserId, duration mins, sent requests, rejected requests, rejected users for pairs, rejected demographics, invalid user, rejected workouts, invalid workouts
            String statistics = new DateTime().toString(FORMATTER);
            statistics += ";" + (j + 1) + ";";
            statistics += startUserId + ";";
            statistics += endUserId + ";";
            System.out.println("Iteration " + (j + 1) + " ended:");
            long duration = (new DateTime().getMillis() - iterationStart.getMillis()) / 60000; // in minutes
            System.out.println("Duration (mins): " + duration + ", from " + iterationStart + " to "
                    + new DateTime().toString(FORMATTER));
            statistics += duration + ";";
            System.out.println("Sent requests: " + sentRequests);
            statistics+= sentRequests + ";";
            System.out.println("Rejected requests: " + rejectedRequests);
            statistics+=rejectedRequests + ";";
            System.out.println("Rejected users for pairs: " + rejectedUsersForPairs.size());
            statistics+= rejectedUsersForPairs.size() +";";
            System.out.println("Rejected user demographics: " + rejectedUserIds.size());
            statistics+= rejectedUserIds.size() +";";
            if (rejectedUserIds.size() > 0) writeIDsToFile(REJECTED_USER_PATH, rejectedUserIds);
            System.out.println("Invalid user ids: " + invalidUserIds.size());
            statistics+= invalidUserIds.size() +";";
            if (invalidUserIds.size() > 0) writeIDsToFile(INVALID_USER_PATH, invalidUserIds);
            System.out.println("Rejected workouts: " + rejectedWorkoutIds.size());
            statistics+= rejectedWorkoutIds.size();
            if(rejectedWorkoutIds.size() > 0) writeIDsToFile(REJECTED_WRKT_PATH,  rejectedWorkoutIds);
            System.out.println("Invalid workouts: " + invalidWorkoutIds.size());
            statistics+= invalidWorkoutIds.size();

            try {
                basicWriteToFile(STAT_PATH, statistics);
            } catch (IOException e) {
                LOGGER.error("Can't write statistics to file");
                System.out.println("Can't write statistics to file: " + e);
            }

            toDefaults();
            startUserId = endUserId;
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Processing started at " + startTime.toString(FORMATTER) +
                                    ", ended at " + new DateTime().toString(FORMATTER));
    }

    private static void toDefaults()
    {
        rejectedUserIds = new ArrayList<>();
        invalidUserIds = new ArrayList<>();
        rejectedWorkoutIds = new ArrayList<>();
        invalidWorkoutIds = new ArrayList<>();
        rejectedUsersForPairs = new ArrayList<>();
        sentRequests = 0;
        rejectedRequests = 0;
    }

    private static void processOneId(int id) {
        boolean fromUs = false;
        int retrievedWorkoutCount = 0;
        String notLoadedWorkoutIds = "";
        String workoutsUrlContent;
        // try to retrieve user-workout pairs
        try
        {
            sentRequests++;
            workoutsUrlContent = UrlConnector.getUserWorkoutPairsUrlContent( id );
        } catch (IOException e)
        {
            workoutsUrlContent = processUrlIOException(e, id, true, null);
        }
        // if request was successful
        if (workoutsUrlContent != null)
        {
            List<Workout> workouts = UrlConnector.parseWorkoutsInInterval( id, workoutsUrlContent );
            // check if user has any workouts within defined time interval
            if (workouts != null && workouts.size() > 0)
            {
                String logMessage = id + ";";
                String userJsonContent;
                // if user has workouts within time of interest, retrieve demographic data
                try
                {
                    sentRequests++;
                    userJsonContent = UrlConnector.getUserUrlContent( id );
                }
                catch ( IOException e )
                {
                    userJsonContent = processUrlIOException( e, id, false, null );
                }
                // if data present, save it
                if (userJsonContent != null)
                {
                    try
                    {
                        JSONObject userObject = new JSONObject( userJsonContent );
                        if (userObject.getString( "country" ).toLowerCase().equals("us"))
                        {
                            DataRetrieval.writeToJson( id, "user_us", userJsonContent );
                            fromUs = true;
                        }
                        else
                        {
                            fromUs = false;
                            DataRetrieval.writeToJson( id, "user", userJsonContent );
                        }
                    }
                    catch (Exception e) // JSON or NullPointer
                    {
                        fromUs = false;
                        DataRetrieval.writeToJson( id, "user", userJsonContent );
                    }

                    logMessage += "valid;";
                    System.out.println("User " + id + " json saved");
                    logMessage += workouts.size() + ";";
                }
                else {logMessage += "invalid;";}
                // retrieve geometries of trips
                for (Workout w: workouts)
                {
                    String workoutContent;
                    try
                    {
                        sentRequests++;
                        workoutContent = UrlConnector.getWorkoutUrlContent( w );
                    }
                    catch ( IOException e )
                    {
                        workoutContent = processUrlIOException( e, id, false, w );
                    }
                    if (workoutContent != null)
                    {
                        if (fromUs)
                        {
                            DataRetrieval.writeToJson( w.getId(), "workout_us", workoutContent );
                        }
                        else
                        {
                            DataRetrieval.writeToJson( w.getId(), "workout", workoutContent );
                        }

                        writePairToFile(id, w.getId(), w.getLocalStartTime(), w.getSport());
                        retrievedWorkoutCount++;

                        logMessage += w.getId() + " ";
                        System.out.println("Workout " + w.getId() + " json saved");
                    }
                    else
                    {
                        notLoadedWorkoutIds += w.getId() + " ";
                    }
                    try { Thread.sleep( 2000 ); }
                    catch ( InterruptedException e ) { e.printStackTrace(); }
                }
                System.out.println(retrievedWorkoutCount + " workouts retrieved for user " + id);
                if (notLoadedWorkoutIds.length() > 0)
                {
                    logMessage += ";" + notLoadedWorkoutIds;
                }
                else {logMessage += ";n/a";}
                LOGGER.info( logMessage );
                System.out.println(sentRequests + " requests sent; " + rejectedRequests + " requests rejected");
            }
        }

    }

    private static String processUrlIOException(IOException e, int id, boolean pairs, Workout workout)
    {
        String jsonContent = null;
        if (e.getMessage().contains( "429" ))
        {
            rejectedRequests++;
            System.out.println(id + ": code 429; number of sent requests up to this time: " + sentRequests);
            try { Thread.sleep( 13000 ); }
            catch ( InterruptedException e1 ) { LOGGER.error( "Can't sleep on id " + id ); }
            try
            {
                sentRequests++;
                if (pairs) jsonContent = UrlConnector.getUserWorkoutPairsUrlContent( id );
                else if ( workout == null ) jsonContent = UrlConnector.getUserUrlContent( id );
                else jsonContent = UrlConnector.getWorkoutUrlContent( workout );
            }
            catch ( Exception ex )
            {
                if (workout == null) System.out.println( id + " user rejected after sleeping for 13\": " + ex);
                else System.out.println( workout.getId() + " wrkt rejected after sleeping for 13\": " + ex);

                if (pairs) rejectedUsersForPairs.add(id);
                else if (workout == null) rejectedUserIds.add( id );
                else rejectedWorkoutIds.add( workout.getId() );
            }
        }
        else if (e instanceof java.io.FileNotFoundException || e.getMessage().contains( "500" ))
        {
            if (workout == null) invalidUserIds.add(id);
            else invalidWorkoutIds.add(workout.getId());
        }
        else if (e.getMessage().contains("403"))
        {
            int j = workout != null ? workout.getId() : id;
            LOGGER.error("FATAL: 403! Terminate. Last processed id: " + j);
            System.out.println("FATAL: 403! Terminate. Last processed id: " + j);
            System.exit(-1);
        }
        else if (e instanceof UnknownHostException || e instanceof ConnectException)
        {
            if (workout == null) rejectedUserIds.add( id );
            else rejectedWorkoutIds.add( workout.getId() );
            int j = workout != null ? workout.getId() : id;
            LOGGER.error( "NO CONNECTION! Trying to sleep for 30s, id cause: " + j );
            try { Thread.sleep( 30000 ); }
            catch ( InterruptedException e1 )
            {
                LOGGER.error( "NO CONNECTION! Tried to sleep but failed =( " + j );
                System.exit( -1 );
            }
        }
        else
        {
            if (workout == null) rejectedUserIds.add( id );
            else rejectedWorkoutIds.add( workout.getId() );
            LOGGER.error( "X3 what is that: ", e );
            try { Thread.sleep( 30000 ); }
            catch ( InterruptedException e1 )
            { LOGGER.error( "Sleep just in case on " + id ); }
        }
        return jsonContent;
    }

    private static void writePairToFile(int id, int workoutId, DateTime start, int sport)
    {
        try
        {
            basicWriteToFile(RETRIEVED_PAIRS_PATH,
                    (workoutId + "," + sport + "," + id + "," + start.toString(FORMATTER)));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static void writeIDsToFile(Path file, List<Integer> ids)
    {
        StringBuilder contentBuilder = new StringBuilder();
        String part;
        for (int id: ids)
        {
            part = id + ", ";
            contentBuilder.append(part);
        }
        try {
            basicWriteToFile(file, contentBuilder.toString());
        } catch (IOException e) {
            LOGGER.error(e);
            LOGGER.info("IDs:");
            LOGGER.info(contentBuilder.toString());
        }
    }

    private static void basicWriteToFile(Path file, String line) throws IOException
    {
        List<String> lines = new ArrayList<>(  );
        lines.add(line);
        Files.write( file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND );
    }
}

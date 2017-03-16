package com.evnica.endomondo.main.workflow;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.connect.UrlConnector;
import com.evnica.endomondo.main.model.AthleteRepository;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutRepository;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class: DataRetrieval
 * Version: 0.1
 * Created on 02.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class DataRetrieval
{
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(DataRetrieval.class.getName());
    private static int rejectedIdCount, invalidIdCount;
    private static List<Integer> rejectedUserIds = new ArrayList<>(  );
    private static List<Integer> rejectedWorkoutIds = new ArrayList<>(  );
    private static int numOfRandoms, iterationSize, start;
    private static boolean withDbUse = false;

    private static Map<Integer, List<Workout>> workoutMap;

    public static void main( String[] args )
    {
        readParameters();
        // 100, 10, after 10000
        retrieveData(numOfRandoms, iterationSize, start, withDbUse);
    }

    private static void readParameters()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter number of random integers for delay:");
        try
        {
            numOfRandoms = Integer.parseInt( reader.readLine() );
            System.out.print( "Enter iteration size:" );
            iterationSize = Integer.parseInt( reader.readLine() );
            System.out.println( "Enter start id:" );
            start = Integer.parseInt( reader.readLine() );
            System.out.println( "Do you want to store results in DB? 'Y' for YES: " );
            if ( reader.readLine().toUpperCase().equals( "Y" ) )
            {
                withDbUse = true;
                System.out.println( "Please input db password:" );
                DbConnector.setPwd( reader.readLine() );
            }

            System.out.println( "Start: " + start + ", end id " + ( numOfRandoms * iterationSize + start ) + ", iteration size: "
                    + iterationSize + ". Random delays apply" );
        }
        catch(NumberFormatException nfe)
        {
            System.err.println("Invalid Format!");
            System.out.println("");
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private static void retrieveData(int numOfRandoms, int iterationSize, int start, boolean withDbUse)
    {
        {
            Random random = new Random();
            IntStream intStream = random.ints(8, 20);
            List<Integer> randomBetween8And20 = intStream
                    .limit(numOfRandoms)
                    .boxed()
                    .collect( Collectors.toList());

            try
            {
                List<Integer> ids;
                if ( withDbUse )
                {
                    DbConnector.connectToDb();
                    WorkoutRepository.setConnection( DbConnector.getConnection() );
                    //get distinct user ids
                    ids = WorkoutRepository.getUserIds();
                    WorkoutRepository.setConnection( DbConnector.getConnection() );
                }
                else
                {
                    ids = getIdsFromFile();
                }

                System.out.println( ids.size() + " distinct users in db. Starting data retrieval..." );
                int end;
                for (int j = 0; j < numOfRandoms; j++)
                {
                    end = start + iterationSize;
                    System.out.println("Processing from " + start + " to " + end);

                    rejectedIdCount = 0;
                    invalidIdCount = 0;
                    rejectedUserIds = new ArrayList<>(  );
                    rejectedWorkoutIds = new ArrayList<>(  );
                    String logMessage;
                    String notLoadedWorkoutIds = "";

                    for ( int i = start; i < end; i++ )
                    {
                        logMessage = new DateTime( ).toString( "yyyy-MM-dd HH:mm:ss" );
                        int id = ids.get( i );
                        logMessage += ";" + id + ";";
                        // load user data - done by UrlConnector
                        String userJsonContent;
                        try
                        {
                            userJsonContent = UrlConnector.getUserUrlContent( id );
                        }
                        catch ( IOException e )
                        {
                            userJsonContent = processUrlIOException( e, id, null, withDbUse );
                        }
                        if (userJsonContent != null)
                        {
                            writeToJson( id, "user", userJsonContent );
                            logMessage += "valid;";
                            System.out.println("User " + id + " json saved");
                        }
                        else {logMessage += "invalid;";}
                        try
                        {
                            // select all workouts associated with this user id
                            List<Workout> workouts;
                            if ( withDbUse )
                            {
                                workouts = WorkoutRepository.selectByUserId( id );
                            }
                            else
                            {
                                workouts = getWorkoutIdsFromFile(id);
                            }
                            // for each workout load json content and save to json
                            logMessage += workouts.size() + ";";

                            for (Workout w: workouts)
                            {
                                String workoutContent;
                                try
                                {
                                    workoutContent = UrlConnector.getWorkoutJsonUrlContent( w );
                                }
                                catch ( IOException e )
                                {
                                    workoutContent = processUrlIOException( e, w.getId(), w, withDbUse );
                                }
                                if (workoutContent != null)
                                {
                                    writeToJson( w.getId(), "workout", workoutContent );
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
                            if (notLoadedWorkoutIds.length() > 0)
                            {
                                logMessage += ";" + notLoadedWorkoutIds;
                            }
                            else {logMessage += ";n/a";}

                            LOGGER.info( logMessage );

                        }
                        catch ( SQLException e )
                        {
                            LOGGER.error( "user id " + id + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                        System.out.println("User " + id + " data retrieved. Next!");
                        Thread.sleep( randomBetween8And20.get( j ) );
                    }
                    jsonLog( start, end, rejectedUserIds, rejectedWorkoutIds );
                    start = end;
                    System.out.println("Iteration " + j + " ended");
                    System.out.println("Rejected users: " + rejectedIdCount);
                    System.out.println("Invalid users: " + invalidIdCount);
                }

            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    DbConnector.closeConnection();
                } catch ( SQLException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }


    // save to a file if not empty
    // folder = user
    static void writeToJson(int id, String folder, String content)
    {
        try
        {
            Path file = Paths.get( "./jsonResult/" + folder + "/" + id + ".json" );
            List<String> lines = Arrays.asList(content.split( "\n" ));
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE);
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    // use only after DbConnector was connected to DB
    private static String processUrlIOException(IOException e, int id, Workout workout, boolean withDbUse)
    {
        String jsonContent = null;
        if (e.getMessage().contains( "429" ))
        {
            rejectedIdCount++;
            System.out.println(id + " rejected (429). Retry in 13\"" );
            try { Thread.sleep( 13000 ); }
            catch ( InterruptedException e1 ) { LOGGER.error( "Can't sleep on id " + id ); }
            try
            {
                if ( workout == null )
                {
                    jsonContent = UrlConnector.getUserUrlContent( id );
                }
                else
                {
                    jsonContent = UrlConnector.getWorkoutJsonUrlContent( workout );
                }
            }
            catch ( Exception ex )
            {
                System.out.println( id + " rejected after sleeping for 13\": " + ex);
                if (workout == null)
                {
                    rejectedUserIds.add( id );
                }
                else
                {
                    rejectedWorkoutIds.add( id );
                }
            }
        }
        else if (e.getMessage().contains( "500" ) || e instanceof java.io.FileNotFoundException)
        {
            invalidIdCount++;
            if ( withDbUse )
            {
                AthleteRepository.setConnection( DbConnector.getConnection() );
                try
                {
                    AthleteRepository.insertInvalidity( id );
                } catch ( SQLException e1 )
                {
                    e1.printStackTrace();
                    LOGGER.error( "Error inserting invalid user " + id + ": ", e1 );
                }
            }
            else
            {
                writeInvalidAthleteToFile(id);
            }
        }
        else if (e.getMessage().contains("403"))
        {
            LOGGER.error("FATAL: 403! Exiting. Last processed id: " + id);
            System.out.println("FATAL: 403! Exiting. Last processed id: " + id);
            System.exit(-1);
        }
        else if (e instanceof UnknownHostException || e instanceof ConnectException )
        {
            if (workout == null) { rejectedUserIds.add( id ); }
            else {rejectedWorkoutIds.add( id );}
            LOGGER.error( "AAAAAAAAA!!! Connection lost! Trying to sleep for 30s, id cause: " + id );
            try { Thread.sleep( 30000 ); }
            catch ( InterruptedException e1 )
            {
                LOGGER.error( "AAAAAAAAA!!! Connection lost! Tried to sleep but failed =( " + id );
                System.exit( -1 );
            }
        }
        else
        {
            if (workout == null) { rejectedUserIds.add( id ); }
            else {rejectedWorkoutIds.add( id );}
            LOGGER.error( "X3 what is that: ", e );
            try { Thread.sleep( 30000 ); }
            catch ( InterruptedException e1 )
            { LOGGER.error( "Sleep just in case on " + id ); }
        }
        return jsonContent;
    }

    static void jsonLog(int startUser, int endUser, List<Integer> rejectedUserIds, List<Integer> rejectedWorkoutIds)
    {
        try
        {
            Path file = Paths.get( "./log/jsonRetrieval.log" );
            List<String> lines = new ArrayList<>(  );
            lines.add( "{\"time\":\"" + new DateTime().toString("yyyy-MM-dd HH-mm-ss") + "\"," );
            lines.add( "\"start_user\":" + Integer.toString( startUser ) );
            lines.add( ",\"end_user\":" + Integer.toString( endUser ) );
            lines.add( ",\"rejected_users\":" + toJsonArray( rejectedUserIds ) );
            lines.add( ",\"rejected_wrkt\":" + toJsonArray( rejectedWorkoutIds ) );
            lines.add( "}," );
            Files.write( file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static String toJsonArray(List<Integer> list)
    {
        StringBuilder result = new StringBuilder(  );
        result.append( "[" );
        if (list.size() > 0)
        {
            result.append( Integer.toString( list.get( 0 ) ) );
            if (list.size() > 1)
            {
                for (int i = 1; i < list.size(); i++)
                {
                    result.append( "," );
                    result.append( Integer.toString( list.get( i ) ) );
                }
            }
        }
        result.append( "]" );
        return result.toString();

    }

    private static List<Integer> getIdsFromFile() throws FileNotFoundException
    {
        String userWorkoutPairs = new Scanner
                                 (new FileInputStream
                                 (new File("./interimTables/workout-user-all.txt")), "UTF-8")
                                 .useDelimiter("\\A").next();
        String[] individualPairs = userWorkoutPairs.split("\n");
        String[] entry;
        DateTime timestamp;
        int workoutId, userId, sport;
        List<Workout> workouts = new ArrayList<>();
        for (String individualPair : individualPairs)
        {
            //id,sport,user_id,start_dt
            entry = individualPair.split(",");
            workoutId = Integer.parseInt(entry[0]);
            sport = Integer.parseInt(entry[1]);
            userId = Integer.parseInt(entry[2]);
            timestamp = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(entry[3].substring(0, 19));

            workouts.add(new Workout(workoutId, sport, timestamp, userId));
        }

        workoutMap = workouts.stream()
                .collect( Collectors.groupingBy( Workout::getUserId ) );

        return workoutMap.entrySet().stream()
                                .sorted(Comparator.comparing( Map.Entry::getKey ))
                                .map( Map.Entry::getKey ).collect( Collectors.toList());
    }

    private static List<Workout> getWorkoutIdsFromFile(int userId)
    {
        return workoutMap.get( userId );
    }

    private static void writeInvalidAthleteToFile(int id)
    {
        try
        {
            Path file = Paths.get( "./log/invalidUsers.log" );
            List<String> lines = new ArrayList<>(  );
            lines.add( Integer.toString( id ) );
            Files.write( file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}

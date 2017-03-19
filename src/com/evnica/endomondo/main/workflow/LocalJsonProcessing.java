package com.evnica.endomondo.main.workflow;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.*;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Class: LocalJsonProcessing
 * Version: 0.1
 * Created on 06.03.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class LocalJsonProcessing
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(LocalJsonProcessing.class.getName());
    private static boolean parseWorkouts = true;
    private static String dir = "C:\\Users\\d.strelnikova\\DATA\\archive\\workout\\___interim";


    public static void main(String[] args) {
        readParameters();
        if (parseWorkouts) {
            processWorkouts(dir);
        } else {
            processAthletes(dir);
        }

    }

    private static void readParameters()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Workouts are parsed/added to DB by default. If you want to parse athletes, press 'A':");
        try
        {
            if ( reader.readLine().toUpperCase().equals( "A" ) )
            {
                parseWorkouts = false;
            }
            System.out.println( "Enter directory that contains JSON files:" );
            String directory = reader.readLine();
            if (directory != null && directory.length() > 0) {
                dir =  directory;
            }
            System.out.println( "Please input db password:" );
            DbConnector.setPwd( reader.readLine() );

            System.out.println("Start: " + new DateTime().toString(FORMATTER));

        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private static void processAthletes(String dir)
    {
        try {

            DbConnector.connectToDb();
            AthleteRepository.setConnection(DbConnector.getConnection());
            SummaryRepository.setConnection(DbConnector.getConnection());

            Athlete athlete;
            File[] files = getFilesInDir(dir);
            for (File f: files)
            {
                try
                {
                    athlete = processOneAthlete(f);
                    if (athlete != null)
                    {
                        try {
                            AthleteRepository.insert(athlete);
                        } catch (SQLException e) {
                            LOGGER.error(athlete.getId() + " athlete not inserted");
                        }
                        for (SummaryBySport s: athlete.getSummaryBySport())
                        {
                            try {
                                SummaryRepository.insert(s, athlete.getId());
                            } catch (SQLException e) {
                                LOGGER.error("not all summaries loaded for athlete " + athlete.getId());
                            }
                        }
                        System.out.println(athlete.getId() + " from " + athlete.getCountry() + " added");
                    }
                    else
                        {
                            LOGGER.error(f.getName() + " contains no athlete");
                        }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
            DbConnector.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Athlete processOneAthlete(File athleteData) throws IOException
    {
        String jsonContent = new Scanner(new FileInputStream(athleteData), "UTF-8")
                            .useDelimiter("\\A").next();
        return parseAthlete(jsonContent);
    }

    private static File[] getFilesInDir(String dir)
    {
        return new File(dir).listFiles();
    }

    private static Athlete parseAthlete( String jsonContent)
    {
        Athlete athlete = null;
        try {
            JSONObject userObject = new JSONObject( jsonContent );
            try
            {
                int id = userObject.getInt( "id" );
                int gender = userObject.getInt( "gender" );
                athlete = new Athlete(id);
                athlete.setGender(gender);
                try
                {
                    if (userObject.getInt( "workout_count" ) > 0)
                    {
                        athlete.setWorkoutCount(userObject.getInt( "workout_count" ));
                    }
                    else
                    {
                        System.out.println(id + " has no parseWorkouts ");
                    }
                    JSONArray summaryBySport;
                    try
                    {
                        summaryBySport = userObject.getJSONArray( "summary_by_sport" );
                        JSONObject individualSummary;
                        int sportId = -1;
                        for (int i = 0; i < summaryBySport.length(); i++)
                        {
                            try
                            {
                                individualSummary = summaryBySport.getJSONObject( i );
                                sportId = individualSummary.getInt( "sport" );
                                SummaryBySport sportSum = new SummaryBySport();
                                sportSum.sport = sportId;
                                sportSum.count = individualSummary.getInt( "count" );
                                sportSum.totalDistance = individualSummary.getDouble( "total_distance" );
                                sportSum.totalDuration = individualSummary.getDouble( "total_duration" );
                                athlete.addSummaryBySport(sportSum);
                            }
                            catch (JSONException e) {
                                System.out.println(id + " has no summary by sport " + sportId);
                            }
                        }
                    }
                    catch (JSONException e1)
                    {
                        System.out.println(id + " has no summary by sport");
                    }
                    try
                    {
                        String date = userObject.getString( "created_date" ).substring( 0, 19 );
                        DateTime createdOn;
                        try {
                            createdOn = FORMATTER.parseDateTime( date );
                        } catch (Exception e) {
                            createdOn = FORMATTER.withZone(DateTimeZone.UTC).parseDateTime( date );
                        }
                        athlete.setCreatedDate( createdOn );
                    } catch (JSONException e) {
                        System.out.println(id + " has no creation date");
                    }
                    try
                    {
                        String date = userObject.getString( "date_of_birth" ).substring( 0, 19 );
                        DateTime dob;
                        try {
                            dob = FORMATTER.parseDateTime( date );
                        } catch (Exception e) {
                            dob = FORMATTER.withZone(DateTimeZone.UTC).parseDateTime( date );
                        }
                        athlete.setDateOfBirth( dob );
                    }
                    catch (JSONException e)
                    {
                        System.out.println(id + " has no birth date");
                    }
                    try
                    {
                        athlete.setCountry(userObject.getString( "country" ));
                    }
                    catch (JSONException e)
                    {
                        System.out.println(id + " has no country");
                    }
                }
                catch (JSONException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            catch ( JSONException e )
            {
                System.out.println("Except: " + e);
            }
        } catch (JSONException e) {
            System.err.println("Invalid JSON athlete: " + e);
        }
        return athlete;
    }

    private static void processWorkouts(String dir)
    {
        int fileCounter = 0;
        try {
            DbConnector.connectToDb();
            LapRepository.setConnection(DbConnector.getConnection());
            PointRepository.setConnection(DbConnector.getConnection());
            AthleteRepository.setConnection(DbConnector.getConnection());
            WorkoutDetailRepository.setConnection(DbConnector.getConnection());
            File[] workoutFiles = getFilesInDir(dir);
            for (File file: workoutFiles)
            {
                try {
                    int workoutId = Integer.parseInt(file.getName().split(".j")[0]);
                    System.out.print("Starting " + workoutId + ", ");
                    String jsonContent = new Scanner(new FileInputStream(file), "UTF-8")
                            .useDelimiter("\\A").next();
                    WorkoutDetail workout = JSONContentParser.parseWorkoutDetailUrl(jsonContent, workoutId);

                    if (workout != null)
                    {
                        String country = null;
                        if (workout.getUserId() != -1)
                        {
                            try
                            {
                                country = AthleteRepository.getCountry(workout.getUserId());
                                System.out.println(country);
                            } catch (SQLException e) {
                                country = null;
                                System.out.println("no country");
                            }
                        }
                        try
                        {
                            WorkoutDetailRepository.insert(workout);
                            if (workout.getLaps() != null && workout.getLaps().size() > 0)
                            {
                                int insertedLapCount = 0;
                                for (Lap lap: workout.getLaps())
                                {
                                    try
                                    {
                                        insertedLapCount += LapRepository.insert(lap);
                                    }
                                    catch (Exception e)
                                    {
                                        LOGGER.error("Lap " + lap.getId() + " was not inserted for workout " + workoutId, e);
                                    }
                                }
                                System.out.println(insertedLapCount + " laps inserted in DB for workout " + workoutId);
                                try
                                {
                                    WorkoutDetailRepository.update(true, workoutId, insertedLapCount);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    LOGGER.error("Lap count not inserted for wrkt " + workoutId, e);
                                }
                            }

                            if (workout.getPoints() != null && workout.getPoints().size() > 1)
                            {
                                PointRepository.setInsertStatement(country);
                                int insertedPointCount = 0;
                                for (Point p: workout.getPoints())
                                {
                                    try
                                    {
                                        insertedPointCount += PointRepository.insertPoint(p, workoutId);
                                    } catch (SQLException e)
                                    {
                                        LOGGER.error("Can't insert point " + p.getOrder() +
                                                            " from workout " + workout.getId(), e);
                                    }
                                }
                                System.out.println(insertedPointCount + " points inserted in DB for workout " + workoutId);
                                try
                                {
                                    WorkoutDetailRepository.update(false, workoutId, insertedPointCount);
                                } catch (SQLException e) {
                                    System.out.println("Not inserted workout: " + e);
                                    LOGGER.error("Point count not inserted for wrkt " + workoutId, e);
                                }
                            }
                        } catch (SQLException e) {
                            System.out.println(workout.getId() + " wrkt not inserted: " + e);
                            LOGGER.error("Workout " + workout.getId() + " not inserted in DB");
                        }
                    }

                } catch (NumberFormatException e) {
                    LOGGER.error(file.getName() + " not parsed due to wrong id");
                }
                catch (FileNotFoundException e)
                {
                    LOGGER.error(file.getName() + " not found");
                }
                System.out.println("Processed: " + file.getName());
                fileCounter++;
            }
            DbConnector.closeConnection();
            System.out.println(fileCounter + " files processed");
            System.out.println("End: " + new DateTime().toString(FORMATTER));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

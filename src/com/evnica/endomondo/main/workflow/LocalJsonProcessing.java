package com.evnica.endomondo.main.workflow;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.*;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private static int noWorkouts = 0, noBirthday = 0, noDateCreated = 0;
    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(LocalJsonProcessing.class.getName());

    public static void main(String[] args) {
        processWorkouts("./interimTables/wrktJson");
        //processAthletes("./interimTables/userJson");
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
                    System.out.println(id + " has no workouts ");
                    noWorkouts++;
                }
                JSONArray summaryBySport;
                try
                {
                    summaryBySport = userObject.getJSONArray( "summary_by_sport" );
                    JSONObject individualSummary;
                    List<SummaryBySport> summaries = new ArrayList<>();
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
                    athlete.setCreatedDate( formatter.parseDateTime( date ) );
                } catch (JSONException e) {
                    System.out.println(id + " has no creation date");
                    noDateCreated++;
                }
                try
                {
                    String date = userObject.getString( "date_of_birth" ).substring( 0, 19 );
                    athlete.setDateOfBirth( formatter.parseDateTime( date ) );
                }
                catch (JSONException e)
                {
                    System.out.println(id + " has no birth date");
                    noBirthday++;
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
                noWorkouts++;
            }
        }
        catch ( JSONException e )
        {
            System.out.println("Except: " + e);
        }
        return athlete;
    }

    private static void processWorkouts(String dir)
    {
        try {
            DbConnector.connectToDb();
            LapRepository.setConnection(DbConnector.getConnection());
            PointRepository.setConnection(DbConnector.getConnection());
            AthleteRepository.setConnection(DbConnector.getConnection());
            File[] workoutFiles = getFilesInDir(dir);
            for (File file: workoutFiles)
            {
                try {
                    int workoutId = Integer.parseInt(file.getName().split(".j")[0]);
                    String jsonContent = new Scanner(new FileInputStream(file), "UTF-8")
                            .useDelimiter("\\A").next();
                    WorkoutJSON workoutJSON = JSONContentParser.parseWorkoutUrl(jsonContent, workoutId, TargetGeometry.BOTH);

                    if (workoutJSON != null) {
                        try {
                            if (workoutJSON.getLaps() != null && workoutJSON.getLaps().size() > 0)
                            {
                                for (Lap lap: workoutJSON.getLaps())
                                {
                                    LapRepository.insert(lap);
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("Not all laps inserted for workout " + workoutId);
                        }
                        if (workoutJSON.getPoints() != null && workoutJSON.getPoints().size() > 1)
                        {
                            String country = null;
                            if (workoutJSON.getUserId() != -1)
                            {
                                country = AthleteRepository.getCountry(workoutJSON.getUserId());
                            }
                            for (Point p: workoutJSON.getPoints())
                            {
                                try {
                                    PointRepository.insertPoint(country, p, workoutId);
                                } catch (SQLException e) {
                                    try {
                                        PointRepository.insertPoint(null, p, workoutId);
                                    } catch (SQLException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
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
            }
            DbConnector.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

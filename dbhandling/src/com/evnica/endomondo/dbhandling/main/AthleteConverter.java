package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.Athlete;
import com.evnica.endomondo.main.model.AthleteRepository;
import com.evnica.endomondo.main.model.SummaryBySport;
import com.evnica.endomondo.main.model.SummaryRepository;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Project: Endomondo
 * Class: AthleteConverter
 * Version: 0.1
 * Created on 3/31/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class AthleteConverter extends Converter
{

    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(AthleteConverter.class.getName());
    private int processed = 0, insertedAthletes = 0, athletesWithSummaries = 0, insertedSummaries = 0, invalid = 0;

    // outputs the following values: file count, processed files, inserted athletes,
    // inserted athletes with summaries, inserted summary count, invalid athletes;
    @Override
    public int[] process()
    {
        int fileCount = 0;
        try
        {
            DbConnector.connectToDb();
            AthleteRepository.setConnection(DbConnector.getConnection());
            SummaryRepository.setConnection(DbConnector.getConnection());

            File[] files = new File(dir).listFiles();
            if (files != null)
            {
                fileCount = files.length;
                for (File file: files)
                {
                    processFile(file);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Can't connect to DB ", e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try {
                DbConnector.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new int[]{fileCount, processed, insertedAthletes, athletesWithSummaries, insertedSummaries, invalid};
    }

    private void processFile(File file)
    {
        if (file.isDirectory())
        {
            File[] subDirs = file.listFiles();
            if (subDirs != null)
            {
                for (final File subFile : subDirs)
                    processFile(subFile);
            }
        }
        else
        {
            Athlete athlete;
            try
            {
                athlete = parseAthlete(readFile(file));
                if (athlete != null)
                {
                    try
                    {
                        AthleteRepository.insert(athlete);
                        // increment number of inserted in db
                        insertedAthletes++;
                    } catch (SQLException e)
                    {
                        LOGGER.error(athlete.getId() + " athlete not inserted");
                    }
                    if (athlete.getSummaryBySport() != null && athlete.getSummaryBySport().size() > 0)
                    {
                        athletesWithSummaries++;
                    }
                    for (SummaryBySport s: athlete.getSummaryBySport())
                    {
                        try {
                            SummaryRepository.insert(s, athlete.getId());
                            insertedSummaries++;
                        } catch (SQLException e) {
                            LOGGER.error("not all summaries loaded for athlete " + athlete.getId());
                        }
                    }
                    System.out.println(athlete.getId() + " from " + athlete.getCountry() + " added");
                }
                else
                {
                    // if parsing failed, athlete is invalid
                    invalid++;
                    LOGGER.error(file.getName() + " contains no athlete");
                }
                // if there was no IO Exception, file is considered to be processed
                processed++;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static Athlete parseAthlete(String jsonContent)
    {
        Athlete athlete = null;
        try
        {
            JSONObject userObject = new JSONObject( jsonContent );
            try
            {
                int id = userObject.getInt( "id" );
                athlete = new Athlete(id);
                try
                {
                    int gender = userObject.getInt( "gender" );
                    athlete.setGender(gender);
                }
                catch (JSONException e)
                {
                    System.out.println(id + " has no gender ");
                }
                try
                {
                    if (userObject.getInt( "workout_count" ) > 0)
                    {
                        athlete.setWorkoutCount(userObject.getInt( "workout_count" ));
                    }
                    else
                    {
                        System.out.println(id + " has no workout count ");
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
}

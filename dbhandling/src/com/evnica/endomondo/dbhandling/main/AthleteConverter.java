package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.Athlete;
import com.evnica.endomondo.main.model.SummaryBySport;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Project: Endomondo
 * Class: AthleteConverter
 * Version: 0.1
 * Created on 3/31/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class AthleteConverter extends Converter implements Runnable
{

    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(AthleteConverter.class.getName());
    private int processed = 0, convertedAthletes = 0, athletesWithSummaries = 0, summaries = 0, invalid = 0;
    static final String OUTPUT_NAME_ATHLETE = "athlete.txt";
    static final String OUTPUT_NAME_SUMMARY = "summary.txt";
    private static Connection athleteConnection = null;
    private String outDir = null;
    private DateTime retrieved = null;


    @Override
    public boolean initialize()
    {
        boolean success = false;

        athleteConnection = DbConnector.getNewConnection();
        if (athleteConnection != null) success = true;

        return success;
    }

    @Override
    public boolean terminate()
    {
        boolean success;
        try {
            athleteConnection.close();
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    // outputs the following values: file count, processed files, inserted athletes,
    // inserted athletes with summaries, inserted summary count, invalid athletes;

    private int[] process()
    {
        int fileCount = 0;
        try
        {
            File[] files = new File(Converter.athleteDirectory).listFiles();
            if (files != null)
            {
                fileCount = files.length;
                for (File file: files)
                {
                    processDirectory(file);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Can't process ", e.getMessage());
            e.printStackTrace();
        }


        return new int[]{fileCount, processed, convertedAthletes, athletesWithSummaries, summaries, invalid};
    }

    private void processDirectory(File dir)
    {

        if (dir.isDirectory())
        {
            outDir = String.format(OUTPUT_ATHLETES_DIR, dir.getName());
            try
            {
                BasicFileAttributes properties = Files.readAttributes(dir.toPath(), BasicFileAttributes.class);
                retrieved = new DateTime(properties.creationTime().toMillis());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                retrieved = DateTime.parse("2017-03-01"); // about the middle of retrieval period
            }

            File[] subDirs = dir.listFiles();
            if (subDirs != null)
            {
                for (final File subFile : subDirs)
                    processDirectory(subFile);
            }
            BulkLoader.loadAthletes(outDir);
        }
        else
        {
            Athlete athlete;
            if (outDir == null)
            {
                outDir = String.format(OUTPUT_ATHLETES_DIR, dir.getName());
            }
            try
            {
                athlete = JSONContentParser.parseAthlete(readFile(dir));
                if (athlete != null)
                {
                    athlete.retrieved = retrieved;
                    try
                    {
                        write((athlete.toString() + "\n"), outDir, OUTPUT_NAME_ATHLETE);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    convertedAthletes++;

                    if (athlete.getSummaryBySport() != null && athlete.getSummaryBySport().size() > 0)
                    {
                        athletesWithSummaries++;
                    }
                    StringBuilder summaryBuilder = new StringBuilder();
                    for (SummaryBySport s: athlete.getSummaryBySport())
                    {

                        summaryBuilder.append(athlete.getId());
                        summaryBuilder.append("\t");
                        summaryBuilder.append(s.toString());
                        summaryBuilder.append("\n");
                    }
                    try
                    {
                        write(summaryBuilder, outDir, OUTPUT_NAME_SUMMARY);
                        summaries+= athlete.getSummaryBySport().size();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println(athlete.getId() + " from " + athlete.getCountry() + " added");
                }
                else
                {
                    // if parsing failed, athlete is invalid
                    invalid++;
                    LOGGER.error(dir.getName() + " contains no athlete");
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

    static Connection getAthleteConnection() {
        return athleteConnection;
    }

    @Override
    public void run() {
        if (initialize())
        {
            process();
            terminate();
        }
    }
}

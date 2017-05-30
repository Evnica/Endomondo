package com.evnica.endomondo.dbhandling.main;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.nio.channels.Channels;
import java.util.Scanner;


/**
 * Project: Endomondo
 * Class: Converter
 * Version: 0.1
 * Created on 3/31/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public abstract class Converter
{
    static final org.joda.time.format.DateTimeFormatter FORMATTER =
                                                            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final org.joda.time.format.DateTimeFormatter FORMATTER_STYLE =
            DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
    static final String OUTPUT_DIR = "C:/Endoproject/toCopy/workouts/%s/";
    static final String OUTPUT_ATHLETES_DIR = "C:/Endoproject/toCopy/athletes/%s/";
    static final String STAT_FILE_CONVERSION = "stat_conversion.txt";

    static String workoutDirectory = null;
    static String athleteDirectory = null;

    public abstract boolean initialize();

    public abstract boolean terminate();

    static void readParameters()
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            System.out.println("Do you want to process workouts? ('Y' for yes): ");
            if (reader.readLine().toLowerCase().equals("y"))
            {
                System.out.println( "Please enter directory that contains workout JSON files:" );
                String directory = reader.readLine();
                try
                {
                    new File(directory);
                    workoutDirectory =  directory;
                }
                catch (Exception e)
                {
                    System.out.println("Directory is invalid and will not be processed.");
                    workoutDirectory = null;
                }
            }
            System.out.println("Do you want to process athletes? ('Y' for yes): ");
            if (reader.readLine().toLowerCase().equals("y"))
            {
                System.out.println( "Please enter directory that contains athlete JSON files:" );
                String directory = reader.readLine();
                try
                {
                    new File(directory);
                    athleteDirectory =  directory;
                }
                catch (Exception e)
                {
                    System.out.println("Athlete directory is invalid and will not be processed.");
                    athleteDirectory = null;
                }
            }

            /*System.out.println( "Please input db password:" );
            DbConnector.setPwd( reader.readLine() );*/
            System.out.println("Start: " + new DateTime().toString(FORMATTER_STYLE));

        }
        catch ( IOException e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
    }


    static String readFile(File file) throws IOException
    {
        return new Scanner(new FileInputStream(file), "UTF-8")
                .useDelimiter("\\A").next();
    }

    static void write(StringBuilder sb, String dir, String fileName) throws Exception
    {
        write(sb.toString(), dir, fileName);

    }

    public static void write(String sb, String dir, String fileName) throws Exception
    {
        File directory = new File((dir));
        boolean newDirCreated;
        newDirCreated = directory.exists() || directory.mkdir();
        if (newDirCreated)
        {
            String path = dir + fileName;
            File file = new File(path);
            try (Writer writer = Channels.newWriter(new FileOutputStream(
                    file.getAbsoluteFile(), true).getChannel(), "UTF-8")) {
                writer.append(sb);
            }
        } else {
            System.out.println("--------------Can't writhe a log--------------");
            System.out.println(sb);
        }

    }


}

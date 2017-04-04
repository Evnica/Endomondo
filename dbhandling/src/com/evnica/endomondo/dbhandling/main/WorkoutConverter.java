package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.Point;
import com.evnica.endomondo.main.model.PointRepository;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Project: Endomondo
 * Class: WorkoutConverter
 * Version: 0.1
 * Created on 3/31/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class WorkoutConverter extends Converter
{
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(WorkoutConverter.class.getName());
    private static final String OUTPUT_NAME_WRKT = "intr_workout.txt";
    private static final String OUTPUT_NAME_WRKT_DUBIOUS = "intr_workout_dbs.txt";
    private static final String OUTPUT_NAME_POINT = "intr_point_%s.txt";
    private static int processed = 0, full = 0, dubious = 0, invalid = 0;
    private static int processedPointCount = 0;
    private static Map<String, Integer[]> logPointByRegion = new LinkedHashMap<>(); // region - [wrkt_count, pt_cnt]

    @Override
    public int[] process()
    {
        try
        {
            DbConnector.connectToDb();
            initializeLogMap();

            PointRepository.setConnection(DbConnector.getConnection());
            processDirectory(new File(dir));
        }
        catch (Exception e)
        {
            LOGGER.error("Can't process ", e.getMessage());
            System.out.println("Can't process: ");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                DbConnector.closeConnection();
            }
            catch (SQLException e)
            {
                LOGGER.error("Can't close connection: ", e.getMessage());
                e.printStackTrace();
            }
        }
        return new int[]{processed, full, dubious, invalid};
    }

    private void processDirectory(File directory)
    {
        String outDir = String.format(OUTPUT_DIR, directory.getName());
        File[] files = directory.listFiles();
        if (files != null)
        {
            for(File file: files)
            {
                if (file.isDirectory())
                {
                    int count;
                    try
                    {
                        count = file.listFiles().length;
                    }
                    catch (Exception e)
                    {
                        count = 0;
                    }
                    int lastProcessedValue = processed;
                    int lastFullValue = full;
                    int lastDubiousValues = dubious;
                    int lastInvalidValue = invalid;
                    int processedPointLastValue = processedPointCount;

                    processDirectory(file);

                    int processedInDir = processed - lastProcessedValue;
                    int fullInDir = full - lastFullValue;
                    int dubiousInDir = dubious - lastDubiousValues;
                    int invalidInDir = invalid - lastInvalidValue;
                    int processPointsInDir = processedPointCount - processedPointLastValue;

                    System.out.println("Directory: " + file.getName() + ": contains " + count + " files, including:");
                    System.out.println("processed: " + processedInDir + ", full: " + fullInDir
                            + ", dubious: " + dubiousInDir + ", invalid: " + invalidInDir);
                    System.out.println("TOTAL:");
                    System.out.println("Processed: " + processed + ", full: " + full + ", dubious: " + dubious +
                            ", invalid: " + invalid );
                    LOGGER.info("Directory: " + file.getName() + ": contains " + count + " files");
                    LOGGER.info("Processed: " + processedInDir + ", full: " + fullInDir
                          + ", dubious: " + dubiousInDir + ", invalid: " + invalidInDir);
                    for (Map.Entry<String, Integer[]> entry: logPointByRegion.entrySet())
                    {
                        System.out.println(entry.getKey() + ": " + entry.getValue()[0] + ", " + entry.getValue()[1]);
                    }
                    System.out.println("Processed point: in dir "
                                        + processPointsInDir + ", total: " + processedPointCount);
                    LOGGER.info("Processed point: in dir "
                                    + processPointsInDir + ", total: " + processedPointCount);
                    // processedDirName,  path,  count,  processed,   invalid,  dubious,  full,  ptCount
                    writeStatistics(file.getName(), String.format(OUTPUT_DIR, "stat"), STAT_FILE_CONVERSION, count,
                            processedInDir, invalidInDir, dubiousInDir, fullInDir, processPointsInDir);
                    initializeLogMap();
                }
                else
                {
                    processOneWorkout(file, outDir);
                }
            }
        }

    }

    private void processOneWorkout(File file, String outDir)
    {
        String outputWrktFile = OUTPUT_NAME_WRKT;

        try
        {
            int workoutId = Integer.parseInt(file.getName().split(".j")[0]);
            String content = readFile(file);

            final WorkoutDetail workout = JSONContentParser.parseWorkoutDetail(content, workoutId, false);

            if (workout != null)
            {
                String region;
                if (workout.getPointCount() > 0)
                {
                    if (workout.getSpeed() <= 40 && workout.getDistance() > 0 && workout.getDuration() > 0)
                    {
                        System.out.println(workoutId + " is full");
                        full++;
                        region = PointRepository.determineRegion(workout.getPoints().get(0));
                    }
                    else
                    {
                        System.out.println(workoutId + " is dubious");
                        dubious++;
                        outputWrktFile = OUTPUT_NAME_WRKT_DUBIOUS;
                        region = "dbs"; // points of dubious workouts are stored in a separate table
                    }
                    StringBuilder pointBuilder = new StringBuilder();
                    for (Point point : workout.getPoints()) {
                        pointBuilder.append(point.getOrder());
                        pointBuilder.append("\t");
                        pointBuilder.append(workoutId);
                        pointBuilder.append("\t");
                        pointBuilder.append(String.format("%.4f", point.getDistanceFromPrevious()));
                        pointBuilder.append("\t");
                        pointBuilder.append(point.getDurationFromPrevious());
                        pointBuilder.append("\t");
                        pointBuilder.append("'");
                        pointBuilder.append(point.getTimeCaptured().toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
                        pointBuilder.append("'");
                        pointBuilder.append("\t");
                        pointBuilder.append(point.getLon());
                        pointBuilder.append("\t");
                        pointBuilder.append(point.getLat());
                        pointBuilder.append("\t");
                        pointBuilder.append(point.getDistanceFromOffset());
                        pointBuilder.append("\t");
                        pointBuilder.append(point.getDurationFromOffset());
                        pointBuilder.append("\n");
                    }

                    final String outPointFile = String.format(OUTPUT_NAME_POINT, region);

                    write(pointBuilder, outDir, outPointFile);

                    processedPointCount += workout.getPointCount();
                }
                else
                {
                    System.out.println(workoutId + " is dubious (no points)");
                    dubious++;
                    region = "dbs";
                    outputWrktFile = OUTPUT_NAME_WRKT_DUBIOUS;
                }

                StringBuilder workoutBuilder;

                workoutBuilder = new StringBuilder();
                workoutBuilder.append(workout.getId());
                workoutBuilder.append("\t");
                workoutBuilder.append(String.format("%.3f", workout.getDistance()));
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getDuration());
                workoutBuilder.append("\t");
                try
                {
                    workoutBuilder.append(workout.getStartAt().toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
                }
                catch (Exception e)
                {
                    workoutBuilder.append(new DateTime().toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
                }
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getWeather());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getUserId());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getShowMap());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getWorkoutGeometryType().getCode());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getPointCount());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getLapCount());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getSport());
                workoutBuilder.append("\t");
                workoutBuilder.append(workout.getTimeZone());
                workoutBuilder.append("\t");
                workoutBuilder.append(region);
                workoutBuilder.append("\t");
                workoutBuilder.append(String.format("%.3f", workout.getSpeed()));
                workoutBuilder.append("\n");

                write(workoutBuilder, outDir, outputWrktFile);

                if (workout.getPointCount() > 0)
                {
                    if (logPointByRegion.containsKey(region))
                    {
                        Integer[] oldValues = logPointByRegion.get(region);
                        oldValues[0] += 1; // workout count in the region
                        oldValues[1] += workout.getPointCount(); // point count in the region
                    }
                    else
                    {
                        logPointByRegion.put(region, new Integer[]{1, workout.getPointCount()});
                    }
                }
            }
            else
            {
                invalid++;
                System.out.println(workoutId + " is invalid");
            }
            processed++;
        }
        catch (Exception e)
        {
            LOGGER.error(file.getName() + " not parsable/accessible ", e.getMessage());
            System.out.println(file.getName() + " not parsable/accessible");
            e.printStackTrace();
        }
    }

    private static void writeStatistics(String processedDirName, String dir, String fileName,
                                        int count, int processed,  int invalid, int dubious, int full, int ptCount)
    {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(processedDirName);
        logBuilder.append("\t");
        logBuilder.append((count - processed));
        logBuilder.append("\t");
        logBuilder.append(count);
        logBuilder.append("\t");
        logBuilder.append(invalid);
        logBuilder.append("\t\t");
        logBuilder.append(dubious);
        logBuilder.append("\t");
        logBuilder.append(full);
        logBuilder.append("\t");
        logBuilder.append(ptCount);
        logBuilder.append("\t");
        for (Map.Entry<String, Integer[]> entry: logPointByRegion.entrySet())
        {
            logBuilder.append(entry.getKey());
            logBuilder.append("\t");
            logBuilder.append(entry.getValue()[1]); // point count
            logBuilder.append("\t");
            logBuilder.append(entry.getValue()[0]); // wrkt count
            logBuilder.append("\t");
        }
        logBuilder.append("\n");
        try
        {
            write(logBuilder,  dir, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.info("----------------------LOG NOT WRITTEN----------------------------");
            LOGGER.info(logBuilder.toString());
            System.out.println(logBuilder.toString());
        }
    }

    private static void initializeLogMap()
    {
        // could be requested from DB, but now is not necessary
        final String[] regions = {"fl", "us", "ar", "br", "cz", "de", "dk", "es", "fr", "gb", "id", "in",
                "it", "mx", "nl", "all", "pl", "th", "tw", "dbs"};

        for(String r: regions)
        {
            logPointByRegion.put(r, new Integer[]{0,0});
        }
    }

}

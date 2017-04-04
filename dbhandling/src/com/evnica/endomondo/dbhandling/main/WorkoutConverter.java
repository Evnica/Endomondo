package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.Point;
import com.evnica.endomondo.main.model.PointRepository;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: Endomondo
 * Class: ${CLASS_NAME}
 * Version: 0.1
 * Created on 3/31/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class WorkoutConverter extends Converter
{
    private final static Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(WorkoutConverter.class.getName());
    private static final String OUTPUT_NAME_WRKT = OUTPUT_DIR + "workout.txt";
    private static final String OUTPUT_NAME_WRKT_DUBIOUS = OUTPUT_DIR + "workout_dbs.txt";
    private static final String OUTPUT_NAME_POINT = OUTPUT_DIR + "point_%s.txt";
    private static int processed = 0, full = 0, dubious = 0, invalid = 0;
    private static int processedPointCount = 0, wrktPointCount = 0;
    private Map<String, Integer> logPointByRegion = new HashMap<>();

    @Override
    public int[] process()
    {
        try
        {
            DbConnector.connectToDb();
            PointRepository.setConnection(DbConnector.getConnection());
            File[] files = getFilesInDir(dir);
            for(File f: files)
            {
                processOneWorkout(f);
            }
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

    private void processOneWorkout(File file)
    {
        String outputWrktFile = OUTPUT_NAME_WRKT;
        if (file.isDirectory())
        {
            System.out.println("Directory: " + file.getName() + " is being processed");
            File[] subDirs = file.listFiles();
            if (subDirs != null)
            {
                int lastProcessedValue = processed;
                int lastFullValue = full;
                int lastDubiousValues = dubious;
                int lastInvalidValue = invalid;
                for (final File subFile : subDirs)
                {
                    processOneWorkout(subFile);
                }
                System.out.println("Directory: " + file.getName() + ": contains "
                                + subDirs.length + " files, including:");
                System.out.println("processed: " + (processed - lastProcessedValue) +
                                ", full: " + (full - lastFullValue)
                                + ", dubious: " + (dubious - lastDubiousValues) +
                                ", invalid: " + (invalid - lastInvalidValue));
                System.out.println("TOTAL:");
                System.out.println("Processed: " + processed + ", full: " + full + ", dubious: " + dubious +
                                ", invalid: " + invalid );
                LOGGER.info("Directory: " + file.getName() + ": contains " + subDirs.length + " files");
                LOGGER.info("Processed: " + (processed - lastProcessedValue) + ", full: " + (full - lastFullValue)
                        + ", dubious: " + (dubious - lastDubiousValues) + ", invalid: " + (invalid - lastInvalidValue));
                for (Map.Entry<String, Integer> entry: logPointByRegion.entrySet())
                {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                System.out.println("Processed point count: " + processedPointCount);
                LOGGER.info("Processed point count: " + processedPointCount);
            }
        }
        else
        {
            try
            {
                int workoutId = Integer.parseInt(file.getName().split(".j")[0]);
                String content = readFile(file);

                final WorkoutDetail workout = JSONContentParser.parseWorkoutDetail(content, workoutId, false);

                if (workout != null)
                {
                    String region = "all";
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
                            region = "dbs";
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
                            wrktPointCount++;
                        }
                        if (logPointByRegion.containsKey(region))
                        {
                            logPointByRegion.put(region, (logPointByRegion.get(region) + wrktPointCount));
                        }
                        else
                        {
                            logPointByRegion.put(region, wrktPointCount);
                        }
                        final String outPointFile = String.format(OUTPUT_NAME_POINT, region);

                        write(pointBuilder, outPointFile);
                        processedPointCount += wrktPointCount;
                        wrktPointCount = 0;
                    }
                    else
                    {
                        System.out.println(workoutId + " is dubious (no points)");
                        dubious++;
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
                    workoutBuilder.append(workout.getStartAt().toString("yyyy-MM-dd HH:mm:ss.SSSZ"));
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

                    write(workoutBuilder, outputWrktFile);

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
    }
}

package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.model.Point;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.postgis.PGgeometry;

import java.io.*;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Scanner;

/**
 * Project: Endomondo
 * Class: ImportOptimizer
 * Version: 0.1
 * Created on 3/21/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class ImportOptimizer {

    private int processedPointCount = 0;
    private int fileOrderPoints = 1; // fileOrderLaps = 1;
    private static final String OUTPUT_DIR = "C:\\Users\\d.strelnikova\\DATA\\toCopy\\";
    private static final String OUTPUT_NAME_WRKT = OUTPUT_DIR + "workout-%d.txt";
    //private static final String OUTPUT_NAME_LAP = OUTPUT_DIR + "lap-%d.txt";
    private static final String OUTPUT_NAME_POINT = OUTPUT_DIR + "point-%d.txt";
    private static String outNameWrkt, outNamePoint;

    static String composeInsertPointsString(List<Point> points, int workoutId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        String insertStatement =
                "INSERT INTO spatial.point (id, wrkt_id, distance, duration, dt, geom, distance_offset, duration_offset ) VALUES (%d, %d, %f, %d, '%s', '%s', %f, %d);";

        for (Point point : points) {
            stringBuilder.append(
                    String.format(insertStatement,
                            point.getOrder(), workoutId, point.getDistanceFromPrevious(),
                            point.getDurationFromPrevious(), point.getTimeCaptured(), new PGgeometry(point.getPoint()),
                            point.getDistanceFromOffset(), point.getDurationFromOffset()));
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    void stepByStep(String dir) {
        outNameWrkt = String.format(OUTPUT_NAME_WRKT, fileOrderPoints);
        //outNameLap = String.format(OUTPUT_NAME_LAP, fileOrderPoints);
        outNamePoint = String.format(OUTPUT_NAME_POINT, fileOrderPoints);

        File[] dirs = new File(dir).listFiles();
        if (dirs != null) {
            for (File f : dirs) {
                doIt(f);
            }
        }
    }

    private void doIt(final File file) {
        if (file.isDirectory()) {
            File[] subDirs = file.listFiles();
            if (subDirs != null) {
                for (final File subFile : subDirs)
                    doIt(subFile);
            }
        } else {
            try {
                int workoutId = Integer.parseInt(file.getName().split(".j")[0]);
                final String data = new Scanner(new FileInputStream(file), "UTF-8")
                        .useDelimiter("\\A").next();

                final WorkoutDetail workout = JSONContentParser.parseWorkoutDetailUrl(data, workoutId);
                StringBuilder pointBuilder;
                StringBuilder workoutBuilder;

                if (workout != null) {
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
                    if (workout.getPoints() != null) {
                        workoutBuilder.append(workout.getPoints().size());
                    } else {
                        workoutBuilder.append(0);
                    }
                    workoutBuilder.append("\t");
                    if (workout.getLaps() != null) {
                        workoutBuilder.append(workout.getLaps().size());
                    } else {
                        workoutBuilder.append(0);
                    }
                    workoutBuilder.append("\t");
                    workoutBuilder.append(workout.getSport());
                    workoutBuilder.append("\t");
                    workoutBuilder.append(workout.getTimeZone());
                    workoutBuilder.append("\n");

                    write(workoutBuilder, outNameWrkt);


                    if (workout.getPoints() != null) {
                        pointBuilder = new StringBuilder();
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
                            processedPointCount++;
                        }
                        write(pointBuilder, outNamePoint);
                        if (processedPointCount > 5000000) {
                            processedPointCount = 0;
                            fileOrderPoints++;
                            outNamePoint = String.format(OUTPUT_NAME_POINT, fileOrderPoints);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void write(StringBuilder sb, String path) throws Exception {
        File file = new File(path);
        try (Writer writer = Channels.newWriter(new FileOutputStream(
                file.getAbsoluteFile(), true).getChannel(), "UTF-8")) {
            writer.append(sb);
        }
    }
}
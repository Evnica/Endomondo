package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.matching.TemporalAnalysis
 * Version: 0.1
 * Created on 5/28/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class TemporalAnalysisTest {

    private Connection connection;
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
    private static final DateTimeFormatter FORMATTER_SHORT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Before
    public void setUp() throws Exception {
        DbConnector.connectToDb();
        connection = DbConnector.getConnection();
    }

    @After
    public void tearDown() throws Exception {
    DbConnector.closeConnection();
    }

    @Test
    public void getWorkoutPartsInTracts() throws Exception {

        List<WorkoutDetail> workoutList = TemporalAnalysis.retrieveWorkoutsFromDb(connection);
        Map<String, List<WorkoutPartInTract>> parts;
        int i = 0;
        int total = workoutList.size();
        System.out.println(total + " workouts are to be processed");
        for (WorkoutDetail workoutDetail: workoutList)
        {
            parts = TemporalAnalysis
                    .getWorkoutPartsInTracts(connection, workoutDetail.getId(),
                            workoutDetail.getStartAt(), workoutDetail.getDuration());
            TemporalAnalysis.saveWrktPartsInFile(parts,
                    "C:\\Endoproject\\TRACT_STATS\\", "tract_parts.txt");
            i++;
            if (i%50 == 0)
            {
                System.out.println(i + " out of " + total + " workouts processed");
                System.out.println((total - i) + " to go");
                System.out.println("Processing...");
            }
        }
    }

    @Test
    public void getTimeIntervalFromTimeStamp() throws Exception {
        DateTime dt;
        try {
            dt = FORMATTER.parseDateTime("2016-01-05 13:52:46");
        } catch (Exception e) {
            dt = FORMATTER_SHORT.parseDateTime("2016-01-05 01:52:46");
        }

        TimeInterval ti = TemporalAnalysis.getTimeIntervalFromTimeStamp(dt);
        System.out.println(dt.toString(FORMATTER));
        System.out.println(ti);
        System.out.println(TimeInterval.toInt(ti));
    }

}
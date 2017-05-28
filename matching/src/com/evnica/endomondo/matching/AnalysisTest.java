package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import javafx.scene.AmbientLight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: AnalysisTest
 * Version: 0.1
 * Created on 5/22/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class AnalysisTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getTractWorkoutsFromDb() throws Exception {
        DbConnector.connectToDb();
        Map<String, List<TractWorkoutAthleteDetail>> tractDetail = Analysis.getTractWorkoutsFromDb(DbConnector.getConnection());
        System.out.println(tractDetail.size() + " retrieved");
        DbConnector.closeConnection();
    }

    @Test
    public void calcStats() throws Exception {
        Double[] nums = {null, 1.2, 2.2, 7.7, 1.5, 10.0, 11.2, 6.8, null,
                         100.8, 20.0, 38.1, 17.9, 300.3, 12.1, 14.5, 1.2, 0.5, 1.0, 0.8};
        Statistics s = Analysis.calcStatisticsForASet(Arrays.asList(nums));
        System.out.println(s);

        DbConnector.connectToDb();

        Analysis.insertStatisticsIntoDb(DbConnector.getConnection(), "12,kmdff", Statistics.getEmpty(), StatisticsTable.DISTANCE, null);

        DbConnector.closeConnection();

    }

    @Test
    public void calcStatisticsForTracts() throws Exception
    {
       DbConnector.connectToDb();
        Map<String, List<TractWorkoutAthleteDetail>> tractDetail = Analysis.getTractWorkoutsFromDb(DbConnector.getConnection());
        Map<String, List<TractAthleteDetail>> athInTract = Analysis.getTractAthletesFromDb(DbConnector.getConnection());

        List<TractStatistics> tractStats = Analysis.processMaps(tractDetail, athInTract);

        for(TractStatistics t: tractStats)
        {
            System.out.println("Inserting tract " + t.id);
            System.out.println(t.id);
            Analysis.insertTractStatisticsIntoDb(DbConnector.getConnection(), t);
        }

        DbConnector.closeConnection();


    }

}
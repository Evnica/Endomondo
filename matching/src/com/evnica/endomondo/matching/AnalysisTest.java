package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;

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
        ArrayList<TractWorkoutAthleteDetail> tractDetail = Analysis.getTractWorkoutsFromDb(DbConnector.getConnection());
        System.out.println(tractDetail.size() + " retrieved");
        DbConnector.closeConnection();
    }

}
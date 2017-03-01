package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.decode.JSONContentParser;
import com.evnica.endomondo.main.model.LapRepository;
import com.evnica.endomondo.main.model.TargetGeometry;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutJSON;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Scanner;

/**
 * Class: JSONContentParserTest
 * Version: 0.1
 * Created on 26.02.2017 with the help of IntelliJ IDEA (thanks!)
 * Author: Evnica
 * Description:
 */
public class JSONContentParserTest
{
    private String jsonContentVasli = null, jsonContentKari = null;
    private Workout workoutVasli, workoutKari;
    @Before
    public void setUp() throws Exception
    {
        InputStream fileStream = new FileInputStream( new File("testFiles/vasli1.json"));
        jsonContentVasli = new Scanner(fileStream, "UTF-8").useDelimiter("\\A").next();
        String dateTimeStr = "2015-06-10T20:40:06";
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").parseDateTime( dateTimeStr );
        workoutVasli = new Workout(540092518, 2, dateTime, 10097237);

        fileStream = new FileInputStream( (new File( "testFiles/kari1.json" )) );
        jsonContentKari = new Scanner(fileStream, "UTF-8").useDelimiter("\\A").next();
        dateTimeStr = "2016-05-15T19:12:03";
        dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").parseDateTime( dateTimeStr );
        workoutKari = new Workout(726778837, 2, dateTime, 23137052);

    }

    @Test
    public void parseWorkoutUrl() throws Exception
    {
        WorkoutJSON workoutJSON = JSONContentParser.parseWorkoutUrl( jsonContentVasli, workoutVasli, TargetGeometry.BOTH );
        //System.out.println(workoutJSON);

        //System.out.println('\n');

        //workoutJSON = JSONContentParser.parseWorkoutUrl( jsonContentKari, workoutKari, TargetGeometry.BOTH );
        //System.out.println(workoutJSON);

        DbConnector.connectToDb();
        LapRepository.setConnection( DbConnector.getConnection() );
        LapRepository.insert( workoutJSON, true, TargetGeometry.POINTS );

        DbConnector.closeConnection();
    }

}
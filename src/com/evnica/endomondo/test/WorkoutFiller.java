package com.evnica.endomondo.test;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.Workout;
import com.evnica.endomondo.main.model.WorkoutRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Project: Endomondo
 * Class: WorkoutFiller
 * Version: 0.1
 * Created on 3/1/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class WorkoutFiller {

    public static void main(String[] args) throws Exception
    {
        InputStream fileStream = new FileInputStream( new File("interimTables/workout-user-all.txt"));
        String userWorkoutPairs = new Scanner(fileStream, "UTF-8").useDelimiter("\\A").next();
        String[] individualPairs = userWorkoutPairs.split("\n");
        String[] entry;
        DateTime timestamp;
        int workoutId, userId, sport;
        DbConnector.connectToDb();
        WorkoutRepository.setConnection( DbConnector.getConnection() );


        for (String individualPair : individualPairs)
        {
            //id,sport,user_id,start_dt
            entry = individualPair.split(",");
            workoutId = Integer.parseInt(entry[0]);
            sport = Integer.parseInt(entry[1]);
            userId = Integer.parseInt(entry[2]);
            timestamp = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(DateTimeZone.UTC)
                    .parseDateTime(entry[3].substring(0, 19));


            WorkoutRepository.insert(new Workout(workoutId, sport, timestamp, userId));
        }


        DbConnector.closeConnection();

    }

}

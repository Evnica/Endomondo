package com.evnica.endomondo.matching;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.*;
import java.util.ArrayList;

/**
 * Project: Endomondo
 * Class: Analysis
 * Version: 0.1
 * Created on 5/22/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class Analysis
{
    private static final String GET_TRACT_WORKOUT_ATHLETE_DETAIL = "SELECT * FROM production.ctr_wrkt_ath_detail ORDER BY affgeoid;";

    static ArrayList<TractWorkoutAthleteDetail> getTractWorkoutsFromDb(Connection connection)
    {
        ArrayList<TractWorkoutAthleteDetail> tractWorkouts = new ArrayList<>();
        TractWorkoutAthleteDetail current;
        ResultSet resultSet = null;
        try
        {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(GET_TRACT_WORKOUT_ATHLETE_DETAIL);
            while (resultSet.next())
            {
                current = new TractWorkoutAthleteDetail();
                current.tractId = resultSet.getString("affgeoid");
                current.wrktId = resultSet.getInt("wrkt_id");
                current.athleteId = resultSet.getInt("athlete_id");
                current.distance = resultSet.getDouble("distance");
                current.duration = resultSet.getDouble("duration");
                String date = resultSet.getString("start_at");
                current.startAt = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
                current.sport = resultSet.getInt("sport");
                current.speed = resultSet.getDouble("speed");
                current.age = resultSet.getInt("age");
                current.gender = resultSet.getInt("gender");
                tractWorkouts.add(current);
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally
        {
            try {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    return tractWorkouts;
    }
}

package com.evnica.endomondo.matching;


import com.evnica.endomondo.dbhandling.main.Converter;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Project: Endomondo
 * Class: TemporalAnalysis
 * Version: 0.1
 * Created on 5/28/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class TemporalAnalysis
{

    private static final String GET_POINTS_WRKT = "SELECT id, affgeoid, distance_offset, duration_offset, dt FROM production.point_mia WHERE wrkt_id = ? ORDER BY id;";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
    private static final DateTimeFormatter FORMATTER_SHORT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SELECT_WRKT_DETAIL = "SELECT DISTINCT wrkt_id, duration, start_at FROM production.ctr_wrkt_ath_detail GROUP BY wrkt_id, duration, start_at ORDER BY wrkt_id";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0000");

    static List<WorkoutDetail> retrieveWorkoutsFromDb(Connection connection)
    {
        Statement statement = null;
        ResultSet resultSet = null;
        List<WorkoutDetail> workoutList = new ArrayList<>();
        WorkoutDetail currWorkout;
        String dt;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(SELECT_WRKT_DETAIL);
            while (resultSet.next())
            {
                currWorkout = new WorkoutDetail();
                currWorkout.setDuration(resultSet.getDouble("duration"));
                currWorkout.setId(resultSet.getInt("wrkt_id"));
                dt = resultSet.getString("start_at");
                try {
                    currWorkout.setStartAt(FORMATTER_SHORT.parseDateTime(dt));
                } catch (Exception e) {
                    currWorkout.setStartAt(FORMATTER.parseDateTime(dt));
                }
                workoutList.add(currWorkout);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (resultSet != null) try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return workoutList;
    }

    static Map<String, List<WorkoutPartInTract>> getWorkoutPartsInTracts(Connection connection, int wrktId,
                                                                         DateTime startAt, double duration)
    {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Map<String, List<WorkoutPartInTract>> workoutPartsMap = new HashMap<>();
        WorkoutPartInTract workoutPartInTract;
        TimeInterval firstTimeInterval = getTimeIntervalFromTimeStamp(startAt);
        boolean withinOneInterval = false;
        if (firstTimeInterval.equals(getTimeIntervalFromTimeStamp(startAt.plusSeconds((int)duration))))
        {
            withinOneInterval = true;
        }
        try
        {
            statement = connection.prepareStatement(GET_POINTS_WRKT);
            statement.setInt(1, wrktId);
            resultSet = statement.executeQuery();
            String lastAffGeoId = "";
            String currentAffGeoId = "";
            double firstDistance = 0,
                    lastDistanceInTract = 0,
                    lastDistance = 0,
                    firstDuration = 0,
                    lastDurationInTract = 0,
                    lastDuration = 0;
            TimeInterval lastTimeIntervalInTract = null,
                    lastTimeInterval = null;
            if (!withinOneInterval)
            {
                lastTimeIntervalInTract = firstTimeInterval;
            }
            while (resultSet.next())
            {
                lastDistance = resultSet.getDouble("distance_offset");
                lastDuration = resultSet.getDouble("duration_offset");
                String dt = resultSet.getString("dt");
                int dayOfWeek = startAt.getDayOfWeek();
                DateTime timestamp;
                if (!withinOneInterval)
                {
                    try
                    {
                        timestamp = FORMATTER_SHORT.parseDateTime(dt);
                        lastTimeInterval = getTimeIntervalFromTimeStamp(timestamp);
                    }
                    catch (Exception e)
                    {
                        timestamp = FORMATTER.parseDateTime(dt);
                        lastTimeInterval = getTimeIntervalFromTimeStamp(timestamp);
                    }
                    dayOfWeek = timestamp.getDayOfWeek();
                }
                currentAffGeoId = resultSet.getString("affgeoid");
                if (! currentAffGeoId.equals(lastAffGeoId))
                {
                    workoutPartInTract = new WorkoutPartInTract();
                    workoutPartInTract.wrktId = wrktId;

                    if (withinOneInterval)
                    {
                        workoutPartInTract.timeInterval = firstTimeInterval;
                    }
                    else
                    {
                        workoutPartInTract.timeInterval = lastTimeIntervalInTract;
                        firstTimeInterval = lastTimeInterval;
                    }
                    workoutPartInTract.isWeekend = (dayOfWeek == 6 || dayOfWeek == 7);
                    workoutPartInTract.distance = lastDistanceInTract - firstDistance;
                    workoutPartInTract.duration = lastDurationInTract - firstDuration;

                    if (workoutPartsMap.containsKey(lastAffGeoId))
                    {
                        workoutPartsMap.get(lastAffGeoId).add(workoutPartInTract);
                    }
                    else
                    {
                        List<WorkoutPartInTract> parts = new ArrayList<>();
                        parts.add(workoutPartInTract);
                        workoutPartsMap.put(lastAffGeoId, parts);
                    }

                    lastAffGeoId = currentAffGeoId;
                    firstDistance = lastDistance;
                    firstDuration = lastDuration;
                }
                else
                {
                    lastDistanceInTract = lastDistance;
                    lastDurationInTract = lastDuration;

                    if (!withinOneInterval)
                    {
                        lastTimeIntervalInTract = lastTimeInterval;
                    }
                }
            }
            workoutPartInTract  = new WorkoutPartInTract();
            workoutPartInTract.wrktId = wrktId;
            workoutPartInTract.distance = lastDistance - firstDistance;
            workoutPartInTract.duration = lastDuration - firstDuration;
            if (withinOneInterval)
            {
                workoutPartInTract.timeInterval = firstTimeInterval;
            }
            else
            {
                workoutPartInTract.timeInterval = lastTimeInterval;
            }

            if (workoutPartsMap.containsKey(currentAffGeoId))
            {
                workoutPartsMap.get(currentAffGeoId).add(workoutPartInTract);
            }
            else
            {
                workoutPartsMap.put(currentAffGeoId, Collections.singletonList(workoutPartInTract));
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
         if (statement != null)
         {
             try {
                 statement.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         if (resultSet != null)
         {
             try {
                 resultSet.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
        }
        workoutPartsMap.remove("");
        return workoutPartsMap;
    }

    static TimeInterval getTimeIntervalFromTimeStamp(DateTime dateTime)
    {
        TimeInterval interval;
        int hourOfTheDay = dateTime.getHourOfDay();
        if (hourOfTheDay >= 0 && hourOfTheDay < 5)
        {
            interval = TimeInterval.ZERO;
        }
        else if (hourOfTheDay >= 5 && hourOfTheDay < 10)
        {
            interval = TimeInterval.ONE;
        }
        else if (hourOfTheDay >= 10 && hourOfTheDay < 15)
        {
            interval = TimeInterval.TWO;
        }
        else if (hourOfTheDay >= 15 && hourOfTheDay < 20)
        {
            interval = TimeInterval.THREE;
        }
        else
        {
            interval = TimeInterval.FOUR;
        }
        return interval;
    }

    static boolean saveWrktPartsInFile(Map<String, List<WorkoutPartInTract>> wrktParts, String dir, String filename)
    {
        StringBuilder stringBuilder = new StringBuilder();
        List<WorkoutPartInTract> partsInTract;
        int i = 0;
        for (String tractId: wrktParts.keySet())
        {
            partsInTract = wrktParts.get(tractId);
            for (WorkoutPartInTract part: partsInTract)
            {
                if (part.distance > 0.01)
                {
                    stringBuilder.append(tractId);
                    stringBuilder.append("\t");
                    stringBuilder.append(DECIMAL_FORMAT.format(part.distance));
                    stringBuilder.append("\t");
                    stringBuilder.append(part.duration);
                    stringBuilder.append("\t");
                    stringBuilder.append(TimeInterval.toInt(part.timeInterval));
                    stringBuilder.append("\t");
                    stringBuilder.append(part.isWeekend);
                    stringBuilder.append("\t");
                    stringBuilder.append(part.wrktId);
                    stringBuilder.append("\t");
                    stringBuilder.append(i);
                    stringBuilder.append("\n");
                    i++;
                }
            }
        }
        try {
            Converter.write(stringBuilder.toString(), dir, filename);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

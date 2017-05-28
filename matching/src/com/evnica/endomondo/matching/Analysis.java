package com.evnica.endomondo.matching;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Project: Endomondo
 * Class: Analysis
 * Version: 0.1
 * Created on 5/22/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
class Analysis
{
    private static final String GET_TRACT_WORKOUT_ATHLETE_DETAIL =
            "SELECT * FROM production.florida_wrkt_athl ORDER BY gid";
    //"SELECT * FROM production.ctr_wrkt_ath_detail ORDER BY affgeoid;";
    private static final String GET_TRACT_ATHLETE_DETAIL = "SELECT * FROM production.florida_ath_county ORDER BY gid";
    //"SELECT * FROM production.ctr_ath_detail ORDER BY affgeoid;";
    private static final String INSERT_STATS =
            "INSERT INTO production.%s (minimum, maximum, median, mean, trimmed_mean, st_dev, lower_quart," +
            " upper_quart, total_cnt, outlier_cnt, sample_30plus, id, last_nonoutlier) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_AGE_STATS =
       "INSERT INTO production.florida_age_gender (minimum, maximum, median, mean, trimmed_mean, st_dev, lower_quart, " +
       "upper_quart, total_cnt, outlier_cnt, sample_30plus, id, last_nonoutlier, male_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    static Map<String, List<TractWorkoutAthleteDetail>> getTractWorkoutsFromDb(Connection connection)
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
                current.tractId = resultSet.getString("gid");//("affgeoid");
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
        return tractWorkouts.stream()
                .collect(Collectors.groupingBy(TractWorkoutAthleteDetail::getTractId));
    }

    static Map<String, List<TractAthleteDetail>> getTractAthletesFromDb(Connection connection)
    {
        ArrayList<TractAthleteDetail> tractAthletes = new ArrayList<>();
        TractAthleteDetail current;
        ResultSet resultSet = null;
        try
        {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(GET_TRACT_ATHLETE_DETAIL);
            while (resultSet.next())
            {
                try {
                    current = new TractAthleteDetail();
                    current.tractId = resultSet.getString("gid");//("affgeoid");
                    current.id = resultSet.getInt("athlete_id");
                    int ageInDb = resultSet.getInt("age");
                    if (ageInDb > 0)
                    {
                        current.age = ageInDb;
                    }
                    else
                    {
                        current.age = null;
                    }
                    current.gender = resultSet.getInt("gender");
                    current.country = resultSet.getString("country");
                    tractAthletes.add(current);
                } catch (NullPointerException e) {
                    System.err.println(e.getMessage());
                }
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
        return tractAthletes.stream()
                .collect(Collectors.groupingBy(TractAthleteDetail::getTractId));
    }

    private static TractStatistics calcStatisticsForTract(String id, List<TractWorkoutAthleteDetail> workoutsInTract,
                                           List<TractAthleteDetail> athletesInTract)
    {
        TractStatistics statistics = new TractStatistics();
        statistics.id = id;

        ArrayList<Double> distances = new ArrayList<>();
        ArrayList<Double> durations = new ArrayList<>();
        ArrayList<Double> speeds = new ArrayList<>();
        ArrayList<Double> ageValues = new ArrayList<>();
        for (TractWorkoutAthleteDetail entry: workoutsInTract)
        {
            distances.add(entry.distance);
            durations.add(entry.duration / 60);
            speeds.add(entry.speed);
        }

        if (distances.size() >=5)
        {
            Statistics distStat = calcStatisticsForASet(distances);
            if (distStat != null)
            {
                statistics.distanceStatistics = distStat;
            }
        }
        else
        {
            statistics.distanceStatistics.totalCount = distances.size();
        }
        if (durations.size() >= 5)
        {
            Statistics durStat = calcStatisticsForASet(durations);
            if (durStat != null) {
                statistics.durationStatistics = durStat;
            }
        }
        else
        {
            statistics.durationStatistics.totalCount = durations.size();
        }
        if (speeds.size() >= 5)
        {
            Statistics speedStat = calcStatisticsForASet(speeds);
            if (speedStat != null) {
                statistics.speedStatistics = speedStat;
            }
        }
        else
        {
            statistics.speedStatistics.totalCount = speeds.size();
        }


        if (athletesInTract.size() >= 5)
        {
            int maleCount = 0, femaleCount = 0;

            for (TractAthleteDetail entry: athletesInTract)
            {
                if (entry.age != null)
                {
                    ageValues.add(entry.age * 1.0);
                }
                if (entry.gender == 0)
                {
                    maleCount++;
                }
                else if (entry.gender == 1)
                {
                    femaleCount++;
                }
            }

            if (ageValues.size() >= 5)
            {
                statistics.ageStatistics = calcStatisticsForASet(ageValues);
            }
            else
            {
                statistics.ageStatistics.totalCount = ageValues.size();
            }

            if (maleCount + femaleCount >= 5)
            {
                statistics.percentageOfMale = (maleCount * 1.0) / ((maleCount + femaleCount) *1.0);
            }
            else
            {
                statistics.percentageOfMale = -1;
            }
        }
        else
        {
            statistics.ageStatistics.totalCount = athletesInTract.size();
        }
        return statistics;
    }

    static Statistics calcStatisticsForASet(List<Double> values)
    {
        DescriptiveStatistics statsApache = new DescriptiveStatistics();
        for (Double value: values)
        {
            if (value != null && value != 0)
            {
                statsApache.addValue(value);
            }
        }
        Statistics statistics = null;

        if (statsApache.getValues().length >= 5)
        {
            statistics = new Statistics();

            if (statsApache.getValues().length > 29)
            {
                statistics.sample30orMore = true;
            }

            statistics.min = statsApache.getMin();
            statistics.max = statsApache.getMax();
            statistics.mean = statsApache.getMean();
            statistics.median = statsApache.getPercentile(50);
            statistics.stDev = statsApache.getStandardDeviation();
            statistics.lowerQuartile = statsApache.getPercentile(25);
            statistics.upperQuartile = statsApache.getPercentile(75);
            double iqr = statistics.upperQuartile - statistics.lowerQuartile;

            double mildOutlierStart = statistics.upperQuartile + 1.5 * iqr;

            int outlierCount = 0;
            double[] sortedValues = statsApache.getSortedValues();

            for (int i = sortedValues.length - 1; i >= 0; i--)
            {
                if (sortedValues[i] > mildOutlierStart)
                {
                    outlierCount++;
                }
                else
                {
                    statistics.lastNonOutlier = sortedValues[i];
                    break;
                }
            }
            statistics.outlierCount = outlierCount;
            statistics.totalCount = sortedValues.length;

            if (outlierCount == 0)
            {
                statistics.trimmedMean = statistics.mean;
            }
            else
            {
                if ((outlierCount * 2 + 5) < sortedValues.length)
                    {
                    double trimmedSum = 0;
                    for (int i = outlierCount; i < sortedValues.length - outlierCount; i++)
                    {
                        double curr = sortedValues[i];
                        trimmedSum += curr;
                    }
                    statistics.trimmedMean = trimmedSum / (sortedValues.length - outlierCount * 2.0);
                }
                else
                {
                    statistics.trimmedMean = -1.0;
                }
            }
        }

        return  statistics;

    }

    static void insertStatisticsIntoDb(Connection connection, String id, Statistics s, StatisticsTable table, Double malePercentage)
    {
        if (s != null)
        {
            PreparedStatement statement = null;
            String query;
            if (table != StatisticsTable.AGE) {
                query = String.format(INSERT_STATS, table.toString());
            }
            else
            {
                query = INSERT_AGE_STATS;
            }
            try
            {
                statement = connection.prepareStatement(query);
                statement.setDouble(1, s.min);
                statement.setDouble(2, s.max);
                statement.setDouble(3, s.median);
                statement.setDouble(4, s.mean);
                statement.setDouble(5, s.trimmedMean);
                statement.setDouble(6, s.stDev);
                statement.setDouble(7, s.lowerQuartile);
                statement.setDouble(8, s.upperQuartile);
                statement.setDouble(9, s.totalCount);
                statement.setDouble(10, s.outlierCount);
                statement.setBoolean(11, s.sample30orMore);
                statement.setString(12, id);
                statement.setDouble(13, s.lastNonOutlier);
                if (table == StatisticsTable.AGE)
                {
                   statement.setDouble(14, malePercentage);
                }
                statement.executeUpdate();
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
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    static boolean insertTractStatisticsIntoDb(Connection connection, TractStatistics ts)
    {
        boolean success = false;
        try
        {
            connection.setAutoCommit(false);

            insertStatisticsIntoDb(connection, ts.id, ts.distanceStatistics, StatisticsTable.DISTANCE, null);
            insertStatisticsIntoDb(connection, ts.id, ts.durationStatistics, StatisticsTable.DURATION, null);
            insertStatisticsIntoDb(connection, ts.id, ts.speedStatistics, StatisticsTable.SPEED, null);
            insertStatisticsIntoDb(connection, ts.id, ts.ageStatistics, StatisticsTable.AGE, ts.percentageOfMale);

            connection.commit();
            connection.setAutoCommit(true);

            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return success;
    }

    static List<TractStatistics> processMaps(Map<String, List<TractWorkoutAthleteDetail>> tractWrktData,
                               Map<String, List<TractAthleteDetail>> tractAthData )
    {

        Set<String> tracts = tractWrktData.keySet();
        List<TractWorkoutAthleteDetail> currentWrktData;
        List<TractAthleteDetail> currentAthData;
        List<TractStatistics> allTractStats = new ArrayList<>();
        for (String tractId: tracts)
        {
            currentWrktData = tractWrktData.get(tractId);
            currentAthData = tractAthData.get(tractId);
            allTractStats.add(calcStatisticsForTract(tractId, currentWrktData, currentAthData));
        }


        return allTractStats;
    }

}

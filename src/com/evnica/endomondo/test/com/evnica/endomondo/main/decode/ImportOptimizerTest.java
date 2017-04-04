package com.evnica.endomondo.main.decode;

import com.evnica.endomondo.main.connect.DbConnector;
import com.evnica.endomondo.main.model.WorkoutDetail;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Scanner;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.main.decode.ImportOptimizer
 * Version: 0.1
 * Created on 3/21/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class ImportOptimizerTest {
    private WorkoutDetail workout = null;

    @Before
    public void setUp() throws Exception
    {
        DbConnector.connectToDb();
        int workoutId = 540092518;
        String jsonContent = new Scanner
                (new FileInputStream
                ("C:\\Users\\d.strelnikova\\IdeaProjects\\Endomondo\\testFiles\\short_points.json"),
                "UTF-8")
                .useDelimiter("\\A").next();
        workout = JSONContentParser.parseWorkoutDetail(jsonContent, workoutId, false);
    }

    @Test
    public void composeInsertPointsStringTest() throws Exception
    {
        if (workout != null)
        {
            System.out.println(ImportOptimizer.composeInsertPointsString(workout.getPoints(), workout.getId()));
        }
        DbConnector.closeConnection();
    }

    @Test
    public void fromJSONtoDbFilesTest()
    {
        new ImportOptimizer().stepByStep("T:\\geomatics\\Dariia\\complex retrieval\\workout_us\\----19850000-19950000");
    }

}
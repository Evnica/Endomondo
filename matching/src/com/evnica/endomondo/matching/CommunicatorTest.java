package com.evnica.endomondo.matching;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.matching.Communicator
 * Version: 0.1
 * Created on 4/18/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class CommunicatorTest
{
    @Before
    public void setUp() throws Exception
    {
        assertTrue(Communicator.initialize());
    }

    @After
    public void tearDown() throws Exception
    {
        assertTrue(Communicator.terminate());
    }

    @Test
    public void selectWrktPointsIntoTable() throws Exception
    {
        assertTrue(Communicator.selectWrktPointsIntoTable(678690721));
    }

    @Test
    public void calculateExtent()
    {
        System.out.println(Communicator.calculateExtent());
    }

    @Test
    public void createNetworkViewForExtent()
    {
        String extent = Communicator.calculateExtent();
        assertTrue(Communicator.createNetworkViewForExtent(extent));
    }

    @Test
    public void intersectBuffersAndMore()
    {
        assertTrue(Communicator.fillInWorkoutDetail(678690721));

        ArrayList<SegmentPairedWithPoint> segments = Communicator.intersectBuffers();
        System.out.println("Segments intersected:");
        segments.forEach(System.out::println);

        Route probableRoute = Communicator.restoreTrip(segments);

        probableRoute.roadSegments.forEach(System.out::println);

        Communicator.insertRouteIntoDb(probableRoute, Communicator.currentWorkout.getId(),
                Communicator.currentWorkout.getUserId());

        System.out.println("Can you believe it?");

    }

}
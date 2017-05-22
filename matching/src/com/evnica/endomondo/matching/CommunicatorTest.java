package com.evnica.endomondo.matching;

import com.evnica.endomondo.main.connect.DbConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;

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
        assertTrue(Communicator.selectWrktPointsIntoTable(691595848));
        String extent = Communicator.calculateExtent();
        assertTrue(Communicator.createNetworkViewForExtent(extent));
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
    public void intersectBuffersAndMore() throws Exception
    {
        int wrktId = 686098981;
        assertTrue(Communicator.selectWrktPointsIntoTable(wrktId));
        System.out.println("Points inserted");
        String extent = Communicator.calculateExtent();
        assertTrue(Communicator.createNetworkViewForExtent(extent));

        assertTrue(Communicator.fillInWorkoutDetail(wrktId));

        ArrayList<SegmentPairedWithPoint> segments = Communicator.intersectBuffers();
        //System.out.println("Segments intersected:");
        //segments.forEach(System.out::println);

        Route probableRoute = Communicator.restoreTrip(segments);
        probableRoute.id = Communicator.currentWorkout.getId();
        System.out.println(probableRoute);

        //probableRoute.roadSegments.forEach(System.out::println);

        Communicator.insertRouteIntoDb(probableRoute);
        Communicator.insertRouteSegmentsIntoDb(probableRoute, Communicator.currentWorkout.getUserId());

        DbConnector.closeConnection();

        System.out.println("Can you believe it?");

    }

    @Test
    public void findLoops()
    {
        LinkedList<SegmentPairedWithPoint> probableList = new LinkedList<>();
        probableList.add(new SegmentPairedWithPoint(0));
        probableList.add(new SegmentPairedWithPoint(1));
        probableList.add(new SegmentPairedWithPoint(2));
        probableList.add(new SegmentPairedWithPoint(3));
        probableList.add(new SegmentPairedWithPoint(4));
        probableList.add(new SegmentPairedWithPoint(5));
        probableList.add(new SegmentPairedWithPoint(2));
        probableList.add(new SegmentPairedWithPoint(7));
        probableList.add(new SegmentPairedWithPoint(8));
        probableList.add(new SegmentPairedWithPoint(9));
        probableList.add(new SegmentPairedWithPoint(10));
        probableList.add(new SegmentPairedWithPoint(11));
        probableList.add(new SegmentPairedWithPoint(12));
        probableList.add(new SegmentPairedWithPoint(13));
        probableList.add(new SegmentPairedWithPoint(8));
        probableList.add(new SegmentPairedWithPoint(15));
        probableList.add(new SegmentPairedWithPoint(16));
        probableList.add(new SegmentPairedWithPoint(17));
        probableList.add(new SegmentPairedWithPoint(18));
        probableList.add(new SegmentPairedWithPoint(19));
        probableList.add(new SegmentPairedWithPoint(20));
        probableList.add(new SegmentPairedWithPoint(16));
        probableList.add(new SegmentPairedWithPoint(17));
        probableList.add(new SegmentPairedWithPoint(23));
        probableList.add(new SegmentPairedWithPoint(24));

        Route r = new Route();

        Communicator.determineLoops(probableList, r);
        System.out.println(r);
        for (SegmentPairedWithPoint s: r.roadSegments)
        {
            System.out.print(s.segmentId + " ");
        }
    }

}
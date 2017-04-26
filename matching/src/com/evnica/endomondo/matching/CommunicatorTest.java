package com.evnica.endomondo.matching;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        assertTrue(Communicator.selectWrktPointsIntoTable(664214853));
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

}
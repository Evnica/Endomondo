package com.evnica.endomondo.dbhandling.main;

import com.evnica.endomondo.main.connect.DbConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Project: Endomondo
 * Class: com.evnica.endomondo.dbhandling.main.BulkLoader
 * Version: 0.1
 * Created on 4/4/2017 with the help of IntelliJ IDEA (thanks!)
 * Author: DS
 * Description:
 */
public class BulkLoaderTest {
    @Before
    public void setUp() throws Exception {
        DbConnector.connectToDb();
    }

    @After
    public void tearDown() throws Exception {
        DbConnector.closeConnection();
    }

    @Test
    public void loadInDb() throws Exception {
        BulkLoader.loadInDb("C:\\Endoproject\\toCopy\\(...-000)");
    }

}
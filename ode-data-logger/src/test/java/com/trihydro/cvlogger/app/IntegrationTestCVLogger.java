package com.trihydro.cvlogger.app;

import com.trihydro.cvlogger.app.helpers.SqlConnection;
import java.sql.Connection;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;


/**
 * Integration test for CV Logger
 */
@Ignore
public class IntegrationTestCVLogger
{
    @Test 
    public void TestMakeJDBCConnection(){
        Connection connection =  SqlConnection.makeJDBCConnection();
        assertNotNull(connection);
    }
}

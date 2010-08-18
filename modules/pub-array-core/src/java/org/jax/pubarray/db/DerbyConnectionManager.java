/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.pubarray.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A connection factory for derby
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class DerbyConnectionManager implements ConnectionManager
{
    private static final Logger LOG = Logger.getLogger(
            DerbyConnectionManager.class.getName());
    
    private static final String DATABASE_URL_PREFIX =
        "jdbc:derby:directory:";
    
    private static final String DRIVER =
        "org.apache.derby.jdbc.EmbeddedDriver";
    
    private String databaseUrl;
    
    /**
     * Constructor
     */
    public DerbyConnectionManager()
    {
    }
    
    /**
     * Constructor
     * @param databaseDirectory
     *          the database dir
     */
    public DerbyConnectionManager(String databaseDirectory)
    {
        try
        {
            Class.forName(DRIVER);
            this.databaseUrl = DATABASE_URL_PREFIX + databaseDirectory;
        }
        catch(ClassNotFoundException ex)
        {
            throw new RuntimeException(
                    "Failed to load Derby Driver: " + DRIVER,
                    ex);
        }
    }
    
    /**
     * Create a new connection
     * @return
     *          the connection
     * @throws SQLException
     *          if there's some problem creating the connection
     */
    public Connection createConnection() throws SQLException
    {
        String databaseUrl = this.databaseUrl;
        if(databaseUrl == null)
        {
            throw new NullPointerException("Database URL can't be null");
        }
        else
        {
            LOG.info("Initializing DB with URL: " + this.databaseUrl);
            Connection connection = DriverManager.getConnection(
                    this.databaseUrl + ";create=true");
            connection.setAutoCommit(false);
            return connection;
        }
    }
    
    /**
     * Shutdown the database driver
     * @param connection
     *          the connection to use when shutting down
     */
    public void shutdownDatabase(Connection connection)
    {
        try
        {
            DriverManager.getConnection(
                    this.databaseUrl + ";shutdown=true");
        }
        catch(SQLException ex)
        {
            // Don't care
            // Shutdown commands always raise SQLExceptions (not sure why)
            // see http://db.apache.org/derby/docs/dev/devguide/tdevdvlp20349.html
        }
        catch (Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Datatbase connection did not shut down normally",
                    ex);
        }
    }
}

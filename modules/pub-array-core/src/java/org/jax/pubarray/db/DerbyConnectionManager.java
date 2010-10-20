/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
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

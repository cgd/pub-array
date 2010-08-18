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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.util.io.FileUtilities;

/**
 * A connection factory for HSQLDB
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class HSQLDBConnectionManager implements ConnectionManager
{
    private static final Logger LOG = Logger.getLogger(
            HSQLDBConnectionManager.class.getName());
    
    private static final String DATABASE_FILE_URL_PREFIX = "jdbc:hsqldb:file:";
    private static final String DATABASE_FILE_URL_SUFFIX = ";hsqldb.default_table_type=cached";
    private static final String DATABASE_CLASSPATH_URL_PREFIX = "jdbc:hsqldb:res:";
    
    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    
    private final String databaseUrl;
    private final boolean useClasspath;
    private final String databasePath;
    
    /**
     * Constructor
     * @param databasePath
     *          the database path
     * @param useClasspath
     *          if true use the classpath otherwise use the file system
     */
    public HSQLDBConnectionManager(
            String databasePath,
            boolean useClasspath)
    {
        this.useClasspath = useClasspath;
        this.databasePath = databasePath;
        try
        {
            Class.forName(DRIVER);
            
            if(useClasspath)
            {
                this.databaseUrl =
                    DATABASE_CLASSPATH_URL_PREFIX + databasePath;
            }
            else
            {
                this.databaseUrl =
                    DATABASE_FILE_URL_PREFIX + databasePath + DATABASE_FILE_URL_SUFFIX;
            }
        }
        catch(ClassNotFoundException ex)
        {
            throw new RuntimeException(
                    "Failed to load Driver: " + DRIVER,
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
            try
            {
                LOG.info("Initializing DB with URL: " + this.databaseUrl);
                Connection connection = DriverManager.getConnection(
                        this.databaseUrl);
                connection.setAutoCommit(false);
                return connection;
            }
            catch(SQLException ex)
            {
                LOG.warning(
                        "Failed to initialize database on classpath. " +
                        "Attempting to fall back on unpacking the database " +
                        "to a temporary directory.");
                if(this.useClasspath)
                {
                    return this.fallbackOnFileUrlHack();
                }
                else
                {
                    // there is nothing we can try. just re-throw the exception
                    throw ex;
                }
            }
        }
    }
    
    // TODO this whole function is a hack to get around the glassfish
    //      class-loader behavior. It should not be needed any more when
    //      http://sourceforge.net/projects/hsqldb/forums/forum/73673/topic/3510142
    //      makes it into hsqldb release code
    private Connection fallbackOnFileUrlHack() throws SQLException
    {
        final String hackDBName = "dbhack";
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            File tempDir = FileUtilities.createTempDir();
            LOG.info("Unpacking database to: " + tempDir.getAbsolutePath());
            
            String[] suffixes =
                new String[] {"backup", "data", "properties", "script"};
            
            for(String currSuffix: suffixes)
            {
                String currResource = this.databasePath + "." + currSuffix;
                InputStream sourceStream = cl.getResourceAsStream(currResource);
                
                if(sourceStream != null)
                {
                    sourceStream = new BufferedInputStream(sourceStream);
                    File sinkFile = new File(tempDir, hackDBName + "." + currSuffix);
                    BufferedOutputStream sinkStream = new BufferedOutputStream(
                            new FileOutputStream(sinkFile));
                    
                    FileUtilities.writeSourceToSink(sourceStream, sinkStream);
                    sinkStream.flush();
                    sinkStream.close();
                    
                    LOG.info(
                            "wrote resource " + currResource +
                            " to file " + sinkFile.getAbsolutePath());
                }
                else
                {
                    LOG.info("Resource not found: " + currResource);
                }
            }
            
            Connection connection = DriverManager.getConnection(
                    DATABASE_FILE_URL_PREFIX +
                    new File(tempDir, hackDBName).getAbsolutePath() +
                    DATABASE_FILE_URL_SUFFIX);
            connection.setAutoCommit(true);
            return connection;
        }
        catch(IOException ex)
        {
            LOG.log(Level.SEVERE, "IO exception on fallback hack", ex);
            throw new SQLException(ex.getMessage());
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
            connection.createStatement().execute("SHUTDOWN");
        }
        catch (Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Datatbase connection did not shut down normally",
                    ex);
        }
    }
}

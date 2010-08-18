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

package org.jax.pubarray.server;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jax.pubarray.db.ConnectionManager;
import org.jax.pubarray.db.ExperimentMetadata;
import org.jax.pubarray.db.PersistenceManager;

/**
 * Creates a database connection and injects it into the servlet context
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class DatabaseServletContextListener implements ServletContextListener
{
    private static final Logger LOG = Logger.getLogger(
            DatabaseServletContextListener.class.getName());
    
    private static final String DATABASE_PATH_ATTRIBUTE =
        DatabaseServletContextListener.class.getName() + ".databasePath";
    
    private ConnectionManager connectionManager;
    
    /**
     * The servlet context attribute used for the connection
     */
    public static final String CONNECTION_ATTRIBUTE =
        DatabaseServletContextListener.class.getName() + ".connection";
    
    /**
     * The servlet context attribute used for the experiment metadata
     */
    public static final String EXPERIMENT_METADATA_ATTRIBUTE = "experimentMetadata";
    
    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        LOG.info("initializing");
        
        try
        {
            ServletContext servletContext = sce.getServletContext();
            String databasePath =
                servletContext.getInitParameter(DATABASE_PATH_ATTRIBUTE);
            
            this.connectionManager = this.createConnectionManager(databasePath);
            
            LOG.info("using database path: " + databasePath);
            
            Connection connection = this.connectionManager.createConnection();
            servletContext.setAttribute(
                    CONNECTION_ATTRIBUTE,
                    connection);
            
            PersistenceManager persistenceManager = new PersistenceManager();
            ExperimentMetadata experimentMetadata =
                persistenceManager.getExperimentMetadata(connection);
            servletContext.setAttribute(
                    EXPERIMENT_METADATA_ATTRIBUTE,
                    experimentMetadata);
            
            
            LOG.info("done initializing");
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Failed to initialize connection " +
                    "because of exception",
                    ex);
        }
    }
    
    /**
     * Creates a new connection manager with the given database path
     * @param databasePath
     *          the path
     * @return
     *          the connection manager
     */
    protected abstract ConnectionManager createConnectionManager(String databasePath);

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        LOG.info("cleaning up");
        
        final ServletContext servletContext = sce.getServletContext();
        this.connectionManager.shutdownDatabase(
                (Connection)servletContext.getAttribute(CONNECTION_ATTRIBUTE));
        
        LOG.info("done cleaning up");
    }
}

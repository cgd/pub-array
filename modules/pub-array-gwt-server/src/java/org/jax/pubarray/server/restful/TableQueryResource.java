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

package org.jax.pubarray.server.restful;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.server.DatabaseServletContextListener;
import org.jax.pubarray.server.gwtquery.QueryServiceImpl;

/**
 * A resource for performing table queries
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
@Path("/query-results")
public class TableQueryResource
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            TableQueryResource.class.getName());
    
    @Context
    private HttpServletRequest request;

    @Context
    private ServletContext context;
    
    /**
     * Get a CSV of the latest query
     * @return
     *      the streaming output used to generate the CSV file
     */
    @GET
    @Path("/latest-query.csv")
    @Produces("text/csv")
    public Response getLatestQueryCsv()
    {
        try
        {
            LOG.log(Level.INFO, "serving a request for CSV query results");
            Query latestQuery = (Query)this.request.getSession().getAttribute(
                    QueryServiceImpl.LATEST_QUERY_ATTRIBUTE);
            QueryToCsvStreamingOutput csvOutput = new QueryToCsvStreamingOutput(
                    latestQuery,
                    this.getConnection());
            
            CacheControl noCache = new CacheControl();
            noCache.setNoCache(true);
            
            return Response.ok(csvOutput, "text/csv").cacheControl(noCache).build();
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to create IBS csv file",
                    ex);
            return null;
        }
    }
    
    /**
     * A helper function to get the DB connection
     * @return
     *          the DB connection
     */
    private Connection getConnection()
    {
        return (Connection)this.context.getAttribute(
                DatabaseServletContextListener.CONNECTION_ATTRIBUTE);
    }
}

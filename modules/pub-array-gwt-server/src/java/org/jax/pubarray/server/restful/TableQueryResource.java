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

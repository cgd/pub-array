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

package org.jax.pubarray.server.gwtquery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.pubarray.db.PersistenceManager;
import org.jax.pubarray.gwtcommon.client.GeneImageMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableData;
import org.jax.pubarray.gwtcommon.client.TableMetadata;
import org.jax.pubarray.gwtqueryapp.client.QueryService;
import org.jax.pubarray.server.DatabaseServletContextListener;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server-side implementation of the {@link QueryService} interface
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QueryServiceImpl
extends RemoteServiceServlet
implements QueryService
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3882950650161028053L;
    
    public static final String LATEST_QUERY_ATTRIBUTE = "LATEST_QUERY";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QueryServiceImpl.class.getName());

    private final PersistenceManager persistenceManager =
        new PersistenceManager();

    /**
     * {@inheritDoc}
     */
    public TableData<String> executeQuery(
            Query query,
            int rowOffset,
            int rowCount)
    {
        try
        {
            if(query.getFilters().length > Query.MAX_PERMITTED_FILTER_COUNT ||
               query.getTermsOfInterest().length > Query.MAX_PERMITTED_TERMS ||
               query.getTableCount() > Query.MAX_PERMITTED_TABLE_COUNT)
            {
                LOG.severe("Rejecting query that is beyond resource limits");
                return null;
            }
            else
            {
                // before we do any work save the query as the latest
                this.getThreadLocalRequest().getSession().setAttribute(
                        LATEST_QUERY_ATTRIBUTE,
                        query);
                
                TableData<Object> queryResult = this.persistenceManager.executeQuery(
                        this.getConnection(),
                        query,
                        rowOffset,
                        rowCount);
                return queryResult.toStringData();
            }
        }
        catch(SQLException ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            //      exception fly
            LOG.log(Level.SEVERE,
                    "Failed to execute data query",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public TableMetadata getDataTableMetadata()
    {
        try
        {
            Connection connection = this.getConnection();
            return this.persistenceManager.getDataTableMetadata(connection);
        }
        catch(Exception ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            //      exception fly
            LOG.log(Level.SEVERE,
                    "Failed to get data and annotation metadata",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public TableMetadata[] getAnnotationMetadata(String category)
    {
        try
        {
            Connection connection = this.getConnection();
            List<TableMetadata> metadataList =
                this.persistenceManager.getAnnotationTableMetadata(
                        connection,
                        category,
                        false);
            
            return metadataList.toArray(new TableMetadata[metadataList.size()]);
        }
        catch(Exception ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            //      exception fly
            LOG.log(Level.SEVERE,
                    "Failed to get data and annotation metadata",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getAnnotationCategories()
    {
        try
        {
            Connection connection = this.getConnection();
            List<String> categories =
                this.persistenceManager.getAnnotationTableCategories(connection);
            
            return categories.toArray(new String[categories.size()]);
        }
        catch(Exception ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            //      exception fly
            LOG.log(Level.SEVERE,
                    "Failed to get data and annotation categories",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public TableColumnMetadata[] getDesignTerms()
    {
        try
        {
            TableColumnMetadata[] designTableMetadata =
                this.persistenceManager.getDesignTableColumnMetadata(
                        this.getConnection());
            return designTableMetadata;
        }
        catch(SQLException ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            //      exception fly
            LOG.log(Level.SEVERE,
                    "Failed to get design data",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String[][] getDesign()
    {
        try
        {
            TableData<Object> designData = this.persistenceManager.getDesignData(
                    this.getConnection());
            TableData<String> stringDesignData = designData.toStringData();
            return stringDesignData.getData();
        }
        catch(SQLException ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            // exception fly
            LOG.log(Level.SEVERE, "Failed to get design data", ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public GeneImageMetadata[] getGeneImageMetadata(String[] geneIds)
    {
        try
        {
            List<GeneImageMetadata> geneImgMetaList =
                new ArrayList<GeneImageMetadata>();
            for(String geneId : geneIds)
            {
                geneImgMetaList.addAll(Arrays.asList(this.persistenceManager.getGeneImageMetadata(
                        this.getConnection(),
                        geneId)));
            }
            
            return geneImgMetaList.toArray(
                    new GeneImageMetadata[geneImgMetaList.size()]);
        }
        catch(SQLException ex)
        {
            // TODO this error should be shared w/ the user maybe let the
            // exception fly
            LOG.log(Level.SEVERE, "Failed to get image metadata", ex);
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
        return (Connection)this.getServletContext().getAttribute(
                DatabaseServletContextListener.CONNECTION_ATTRIBUTE);
    }
}

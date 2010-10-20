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

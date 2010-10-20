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

package org.jax.pubarray.gwtqueryapp.client;

import org.jax.pubarray.gwtcommon.client.GeneImageMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableData;
import org.jax.pubarray.gwtcommon.client.TableMetadata;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The asynchronous interface for {@link QueryService}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface QueryServiceAsync
{
    /**
     * Perform a query with the given filters
     * @param query
     *          the query to use
     * @param rowOffset
     *          the row offset
     * @param rowCount
     *          the row count
     * @param callback
     *          callback for the results
     */
    public void executeQuery(
            Query query,
            int rowOffset,
            int rowCount,
            AsyncCallback<TableData<String>> callback);
    
    /**
     * Get metadata for the data table
     * @param callback
     *          callback for the results
     */
    public void getDataTableMetadata(AsyncCallback<TableMetadata> callback);
    
    /**
     * Get a list of all of the annotation data categories that are available
     * @param callback
     *          callback for the results
     */
    public void getAnnotationCategories(AsyncCallback<String[]> callback);
    
    /**
     * Getter that returns metadata for annotation
     * tables (everything except for the data and design tables).
     * then table index
     * @param annotationCategory
     *          the annotation category that we want tables from
     * @param callback
     *          callback for the results
     */
    public void getAnnotationMetadata(
            String annotationCategory,
            AsyncCallback<TableMetadata[]> callback);
    
    /**
     * Get the design data
     * @param callback
     *          callback for the results
     */
    public void getDesign(AsyncCallback<String[][]> callback);
    
    /**
     * Get the design terms. These are the column headers in the design file
     * @param callback
     *          callback for the results
     */
    public void getDesignTerms(AsyncCallback<TableColumnMetadata[]> callback);
    
    /**
     * Getter for the gene image metadata
     * @param geneIds the gene IDs
     * @param callback the callback
     */
    public void getGeneImageMetadata(
            String[] geneIds,
            AsyncCallback<GeneImageMetadata[]> callback);
}

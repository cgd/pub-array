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

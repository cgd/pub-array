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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The GWT service interface for PubArray queries
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
@RemoteServiceRelativePath("queryservice")
public interface QueryService extends RemoteService
{
    /**
     * Perform a query with the given filters
     * @param query
     *          the query to use
     * @param rowOffset
     *          the row offset
     * @param rowCount
     *          the row count
     * @return
     *          the results
     */
    public TableData<String> executeQuery(
            Query query,
            int rowOffset,
            int rowCount);
    
    /**
     * Getter that returns metadata for the data table
     * @return
     *          the metadata
     */
    public TableMetadata getDataTableMetadata();
    
    /**
     * Get a list of all of the annotation data categories that are available
     * @return
     *          the categories
     */
    public String[] getAnnotationCategories();
    
    /**
     * Getter that returns metadata for annotation
     * tables (everything except for the data and design tables).
     * then table index
     * @param annotationCategory
     *          the annotation category that we want tables from
     * @return
     *          the metadata
     */
    public TableMetadata[] getAnnotationMetadata(String annotationCategory);
    
    /**
     * Get the design
     * @return
     *          the design
     */
    public String[][] getDesign();
    
    /**
     * Get the design terms. These are the column headers in the design file
     * @return the terms
     */
    public TableColumnMetadata[] getDesignTerms();
    
    /**
     * Getter for the gene image metadata
     * @param geneIds the gene IDs
     * @return  the metadata
     */
    public GeneImageMetadata[] getGeneImageMetadata(String[] geneIds);
}

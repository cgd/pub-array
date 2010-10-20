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

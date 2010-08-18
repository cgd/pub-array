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

package org.jax.pubarray.gwtcommon.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for holding a probe query
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class Query implements Serializable
{
    /**
     * The maximum number of columns that the user is allowed to select
     * from a table
     */
    public static final int MAX_PERMITTED_TERMS = 201;
    
    /**
     * The maximum number of tables that we allow the user to join over
     */
    public static final int MAX_PERMITTED_TABLE_COUNT = 100;
    
    /**
     * how many filters do we want to allow the user to create
     */
    public static final int MAX_PERMITTED_FILTER_COUNT = 100;
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -5548513600067601811L;
    
    /**
     * The sort direction to use
     */
    public enum SortDirection
    {
        /**
         * Sort ascending
         */
        ASCENDING
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Ascending";
            }
        },
        
        /**
         * Sort descending
         */
        DESCENDING
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Descending";
            }
        }
    }
    
    private Filter[] filters;
    
    private QualifiedColumnMetadata orderByColumn;
    
    private SortDirection sortDirection;
    
    private QualifiedColumnMetadata[] termsOfInterest;
    
    /**
     * Constructor that sets filters to an empty mutable list
     */
    public Query()
    {
    }
    
    /**
     * Getter for the filters used in this query
     * @return the filters
     */
    public Filter[] getFilters()
    {
        return this.filters;
    }
    
    /**
     * Setter for the filters to use
     * @param filters the filters to set
     */
    public void setFilters(Filter[] filters)
    {
        this.filters = filters;
    }
    
    /**
     * Getter for the order by column
     * @return the order by column
     */
    public QualifiedColumnMetadata getOrderByColumn()
    {
        return this.orderByColumn;
    }
    
    /**
     * Setter for the order by column
     * @param orderByColumn the order by column to set
     */
    public void setOrderByColumn(QualifiedColumnMetadata orderByColumn)
    {
        this.orderByColumn = orderByColumn;
    }
    
    /**
     * Getter for the sort direction
     * @return the sort direction
     */
    public SortDirection getSortDirection()
    {
        return this.sortDirection;
    }
    
    /**
     * Setter for the sort direction
     * @param sortDirection the sort direction to set
     */
    public void setSortDirection(SortDirection sortDirection)
    {
        this.sortDirection = sortDirection;
    }
    
    /**
     * Getter for the terms of interest
     * @return the terms of interest
     */
    public QualifiedColumnMetadata[] getTermsOfInterest()
    {
        return this.termsOfInterest;
    }
    
    /**
     * Setter for terms of interest
     * @param termsOfInterest the terms of interest to set
     */
    public void setTermsOfInterest(QualifiedColumnMetadata[] termsOfInterest)
    {
        this.termsOfInterest = termsOfInterest;
    }
    
    /**
     * Determine how many tables are in this query
     * @return
     *          the number of tables
     */
    public int getTableCount()
    {
        Set<String> tableNames = new HashSet<String>();
        for(QualifiedColumnMetadata term: this.termsOfInterest)
        {
            tableNames.add(term.getTableName());
        }
        return tableNames.size();
    }
}

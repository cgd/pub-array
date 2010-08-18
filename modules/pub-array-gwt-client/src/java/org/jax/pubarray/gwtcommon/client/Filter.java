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

/**
 * Class for representing a particular filter condition
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class Filter implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2403337600954997860L;

    /**
     * The condition (ie operator) to use for filtering
     */
    @SuppressWarnings("all")
    public enum FilterCondition
    {
        EQUAL_TO
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "=";
            }
        },
        
        LESS_THAN
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "<";
            }
        },
        
        GREATER_THAN
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return ">";
            }
        },
        
        EXACTLY_MATCHES_ANY
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Exactly Matches Any";
            }
        },
        
        PARTIALLY_MATCHES_ANY
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Partially Matches Any";
            }
        }
    }
    
    private QualifiedColumnMetadata column;
    
    private FilterCondition condition;
    
    private String value;
    
    /**
     * Constructor
     */
    public Filter()
    {
    }
    
    /**
     * Constructor
     * @param column
     *          the column to filter
     * @param condition
     *          the condition we're filtering on
     * @param value
     *          the filter value
     */
    public Filter(QualifiedColumnMetadata column, FilterCondition condition, String value)
    {
        this.column = column;
        this.condition = condition;
        this.value = value;
    }

    /**
     * Getter for the column to filter
     * @return the column
     */
    public QualifiedColumnMetadata getColumn()
    {
        return this.column;
    }
    
    /**
     * Setter for the column
     * @param column the column to filter on
     */
    public void setColumn(QualifiedColumnMetadata column)
    {
        this.column = column;
    }
    
    /**
     * @return the condition
     */
    public FilterCondition getCondition()
    {
        return this.condition;
    }
    
    /**
     * @param condition the condition to set
     */
    public void setCondition(FilterCondition condition)
    {
        this.condition = condition;
    }
    
    /**
     * @return the value
     */
    public String getValue()
    {
        return this.value;
    }
    
    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return
            this.column + " " +
            this.condition.toString() + " " +
            this.value;
    }
    
    /**
     * Convert a list of filters into their string representation
     * @param filters
     *          the filter list
     * @return
     *          the string representation of the filters
     */
    public static String toString(Filter[] filters)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < filters.length; i++)
        {
            if(i >= 1)
            {
                sb.append(" AND ");
            }
            
            sb.append('(');
            sb.append(filters[i].toString());
            sb.append(')');
        }
        
        return sb.toString();
    }
}

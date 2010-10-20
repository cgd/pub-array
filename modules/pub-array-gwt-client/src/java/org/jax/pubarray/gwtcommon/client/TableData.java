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

package org.jax.pubarray.gwtcommon.client;

import java.io.Serializable;

/**
 * Holds all or a portion of a data table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <D>
 *          the data type used for fields
 */
public class TableData<D> implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1395094024412181970L;

    private D[][] data;
    
    private int startIndex;

    private int totalRowCount;
    
    /**
     * Constructor
     */
    public TableData()
    {
    }

    /**
     * Constructor
     * @param data
     *          the data for this table
     * @param startIndex
     *          the start index
     * @param totalRowCount
     *          the total row count
     */
    public TableData(
            D[][] data,
            int startIndex,
            int totalRowCount)
    {
        this.data = data;
        this.startIndex = startIndex;
        this.totalRowCount = totalRowCount;
    }
    
    /**
     * Getter for the actual data
     * @return the data
     */
    public D[][] getData()
    {
        return this.data;
    }
    
    /**
     * The data for this table
     * @param data the data to set
     */
    public void setData(D[][] data)
    {
        this.data = data;
    }
    
    /**
     * The start index of these data. This index is relative to the entire
     * data set (if this table data contains the entire data set then
     * this index should be 0)
     * @return the start index
     */
    public int getStartIndex()
    {
        return this.startIndex;
    }
    
    /**
     * Setter for the start index
     * @param startIndex the start index to set
     */
    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }
    
    /**
     * Getter for the total row count. If this table is only a subset of the
     * available data then this will be greater than the number of rows in
     * the data
     * @return the total row count
     */
    public int getTotalRowCount()
    {
        return this.totalRowCount;
    }
    
    /**
     * Setter for the total row count
     * @param totalRowCount the total row count
     */
    public void setTotalRowCount(int totalRowCount)
    {
        this.totalRowCount = totalRowCount;
    }
    
    /**
     * Converts all of the data to string using the {@link Object#toString()}
     * method unless the data is already a 2D string array in which case
     * this is returned
     * @return
     *          the stringified table data
     */
    @SuppressWarnings("unchecked")
    public TableData<String> toStringData()
    {
        D[][] genericData = this.data;
        
        if(genericData instanceof String[][])
        {
            return (TableData<String>)this;
        }
        else
        {
            String[][] stringData = new String[genericData.length][];
            
            for(int rowIndex = 0; rowIndex < stringData.length; rowIndex++)
            {
                D[] genericRow = genericData[rowIndex];
                String[] stringRow = new String[genericRow.length];
                for(int colIndex = 0; colIndex < stringRow.length; colIndex++)
                {
                    if(genericRow[colIndex] == null)
                    {
                        // TODO should we be using null here instead?
                        stringRow[colIndex] = "";
                    }
                    else
                    {
                        stringRow[colIndex] = genericRow[colIndex].toString();
                    }
                }
                
                stringData[rowIndex] = stringRow;
            }
            
            return new TableData<String>(
                    stringData,
                    this.startIndex,
                    this.totalRowCount);
        }
    }
}

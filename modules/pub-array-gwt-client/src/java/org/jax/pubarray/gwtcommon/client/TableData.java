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

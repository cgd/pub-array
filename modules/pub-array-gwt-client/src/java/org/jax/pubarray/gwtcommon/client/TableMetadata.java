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
 * Data that describes the structure of a table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TableMetadata implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5839739773773683412L;
    
    public static final String COLUMN_METADATA_PROP_STRING = "columnMetadata";
    
    private TableColumnMetadata[] columnMetadata;
    
    public static final String TABLE_NAME_PROP_STRING = "tableName";
    
    private String tableName;
    
    public static final String TABLE_ID_PROP_STRING = "tableId";
    
    private String tableId;
    
    /**
     * The default constructor
     */
    public TableMetadata()
    {
    }

    /**
     * Constructor
     * @param tableName
     *          the table's name
     * @param tableId
     *          the ID to use for the table
     * @param columnMetadata
     *          the column metadata for this table
     */
    public TableMetadata(
            String tableName,
            String tableId,
            TableColumnMetadata[] columnMetadata)
    {
        this.tableName = tableName;
        this.tableId = tableId;
        this.columnMetadata = columnMetadata;
    }
    
    /**
     * Getter for the column metadata
     * @return the column metadata
     */
    public TableColumnMetadata[] getColumnMetadata()
    {
        return this.columnMetadata;
    }
    
    /**
     * Setter for the column metadata
     * @param columnMetadata the column metadata
     */
    public void setColumnMetadata(TableColumnMetadata[] columnMetadata)
    {
        this.columnMetadata = columnMetadata;
    }
    
    /**
     * Getter for the table name
     * @return the table name
     */
    public String getTableName()
    {
        return this.tableName;
    }
    
    /**
     * Setter for the table name
     * @param tableName the table name
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    /**
     * Getter for the table ID.
     * @return the ID
     */
    public String getTableId()
    {
        return this.tableId;
    }
    
    /**
     * Setter for the ID
     * @param tableId the ID to set
     */
    public void setTableId(String tableId)
    {
        this.tableId = tableId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("TableMetadata: ");
        sb.append(TABLE_NAME_PROP_STRING);
        sb.append('=');
        sb.append(this.getTableName());
        sb.append(", ");
        sb.append(TABLE_ID_PROP_STRING);
        sb.append('=');
        sb.append(this.getTableId());
        sb.append(", ");
        sb.append(COLUMN_METADATA_PROP_STRING);
        sb.append("=[");
        
        for(int i = 0; i < this.columnMetadata.length; i++)
        {
            if(i >= 1)
            {
                sb.append(", ");
            }
            
            sb.append(this.columnMetadata[i].toString());
        }
        
        sb.append("]");
        
        return sb.toString();
    }
}

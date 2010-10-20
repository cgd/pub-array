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

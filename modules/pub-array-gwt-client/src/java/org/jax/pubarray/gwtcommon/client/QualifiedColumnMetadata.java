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

/**
 * A qualified column name contains the name of the table as well as the name
 * of the column
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QualifiedColumnMetadata extends TableColumnMetadata
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -8016456785573116483L;
    
    private String tableName = null;
    
    /**
     * Default constructor
     */
    public QualifiedColumnMetadata()
    {
    }

    /**
     * Constructor
     * @param tableName
     *          the table name to use
     * @param columnName
     *          the column name to use
     * @param dataType
     *          the type of this column
     * @param description
     *          a description for this column
     */
    public QualifiedColumnMetadata(
            String tableName,
            String columnName,
            DataType dataType,
            String description)
    {
        super(columnName, dataType, description);
        this.tableName = tableName;
    }
    
    /**
     * Constructor
     * @param tableName
     *          the table name to use
     * @param unqualifiedColToCopy
     *          the column to copy from
     */
    public QualifiedColumnMetadata(
            String tableName,
            TableColumnMetadata unqualifiedColToCopy)
    {
        super(unqualifiedColToCopy.getName(),
              unqualifiedColToCopy.getDataType(),
              unqualifiedColToCopy.getDescription());
        this.tableName = tableName;
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
     * @param tableName the table name to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    /**
     * Gets the qualified name which is <code>tableName.columnName</code>
     * @return
     *          the qualified name
     */
    public String getQualifiedName()
    {
        return this.getTableName() + '.' + this.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.tableName.hashCode() + super.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof QualifiedColumnMetadata)
        {
            QualifiedColumnMetadata colMeta = (QualifiedColumnMetadata)obj;
            return this.tableName == colMeta.tableName ||
                   this.tableName.equals(colMeta.tableName) ||
                   super.equals(colMeta);
        }
        else
        {
            return false;
        }
    }
}

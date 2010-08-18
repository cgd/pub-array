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

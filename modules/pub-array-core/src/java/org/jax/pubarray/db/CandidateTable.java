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

package org.jax.pubarray.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.util.io.FlatFileReader;

/**
 * A class for holding on to temporary candidate table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CandidateTable
{
    private TableColumnMetadata[] metadata = null;
    private File serverSideFile = null;
    private String tableName = null;
    private String categoryName = null;
    
    /**
     * Default constructor
     */
    public CandidateTable()
    {
    }

    /**
     * Constructor
     * @param metadata
     *          the metadata for this table
     * @param categoryName
     *          the category that this table falls into
     * @param tableName
     *          the name of the table
     * @param serverSideFile
     *          the file
     */
    public CandidateTable(
            TableColumnMetadata[] metadata,
            String categoryName,
            String tableName,
            File serverSideFile)
    {
        this.metadata = metadata;
        this.categoryName = categoryName;
        this.tableName = tableName;
        this.serverSideFile = serverSideFile;
    }
    
    /**
     * Constructor
     * @param metadata
     *          the metadata for this table
     * @param tableName
     *          the name of the table
     * @param serverSideFile
     *          the file
     */
    public CandidateTable(
            TableColumnMetadata[] metadata,
            String tableName,
            File serverSideFile)
    {
        this.metadata = metadata;
        this.categoryName = null;
        this.tableName = tableName;
        this.serverSideFile = serverSideFile;
    }

    /**
     * Getter for the metadata
     * @return the metadata
     */
    public TableColumnMetadata[] getMetadata()
    {
        return this.metadata;
    }
    
    /**
     * Setter for the metadata
     * @param metadata the metadata to set
     */
    public void setMetadata(TableColumnMetadata[] metadata)
    {
        this.metadata = metadata;
    }
    
    /**
     * Getter for the file
     * @return the file
     */
    public File getServerSideFile()
    {
        return this.serverSideFile;
    }
    
    /**
     * Setter for the file
     * @param serverSideFile the file to set
     */
    public void setServerSideFile(File serverSideFile)
    {
        this.serverSideFile = serverSideFile;
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
     * Getter for the category name
     * @return the category name
     */
    public String getCategoryName()
    {
        return this.categoryName;
    }
    
    /**
     * Setter for the category name
     * @param categoryName the category name to set
     */
    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }
    
    /**
     * A convenience function for reading {@link #getServerSideFile()} as a flat file.
     * Returns null if the file is null
     * @return
     *          the flat file reader or null
     * @throws FileNotFoundException
     *          if we get an exception trying to open the file
     */
    public FlatFileReader readFile() throws FileNotFoundException
    {
        File file = this.getServerSideFile();
        if(file == null)
        {
            return null;
        }
        else
        {
            FlatFileReader designFileReader = new FlatFileReader(
                    new FileReader(file),
                    CandidateDatabaseManager.STORAGE_FORMAT);
            
            return designFileReader;
        }
    }
}

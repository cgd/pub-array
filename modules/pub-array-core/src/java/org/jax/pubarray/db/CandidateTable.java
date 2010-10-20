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

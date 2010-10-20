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

package org.jax.pubarray.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.jax.util.io.FlatFileFormat;
import org.jax.util.io.FlatFileReader;

/**
 * A class that wraps up what a user has told us about a flat file
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FlatFileDescription
{
    private File flatFile;
    
    private String tableName;
    
    private FlatFileFormat format;
    
    /**
     * Default constructor (leaves everything null)
     */
    public FlatFileDescription()
    {
    }

    /**
     * Constructor
     * @param flatFile  see {@link #getFlatFile()}
     * @param tableName see {@link #getTableName()}
     * @param format    see {@link #getFormat()}
     */
    public FlatFileDescription(
            File flatFile,
            String tableName,
            FlatFileFormat format)
    {
        this.flatFile = flatFile;
        this.tableName = tableName;
        this.format = format;
    }
    
    /**
     * Convenience function to create a flat file reader from this description
     * @return the reader
     * @throws FileNotFoundException
     *          if the file from {@link #getFlatFile()} doesn't exist
     */
    public FlatFileReader createReader() throws FileNotFoundException
    {
        return new FlatFileReader(
                new BufferedReader(new FileReader(this.getFlatFile())),
                this.getFormat());
    }

    /**
     * Getter for the flat file that this class describes
     * @return  the flat file
     */
    public File getFlatFile()
    {
        return this.flatFile;
    }

    /**
     * Setter for the flat file
     * @param flatFile the flat file
     */
    public void setFlatFile(File flatFile)
    {
        this.flatFile = flatFile;
    }

    /**
     * Getter for the table name
     * @return  the table name
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
     * Getter for the format
     * @return  the format
     */
    public FlatFileFormat getFormat()
    {
        return this.format;
    }

    /**
     * Setter for the flat file format
     * @param format    the format
     */
    public void setFormat(FlatFileFormat format)
    {
        this.format = format;
    }
}

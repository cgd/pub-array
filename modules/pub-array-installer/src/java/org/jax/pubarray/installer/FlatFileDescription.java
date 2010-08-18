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

package org.jax.pubarray.installer;

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

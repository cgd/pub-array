/*
 * Copyright (c) 2009 The Jackson Laboratory
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

import java.io.File;

/**
 * This class simply holds a file reference to be used as a default starting
 * location when a file load/save dialog is opened
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SharedDirectoryContainer
{
    private File directory;
    
    /**
     * Constructor that sets the directory to null
     */
    public SharedDirectoryContainer()
    {
        this(null);
    }

    /**
     * Constructor
     * @param directory the directory to use
     */
    public SharedDirectoryContainer(File directory)
    {
        this.directory = directory;
    }
    
    /**
     * Getter for the directory
     * @return the directory
     */
    public File getDirectory()
    {
        return this.directory;
    }
    
    /**
     * Setter for the directory
     * @param directory the directory to set
     */
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }
}

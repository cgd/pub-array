/*
 * Copyright (c) 2010 The Jackson Laboratory
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

/**
 * A description for per-gene directory
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PerGeneImageDirectoryDescription
{
    /**
     * the prefix that we'll use for all per-probe images
     */
    public static final String PER_PROBE_IMAGE_PREFIX = "per-probe-images/";
    
    private File directory;
    
    private String name;

    /**
     * Constructor
     * @param directory
     *          the image dir
     * @param name
     *          the name that the user sees
     */
    public PerGeneImageDirectoryDescription(
            File directory,
            String name)
    {
        this.directory = directory;
        this.name = name;
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
    
    /**
     * Getter for the name
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Setter for the name
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
}

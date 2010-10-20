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

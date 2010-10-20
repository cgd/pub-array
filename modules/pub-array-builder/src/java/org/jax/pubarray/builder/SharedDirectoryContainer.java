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

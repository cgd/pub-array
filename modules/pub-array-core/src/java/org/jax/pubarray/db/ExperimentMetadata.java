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

import java.io.Serializable;

/**
 * This class holds experiment-level metadata
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExperimentMetadata implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4825717921791972218L;
    private final String experimentName;
    private final String experimentDescription;
    
    /**
     * Constructor
     * @param experimentName
     *          the experiment name
     * @param experimentDescription
     *          the experiment description
     */
    public ExperimentMetadata(
            String experimentName,
            String experimentDescription)
    {
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
    }
    
    /**
     * Getter for the experiment name
     * @return the experiment name
     */
    public String getExperimentName()
    {
        return this.experimentName;
    }
    
    /**
     * Getter for the experiment description
     * @return the experiment description
     */
    public String getExperimentDescription()
    {
        return this.experimentDescription;
    }
}

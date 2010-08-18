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

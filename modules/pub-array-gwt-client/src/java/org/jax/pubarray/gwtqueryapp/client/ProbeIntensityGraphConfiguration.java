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

package org.jax.pubarray.gwtqueryapp.client;

import java.io.Serializable;

import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ProbeIntensityGraphConfiguration implements Serializable
{

    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 93007299299000555L;
    
    /**
     * the graph type to use when we are grouping values
     */
    public enum GroupedGraphType
    {
        /**
         * use a scatter plot
         */
        SCATTER_PLOT,
        
        /**
         * use a box plot
         */
        BOX_PLOT
    }
    
    private String[] probeIds;
    
    private TableColumnMetadata orderProbesBy;
    
    private boolean groupReplicates;

    private QualifiedColumnMetadata[] termsOfInterest;
    
    private GroupedGraphType groupedGraphType;

    private boolean log2TransformData;

    private boolean includeDataFromAllArrays;
    
    /**
     * constructor
     */
    public ProbeIntensityGraphConfiguration()
    {
        this.probeIds = new String[0];
        this.orderProbesBy = null;
        this.groupReplicates = false;
        this.termsOfInterest = new QualifiedColumnMetadata[0];
        this.groupedGraphType = GroupedGraphType.SCATTER_PLOT;
        this.log2TransformData = false;
        this.includeDataFromAllArrays = false;
    }
    
    /**
     * Getter for the probe IDs
     * @return the probe IDs
     */
    public String[] getProbeIds()
    {
        return this.probeIds;
    }
    
    /**
     * Setter for the probe IDs
     * @param probeIds the probe IDs to set
     */
    public void setProbeIds(String[] probeIds)
    {
        this.probeIds = probeIds;
    }
    
    /**
     * Getter for the terms of interest
     * @return the termsOfInterest
     */
    public QualifiedColumnMetadata[] getTermsOfInterest()
    {
        return this.termsOfInterest;
    }
    
    /**
     * Setter for the terms of interest
     * @param termsOfInterest the termsOfInterest to set
     */
    public void setTermsOfInterest(QualifiedColumnMetadata[] termsOfInterest)
    {
        this.termsOfInterest = termsOfInterest;
    }
    
    /**
     * Getter for the ordering
     * @return the ordering
     */
    public TableColumnMetadata getOrderProbesBy()
    {
        return this.orderProbesBy;
    }
    
    /**
     * Setter for the ordering to use
     * @param orderProbesBy the orderProbesBy to set
     */
    public void setOrderProbesBy(TableColumnMetadata orderProbesBy)
    {
        this.orderProbesBy = orderProbesBy;
    }
    
    /**
     * Should replicates be grouped?
     * @return true if replicates should be grouped
     */
    public boolean getGroupReplicates()
    {
        return this.groupReplicates;
    }
    
    /**
     * Says whether or not replicates should be grouped
     * @param groupReplicates the groupReplicates to set
     */
    public void setGroupReplicates(boolean groupReplicates)
    {
        this.groupReplicates = groupReplicates;
    }
    
    /**
     * Getter for the graph type to use for grouped graphs
     * @return the the type to use
     */
    public GroupedGraphType getGroupedGraphType()
    {
        return this.groupedGraphType;
    }
    
    /**
     * Setter for the grouped graph type
     * @param groupedGraphType the grouped graph type
     */
    public void setGroupedGraphType(GroupedGraphType groupedGraphType)
    {
        this.groupedGraphType = groupedGraphType;
    }
    
    /**
     * Getter for log2 transform boolean
     * @return
     *          true if we should transform the data
     */
    public boolean getLog2TransformData()
    {
        return this.log2TransformData;
    }
    
    /**
     * Setter for LOG2 transforming data
     * @param log2TransformData
     *          should we log2 transform the data?
     */
    public void setLog2TransformData(boolean log2TransformData)
    {
        this.log2TransformData = log2TransformData;
    }

    /**
     * Should we just include terms of interest or all data?
     * @return
     *          should we include data from all arrays
     */
    public boolean getIncludeDataFromAllArrays()
    {
        return this.includeDataFromAllArrays;
    }
    
    /**
     * Should we just include terms of interest or all data?
     * @param includeDataFromAllArrays
     *          should we include data from all arrays
     */
    public void setIncludeDataFromAllArrays(boolean includeDataFromAllArrays)
    {
        this.includeDataFromAllArrays = includeDataFromAllArrays;
    }
}

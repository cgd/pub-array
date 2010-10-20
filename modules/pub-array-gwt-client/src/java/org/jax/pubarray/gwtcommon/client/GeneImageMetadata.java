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

package org.jax.pubarray.gwtcommon.client;

import java.io.Serializable;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GeneImageMetadata implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8789714332491894804L;

    private String geneId;
    
    private String categoryName;
    
    private String imagePath;
    
    /**
     * Constructor
     */
    public GeneImageMetadata()
    {
    }
    
    /**
     * Constructor
     * @param geneId
     *          the gene ID
     * @param categoryName
     *          the category
     * @param imagePath
     *          the image path
     */
    public GeneImageMetadata(String geneId, String categoryName,
            String imagePath)
    {
        super();
        this.geneId = geneId;
        this.categoryName = categoryName;
        this.imagePath = imagePath;
    }
    
    /**
     * @return the geneId
     */
    public String getGeneId()
    {
        return this.geneId;
    }
    
    /**
     * @param geneId the geneId to set
     */
    public void setGeneId(String geneId)
    {
        this.geneId = geneId;
    }
    
    /**
     * @return the categoryName
     */
    public String getCategoryName()
    {
        return this.categoryName;
    }
    
    /**
     * @param categoryName the categoryName to set
     */
    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }
    
    /**
     * Getter for the image path
     * @return the imagePath
     */
    public String getImagePath()
    {
        return this.imagePath;
    }
    
    /**
     * Setter for the image path
     * @param imagePath the imagePath to set
     */
    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return
            (this.geneId == null ? 1 : this.geneId.hashCode()) +
            (this.categoryName == null ? 1 : this.categoryName.hashCode()) +
            (this.imagePath == null ? 1 : this.imagePath.hashCode());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObj)
    {
        if(otherObj instanceof GeneImageMetadata)
        {
            GeneImageMetadata otherGeneImgMeta = (GeneImageMetadata)otherObj;
            return
                (this.geneId == otherGeneImgMeta.geneId ||
                this.geneId.equals(otherGeneImgMeta.geneId)) &&
                (this.categoryName == otherGeneImgMeta.categoryName ||
                this.categoryName.equals(otherGeneImgMeta.categoryName)) &&
                (this.imagePath == otherGeneImgMeta.imagePath ||
                this.imagePath.equals(otherGeneImgMeta.imagePath));
        }
        else
        {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return
            "Image Metadata: geneId=" + this.getGeneId() +
            ", categoryName=" + this.getCategoryName() +
            ", path=" + this.getImagePath();
    }
}

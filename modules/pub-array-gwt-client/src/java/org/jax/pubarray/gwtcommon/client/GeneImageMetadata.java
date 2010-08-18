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

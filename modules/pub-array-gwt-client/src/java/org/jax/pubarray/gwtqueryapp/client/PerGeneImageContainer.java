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

import org.jax.pubarray.gwtcommon.client.GeneImageMetadata;
import org.jax.pubarray.gwtcommon.client.GxtUtil;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * This container can be used to display per-probe images
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PerGeneImageContainer extends LayoutContainer
{
    private final QueryServiceAsync queryService;
    
    private AsyncCallback<GeneImageMetadata[]> currCallback = null;
    
    private FlexTable table;
    
    /**
     * Constructor
     * @param queryService the query service
     */
    public PerGeneImageContainer(QueryServiceAsync queryService)
    {
        this.queryService = queryService;
        this.table = new FlexTable();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        
        this.table = new FlexTable();
        this.add(this.table);
    }
    
    /**
     * Update the selected genes
     * @param selectedGenes the selected genes
     */
    public void updateSelectedGenes(String[] selectedGenes)
    {
        AsyncCallback<GeneImageMetadata[]> callback = new AsyncCallback<GeneImageMetadata[]>()
        {
            /**
             * {@inheritDoc}
             */
            public void onFailure(Throwable caught)
            {
                caught.printStackTrace();
                GxtUtil.showMessageDialog(
                        "Failed to Load Per-Probe Images",
                        "Failed to load per-probe images. Error message: " +
                        caught.getMessage());
            }
            
            /**
             * {@inheritDoc}
             */
            public void onSuccess(GeneImageMetadata[] imageMetadata)
            {
                for(GeneImageMetadata imgMeta: imageMetadata)
                {
                    System.out.println(imgMeta);
                }
                
                if(PerGeneImageContainer.this.currCallback == this)
                {
                    PerGeneImageContainer.this.currCallback = null;
                    PerGeneImageContainer.this.updateImages(imageMetadata);
                }
            }
        };
        
        this.currCallback = callback;
        this.queryService.getGeneImageMetadata(
                selectedGenes,
                callback);
    }

    /**
     * update the images
     * @param imageMetadata the image metadata
     */
    private void updateImages(GeneImageMetadata[] imageMetadata)
    {
        //clear image the table
        this.table.removeAllRows();
        
        if(imageMetadata.length >= 1)
        {
            // add the subheading
            HTML subheading = new HTML("<h3 class=\"page\">Additional Per-Probe Graphs:</h3>");
            this.table.setWidget(0, 0, subheading);
            
            // add all of the images
            for(int i = 0; i < imageMetadata.length; i++)
            {
                GeneImageMetadata currImageMetadata = imageMetadata[i];
                
                CaptionPanel cp = new CaptionPanel(
                        currImageMetadata.getCategoryName() +
                        " (ID: " + currImageMetadata.getGeneId() + ")");
                cp.add(new Image(currImageMetadata.getImagePath()));
                this.table.setWidget(i + 1, 0, cp);
            }
        }
    }
}

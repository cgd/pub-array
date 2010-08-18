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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jax.pubarray.gwtcommon.client.ModelBooleanFunction;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Contains some functionality common to my caching proxies
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class CachedPagingProxy implements DataProxy<PagingLoadResult<? extends ModelData>>
{
    /**
     * The model prop string to use for the label column
     */
    public static final String LABEL_PROP_STRING = "LABEL_PROP";
    
    /**
     * the full list of models
     */
    protected List<ModelData> allModels = Collections.emptyList();
    
    /**
     * A function used to filter out models
     */
    protected ModelBooleanFunction<ModelData> filterFunction = null;
    
    /**
     * Getter for the filter function
     * @return the filter function
     */
    public ModelBooleanFunction<ModelData> getFilterFunction()
    {
        return this.filterFunction;
    }
    
    /**
     * Setter for the filter function
     * @param filterFunction the filter function
     */
    public void setFilterFunction(ModelBooleanFunction<ModelData> filterFunction)
    {
        this.filterFunction = filterFunction;
    }
    
    /**
     * Refresh the data
     * @param pagingLoadConfig
     *          the paging configuration
     * @param callback
     *          the callback
     */
    protected void refreshLoad(
            PagingLoadConfig pagingLoadConfig,
            AsyncCallback<PagingLoadResult<? extends ModelData>> callback)
    {
        System.out.println(
                "Refreshing table results");
        
        final int offset = pagingLoadConfig.getOffset();
        final int limit = pagingLoadConfig.getLimit();
        System.out.println("offset: " + offset);
        System.out.println("limit:  " + limit);
        
        List<ModelData> filteredModels = this.getFilteredModels();
        int modelCount = filteredModels.size();
        List<ModelData> modelPage = new ArrayList<ModelData>(limit);
        for(int i = offset; i < modelCount && i < offset + limit; i++)
        {
            ModelData currTblColModel = filteredModels.get(i);
            modelPage.add(currTblColModel);
        }
        
        callback.onSuccess(new BasePagingLoadResult<ModelData>(
                modelPage,
                offset,
                modelCount));
    }
    
    /**
     * Getter for all of the models
     * @return all of the models
     */
    public List<ModelData> getAllModels()
    {
        return this.allModels;
    }
    
    /**
     * Getter for all of the models that pass through the filter
     * @return
     *          all of the models that pass through our filter
     */
    public List<ModelData> getFilteredModels()
    {
        if(this.filterFunction == null)
        {
            return this.allModels;
        }
        else
        {
            List<ModelData> filteredList = new ArrayList<ModelData>(
                    this.allModels.size());
            for(ModelData currModel: this.allModels)
            {
                if(this.filterFunction.evaluateModel(currModel))
                {
                    filteredList.add(currModel);
                }
            }
            return filteredList;
        }
    }
}

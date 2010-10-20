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

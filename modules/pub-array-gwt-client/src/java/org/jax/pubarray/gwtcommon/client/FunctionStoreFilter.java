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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreFilter;

/**
 * A store filter that uses the provided boolean function to do the filtering.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <T> 
 */
public class FunctionStoreFilter<T extends ModelData> implements StoreFilter<T>
{
    private ModelBooleanFunction<T> filterFunction;
    
    /**
     * Constructor
     * @param filterFunction
     *          the function to use for evaluating models
     */
    public FunctionStoreFilter(ModelBooleanFunction<T> filterFunction)
    {
        this.filterFunction = filterFunction;
    }
    
    /**
     * Setter for the filter function
     * @param filterFunction the filterFunction to set
     */
    public void setFilterFunction(ModelBooleanFunction<T> filterFunction)
    {
        this.filterFunction = filterFunction;
    }
    
    /**
     * Getter for the filter function
     * @return the filterFunction
     */
    public ModelBooleanFunction<T> getFilterFunction()
    {
        return this.filterFunction;
    }

    /**
     * This function filters the given item using the current boolean function.
     * All other parameters are completely ignored.
     * @param store
     *          don't care
     * @param parent
     *          don't care
     * @param item
     *          the item that is filtered by the current filter function
     * @param property
     *          don't care
     * @return
     *          true iff the filter function is null or the filter function
     *          evaluates the given item as true
     */
    public boolean select(Store<T> store, T parent, T item, String property)
    {
        return this.filterFunction == null || this.filterFunction.evaluateModel(item);
    }
}

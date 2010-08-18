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

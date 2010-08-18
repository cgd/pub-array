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

import java.util.List;

import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.TableData;
import org.jax.pubarray.gwtcommon.client.TableDataModelUtil;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Proxy for loading query results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QueryResultsProxy
implements DataProxy<PagingLoadResult<? extends ModelData>>
{
    private final QueryServiceAsync queryService;
    
    private final Query query;
    
    /**
     * Constructor
     * @param queryService
     *          the query service to use
     * @param query
     *          the query to use
     */
    public QueryResultsProxy(
            QueryServiceAsync queryService,
            Query query)
    {
        this.queryService = queryService;
        this.query = query;
    }
    
    /**
     * {@inheritDoc}
     */
    public void load(
            DataReader<PagingLoadResult<? extends ModelData>> reader,
            Object loadConfig,
            final AsyncCallback<PagingLoadResult<? extends ModelData>> callback)
    {
        final PagingLoadConfig pagingLoadConfig = (PagingLoadConfig)loadConfig;
        
        this.queryService.executeQuery(
                this.query,
                pagingLoadConfig.getOffset(),
                pagingLoadConfig.getLimit(),
                new AsyncCallback<TableData<String>>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void onFailure(Throwable caught)
                    {
                        callback.onFailure(caught);
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    public void onSuccess(TableData<String> result)
                    {
                        List<Model> models =
                            TableDataModelUtil.tableDataToModels(
                                    QueryResultsProxy.this.query.getTermsOfInterest(),
                                    result);
                        PagingLoadResult<? extends ModelData> modelResult =
                            new BasePagingLoadResult<Model>(
                                    models,
                                    result.getStartIndex(),
                                    result.getTotalRowCount());
                        
                        callback.onSuccess(modelResult);
                    }
                });
    }
    
    /**
     * Getter for the query
     * @return the query
     */
    public Query getQuery()
    {
        return this.query;
    }
}

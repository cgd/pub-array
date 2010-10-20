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

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

import org.jax.pubarray.gwtcommon.client.Filter;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.QueryModelUtil;
import org.jax.pubarray.gwtcommon.client.TableMetadata;
import org.jax.pubarray.gwtcommon.client.TableMetadataModelUtil;

import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Serves as a proxy for the data and metadata tables
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class DataMetadataProxy extends CachedPagingProxy
{
    private final QueryServiceAsync queryService;
    
    private Filter[] filters;

    private final boolean defaultSelected;
    
    private boolean dataAlreadyRequested = false;

    /**
     * Constructor
     * @param defaultSelected
     *          should rows be selected by default?
     * @param queryService
     *          the query service to use
     */
    public DataMetadataProxy(
            boolean defaultSelected,
            QueryServiceAsync queryService)
    {
        this.defaultSelected = defaultSelected;
        this.queryService = queryService;
    }
    
    /**
     * {@inheritDoc}
     */
    public void load(
            final DataReader<PagingLoadResult<? extends ModelData>> reader,
            final Object loadConfig,
            final AsyncCallback<PagingLoadResult<? extends ModelData>> callback)
    {
        final PagingLoadConfig pagingLoadConfig = (PagingLoadConfig)loadConfig;
        
        if(this.dataAlreadyRequested)
        {
            DeferredCommand.addCommand(new Command()
            {
                /**
                 * {@inheritDoc}
                 */
                public void execute()
                {
                    DataMetadataProxy.this.refreshLoad(pagingLoadConfig, callback);
                }
            });
        }
        else
        {
            System.out.println("Loading data table metadata");
            this.dataAlreadyRequested = true;
            this.queryService.getDataTableMetadata(
                    new AsyncCallback<TableMetadata>()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        public void onFailure(Throwable caught)
                        {
                            // pass on the good news
                            caught.printStackTrace();
                            callback.onFailure(caught);
                        }
                        
                        /**
                         * {@inheritDoc}
                         */
                        public void onSuccess(TableMetadata result)
                        {
                            DataMetadataProxy.this.loadSucceeded(
                                    result,
                                    pagingLoadConfig,
                                    callback);
                        }
                    });
        }
    }
    
    /**
     * Setter for the filters
     * @param filters the filters to set
     */
    public void setFilters(Filter[] filters)
    {
        this.filters = filters;
    }
    
    /**
     * Getter for the filters
     * @return the filters
     */
    public Filter[] getFilters()
    {
        return this.filters;
    }
    
    private void loadSucceeded(
            TableMetadata dataMetadata,
            PagingLoadConfig pagingLoadConfig,
            AsyncCallback<PagingLoadResult<? extends ModelData>> callback)
    {
        this.allModels = TableMetadataModelUtil.tableMetadataToModels(
                true,
                dataMetadata);
        
        // skip the 1st one. it's the probeset ID which we always keep anyway
        if(!this.allModels.isEmpty())
        {
            this.allModels.remove(0);
        }
        
        System.out.println("Model count: " + this.allModels.size());
        
        for(ModelData currTblColModel: this.allModels)
        {
            String name = currTblColModel.get(QualifiedColumnMetadata.NAME_PROP_STRING);
            currTblColModel.set(LABEL_PROP_STRING, name);
            currTblColModel.set(
                    QueryModelUtil.SELECT_COLUMN_ID,
                    this.defaultSelected);
        }
        
        this.refreshLoad(pagingLoadConfig, callback);
    }
}

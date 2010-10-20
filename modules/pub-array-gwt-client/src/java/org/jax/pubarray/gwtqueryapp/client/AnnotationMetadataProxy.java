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
import java.util.List;

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
public class AnnotationMetadataProxy extends CachedPagingProxy
{
    private final QueryServiceAsync queryService;
    
    private Filter[] filters;

    private final boolean defaultSelected;

    private final String category;
    
    private boolean dataAlreadyRequested = false;

    /**
     * Constructor
     * @param category
     *          the annotation category to load
     * @param defaultSelected
     *          should rows be selected by default?
     * @param queryService
     *          the query service to use
     */
    public AnnotationMetadataProxy(
            String category,
            boolean defaultSelected,
            QueryServiceAsync queryService)
    {
        this.category = category;
        this.defaultSelected = defaultSelected;
        this.queryService = queryService;
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
        
        if(this.dataAlreadyRequested)
        {
            DeferredCommand.addCommand(new Command()
            {
                /**
                 * {@inheritDoc}
                 */
                public void execute()
                {
                    AnnotationMetadataProxy.this.refreshLoad(pagingLoadConfig, callback);
                }
            });
        }
        else
        {
            System.out.println("Loading data/annotation table metadata");
            this.dataAlreadyRequested = true;
            this.queryService.getAnnotationMetadata(
                    this.category,
                    new AsyncCallback<TableMetadata[]>()
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
                        public void onSuccess(TableMetadata[] results)
                        {
                            AnnotationMetadataProxy.this.loadSucceeded(
                                    results,
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
            TableMetadata[] results,
            PagingLoadConfig pagingLoadConfig,
            AsyncCallback<PagingLoadResult<? extends ModelData>> callback)
    {
        System.out.println(
                "Got metadata results for " + results.length +
                " data/annotation tables");
        
        this.allModels = new ArrayList<ModelData>();
        for(int tableIndex = 0; tableIndex < results.length; tableIndex++)
        {
            List<ModelData> currTblModels = TableMetadataModelUtil.tableMetadataToModels(
                    true,
                    results[tableIndex]);
            for(ModelData currTblColModel: currTblModels)
            {
                final String name = currTblColModel.get(QualifiedColumnMetadata.NAME_PROP_STRING);
                final String tableName = results[tableIndex].getTableName();
                final String columnText;
                if(tableName == null || this.category.equals(tableName))
                {
                    columnText = name;
                }
                else
                {
                    columnText = tableName + ": " + name;
                }
                
                currTblColModel.set(LABEL_PROP_STRING, columnText);
            }
            
            this.allModels.addAll(currTblModels);
        }
        
        for(ModelData model: this.allModels)
        {
            model.set(
                    QueryModelUtil.SELECT_COLUMN_ID,
                    this.defaultSelected);
        }
        
        System.out.println("Model count: " + this.allModels.size());
        
        this.refreshLoad(
                pagingLoadConfig,
                callback);
    }
}

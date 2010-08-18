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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jax.gwtutil.client.event.ChangeListener;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.QueryModelUtil;
import org.jax.pubarray.gwtcommon.client.TableDataModelUtil;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.Model;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

/**
 * Panel used for showing query results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QueryResultsContainer extends LayoutContainer
{
    private static final String GREY_CSS_STYLE = "background-color:#E0E0E0;";
    private static final GridCellRenderer<Model> GREY_CELL_RENDERER =
        new GridCellRenderer<Model>()
        {
            /**
             * {@inheritDoc}
             */
            public String render(
                    Model model,
                    String property,
                    ColumnData config,
                    int rowIndex,
                    int colIndex,
                    ListStore<Model> store,
                    Grid<Model> grid)
            {
                if(config.style == null)
                {
                    config.style = GREY_CSS_STYLE;
                }
                else if(!config.style.contains(GREY_CSS_STYLE))
                {
                    config.style = GREY_CSS_STYLE + config.style;
                }
                
                return model.get(property);
            }
        };
    
    private static final int DEFAULT_COL_WIDTH = 150;
    
    private static final int PAGING_ROW_COUNT = 50;
    
    private final List<ChangeListener<QueryResultsContainer>> changeListeners;
    
    private final QueryResultsProxy proxy;
    
    private final PagingLoader<PagingLoadResult<Model>> loader;

    private final Query query;

    private final ListStore<Model> store;
    
    private ChangeListener<QueryResultsContainer> internalMaybeLayoutChangedListener;
    
    /**
     * Constructor
     * @param queryService the query service
     * @param query the query to use
     */
    public QueryResultsContainer(
            QueryServiceAsync queryService,
            Query query)
    {
        this.changeListeners = new ArrayList<ChangeListener<QueryResultsContainer>>();
        this.query = query;
        this.proxy = new QueryResultsProxy(queryService, query);
        this.loader = new BasePagingLoader<PagingLoadResult<Model>>(this.proxy);
        this.store = new ListStore<Model>(this.loader);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        
        this.setAutoWidth(true);
        
        // initialize the column model
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>(
                this.query.getTermsOfInterest().length);
        
        // add a select column
        CheckColumnConfig selectColumn = new CheckColumnConfig(
                QueryModelUtil.SELECT_COLUMN_ID,
                "View Details",
                DEFAULT_COL_WIDTH);
        columns.add(selectColumn);
        
        // the data columns
        QualifiedColumnMetadata[] terms = this.query.getTermsOfInterest();
        
        // discover duplicate column names (we'll need to fully qualify these)
        Set<String> allColNames = new HashSet<String>();
        Set<String> colNamesWithDuplicates = new HashSet<String>();
        for(QualifiedColumnMetadata colMeta: terms)
        {
            if(!allColNames.add(colMeta.getName()))
            {
                colNamesWithDuplicates.add(colMeta.getName());
            }
        }
        
        GridCellRenderer<Model> currRenderer = null;
        for(int termIndex = 0; termIndex < terms.length; termIndex++)
        {
            QualifiedColumnMetadata colMeta = terms[termIndex];
            ColumnConfig currColumn = new ColumnConfig();
            
            // the replace(...) is because GXT does not appear to like column
            // names that contain the '.' character.
            //
            // TODO this should eventually use an escape function so that we
            // are guaranteed that there will be no name collisions. Note that
            // this is also done in TableDataModelUtil.tableDataToModels(...)
            currColumn.setId(colMeta.getQualifiedName().replace('.', '+'));
            currColumn.setWidth(DEFAULT_COL_WIDTH);
            
            if(colNamesWithDuplicates.contains(colMeta.getName()))
            {
                // for columns with duplicate names they need to be
                // fully qualified
                currColumn.setHeader(colMeta.getName() + " (" + colMeta.getTableName() + ")");
            }
            else
            {
                currColumn.setHeader(colMeta.getName());
            }
            
            if(termIndex >= 1 && !colMeta.getTableName().equals(terms[termIndex - 1].getTableName()))
            {
                // table name switched so we should swap renderers
                if(currRenderer == null)
                {
                    currRenderer = GREY_CELL_RENDERER;
                }
                else
                {
                    currRenderer = null;
                }
            }
            
            if(currRenderer != null)
            {
                currColumn.setRenderer(currRenderer);
            }
            
            columns.add(currColumn);
        }
        
        for(ColumnConfig colConfig: columns)
        {
            colConfig.setSortable(false);
            colConfig.setMenuDisabled(true);
        }
        
        // initialize the grid that will contain our probe metadata
        final ColumnModel columnModel = new ColumnModel(columns);
        final Grid<Model> probeMetadataGrid = new Grid<Model>(
                this.store,
                columnModel);
        probeMetadataGrid.addPlugin(selectColumn);
        probeMetadataGrid.setLoadMask(true);
        probeMetadataGrid.setAutoWidth(true);
        probeMetadataGrid.mask("Loading...");
        probeMetadataGrid.setHeight(500);
        probeMetadataGrid.getView().setShowDirtyCells(false);
        
        // initialize toolbar that has the grid paging controls
        PagingToolBar pt = new PagingToolBar(PAGING_ROW_COUNT);
        pt.bind(this.loader);
        
        // add components to the content panel
        final ContentPanel panel = new ContentPanel();
        panel.setHeading("Search Results");
        panel.setFrame(true);
        panel.setCollapsible(true);
        panel.setAnimCollapse(false);
        panel.setButtonAlign(HorizontalAlignment.CENTER);
        panel.setIconStyle("icon-table");
        panel.setLayout(new FitLayout());
        panel.add(probeMetadataGrid);
        panel.setBottomComponent(pt);
        
        this.add(panel);
        this.add(new HTML(this.getCsvFileLink()));
        
        this.store.addStoreListener(
                new StoreListener<Model>()
                {
                    private volatile boolean initializationComplete = false;
                    
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void storeUpdate(StoreEvent<Model> se)
                    {
                        System.out.println("query results store update");
                        QueryResultsContainer.this.fireChangeEvent();
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void storeDataChanged(StoreEvent<Model> se)
                    {
                        System.out.println("data store changed");
                        if(!this.initializationComplete)
                        {
                            this.initializationComplete = true;
                            QueryResultsContainer.this.initializationCompleted();
                        }
                    }
                });

        this.loader.load(0, PAGING_ROW_COUNT);
        
        this.internalMaybeLayoutChangedListener =
            new ChangeListener<QueryResultsContainer>()
            {
                /**
                 * {@inheritDoc}
                 */
                public void changeOccured(QueryResultsContainer source)
                {
                    if(probeMetadataGrid.isRendered())
                    {
                        probeMetadataGrid.syncSize();
                    }
                }
            };
        
        // if we don't have this then the grid doesn't resize correctly when the
        // browser window is resized
        Window.addResizeHandler(new ResizeHandler()
        {
            /**
             * {@inheritDoc}
             */
            public void onResize(ResizeEvent event)
            {
                QueryResultsContainer.this.maybeLayoutChanged();
            }
        });
    }
    
    private void initializationCompleted()
    {
        List<Model> models = this.store.getModels();
        if(!models.isEmpty())
        {
            models.get(0).set(QueryModelUtil.SELECT_COLUMN_ID, Boolean.TRUE);
        }
        
        this.fireChangeEvent();
    }

    /**
     * Get the link that should be used for downloading the CSV file
     * @return  the CSV file
     */
    private String getCsvFileLink()
    {
        String csvUrl = "restful/query-results/latest-query.csv";
        return "<a href=\"" + csvUrl + "\">" +
               "Download Results Table (Comma-Separated Values)</a>";
    }
    
    /**
     * Get the terms of interest selected by the user
     * @return
     *          the terms of interest
     */
    public List<String> getSelectedProbeIds()
    {
        List<Model> allModels = this.store.getModels();
        
        List<String> selectedTerms = new ArrayList<String>();
        for(Model currModel: allModels)
        {
            // filter out models that are not selected
            if(QueryModelUtil.isSelected(currModel))
            {
                selectedTerms.add(TableDataModelUtil.extractProbeIdFromModel(
                        this.query.getTermsOfInterest(),
                        currModel));
            }
        }
        
        return selectedTerms;
    }
    
    /**
     * Register listeners for changes to the comparison expression
     * @param changeListener
     *          the listener to register
     */
    public void addChangeListener(ChangeListener<QueryResultsContainer> changeListener)
    {
        this.changeListeners.add(changeListener);
    }
    
    /**
     * Remove listener
     * @see #addChangeListener(ChangeListener)
     * @param changeListener
     *          the change listener to remove from the listener list
     */
    public void removeChangeListener(ChangeListener<QueryResultsContainer> changeListener)
    {
        this.changeListeners.remove(changeListener);
    }
    
    /**
     * Removes all change listeners
     */
    public void clearChangeListeners()
    {
        this.changeListeners.clear();
    }
    
    /**
     * Fire a new change event
     */
    private void fireChangeEvent()
    {
        DeferredCommand.addCommand(new Command()
        {
            /**
             * {@inheritDoc}
             */
            public void execute()
            {
                for(ChangeListener<QueryResultsContainer> changeListener:
                    QueryResultsContainer.this.changeListeners)
                {
                    changeListener.changeOccured(QueryResultsContainer.this);
                }
            }
        });
    }

    /**
     * check to see if the grid layout needs to be updated
     */
    public void maybeLayoutChanged()
    {
        if(this.internalMaybeLayoutChangedListener != null)
        {
            // defer in order to give the browser a chance to handle any
            // other events
            DeferredCommand.addCommand(
                    new Command()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        public void execute()
                        {
                            QueryResultsContainer.this.internalMaybeLayoutChangedListener.changeOccured(
                                    QueryResultsContainer.this);
                        }
                    });
        }
    }
}

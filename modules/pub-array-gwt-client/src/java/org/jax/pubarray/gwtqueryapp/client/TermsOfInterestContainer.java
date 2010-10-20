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
import java.util.Vector;

import org.jax.gwtutil.client.event.ChangeBroadcaster;
import org.jax.gwtutil.client.event.ChangeListener;
import org.jax.pubarray.gwtcommon.client.MatchesAllFunction;
import org.jax.pubarray.gwtcommon.client.MatchesAnyFunction;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadataModelUtil;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.QueryModelUtil;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableMetadata;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.Record.RecordUpdate;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The terms of interest container allows the user to specify which terms
 * (ie columns) they are interested in working with
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TermsOfInterestContainer
extends LayoutContainer
implements ChangeBroadcaster<TermsOfInterestContainer>
{
    private static final int PAGING_ROW_COUNT = 200;
    
    private static final String MATCH_ALL_TERMS_FILTER = "Match All Terms";
    private static final String MATCH_ANY_TERMS_FILTER = "Match Any Term";
    
    private final ChangeListener<TermsOfInterestContainer> initializationCompleteListener;
    private final List<ChangeListener<TermsOfInterestContainer>> changeListeners;
    private final List<ChangeListener<TermsOfInterestContainer>> internalMaybeLayoutChangedListeners;
        
    private final Grid<ModelData> dataGrid;
    private final List<Grid<ModelData>> annotationGrids;
    
    private TableColumnMetadata[] designColumnMetadata = null;
    private String[][] designData = null;
    private TableMetadata dataMetadata;
    
    private final String[] annotationCategories;
    
    // this number is for tracking how many pending load requests there are.
    // Every time we make a load request this number is incremented and
    // every time we get a response it is decremented
    private int pendingLoadRequests = 0;
    private boolean initializationComplete = false;
    
    // This number represents the total number of terms that have been loaded
    // over all of the grids
    private int loadedTermCount = 0;
    
    private class TermsOfInterestStoreListener extends StoreListener<ModelData>
    {
        private final Grid<ModelData> grid;
        
        /**
         * Constructor
         * @param grid
         *          the grid that the events we're listening to are related to
         */
        public TermsOfInterestStoreListener(Grid<ModelData> grid)
        {
            this.grid = grid;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void storeUpdate(StoreEvent<ModelData> se)
        {
            TermsOfInterestContainer.this.termsDataStoreUpdated(
                    se,
                    this.grid);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void storeDataChanged(StoreEvent<ModelData> se)
        {
            TermsOfInterestContainer.this.modelsUploaded(
                    se.getStore());
        }
    }
    
    /**
     * Constructor
     * @param queryService
     *          the query service to use
     * @param annotationCategories
     *          the annotation categories
     * @param initializationCompleteListener
     *          this gets called after initialization is complete
     */
    public TermsOfInterestContainer(
            final QueryServiceAsync queryService,
            final String[] annotationCategories,
            final ChangeListener<TermsOfInterestContainer> initializationCompleteListener)
    {
        this.annotationCategories = annotationCategories;
        this.initializationCompleteListener = initializationCompleteListener;
        
        this.changeListeners = new ArrayList<ChangeListener<TermsOfInterestContainer>>();
        this.internalMaybeLayoutChangedListeners =
            new ArrayList<ChangeListener<TermsOfInterestContainer>>();
        final DataMetadataProxy dataProxy = new DataMetadataProxy(
                false,
                queryService);
        final PagingLoader<PagingLoadResult<ModelData>> dataStoreLoader =
            new BasePagingLoader<PagingLoadResult<ModelData>>(dataProxy);
        
        ColumnModel dataColumnModel = this.makeGridColumnModel();
        final ListStore<ModelData> dataStore =
            new ListStore<ModelData>(dataStoreLoader);
        this.dataGrid = new Grid<ModelData>(
                dataStore,
                dataColumnModel);
        this.dataGrid.addPlugin((ComponentPlugin)dataColumnModel.getColumn(0));
        this.dataGrid.setBorders(false);
        this.dataGrid.setHeight(300);
        this.dataGrid.setLoadMask(true);
        this.dataGrid.setAutoWidth(true);
        this.dataGrid.setAutoExpandColumn(CachedPagingProxy.LABEL_PROP_STRING);
        this.dataGrid.setAutoExpandMax(Integer.MAX_VALUE);
        dataStore.addStoreListener(new TermsOfInterestStoreListener(
                this.dataGrid));
        
        this.annotationGrids = new ArrayList<Grid<ModelData>>(
                annotationCategories.length);
        for(int i = 0; i < annotationCategories.length; i++)
        {
            final AnnotationMetadataProxy proxy = new AnnotationMetadataProxy(
                    annotationCategories[i],
                    false,
                    queryService);
            final PagingLoader<PagingLoadResult<ModelData>> loader =
                new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
            
            final ColumnModel annotationColumnModel = this.makeGridColumnModel();
            final ListStore<ModelData> store = new ListStore<ModelData>(loader);
            final Grid<ModelData> grid = new Grid<ModelData>(
                    store,
                    annotationColumnModel);
            grid.addPlugin((ComponentPlugin)annotationColumnModel.getColumn(0));
            grid.setBorders(false);
            grid.setHeight(300);
            grid.setLoadMask(true);
            grid.setAutoWidth(true);
            grid.setAutoExpandColumn(AnnotationMetadataProxy.LABEL_PROP_STRING);
            grid.setAutoExpandMax(Integer.MAX_VALUE);
            store.addStoreListener(new TermsOfInterestStoreListener(
                    grid));
            
            this.annotationGrids.add(grid);
        }
        
        queryService.getDesignTerms(
                new AsyncCallback<TableColumnMetadata[]>()
                {
                    public void onFailure(Throwable caught)
                    {
                        // TODO tell user
                        caught.printStackTrace();
                    }

                    public void onSuccess(TableColumnMetadata[] metadata)
                    {
                        TermsOfInterestContainer.this.designColumnMetadataLoaded(metadata);
                    }
                });
        
        queryService.getDesign(
                new AsyncCallback<String[][]>()
                {
                    public void onFailure(Throwable caught)
                    {
                        // TODO tell user
                        caught.printStackTrace();
                    }

                    public void onSuccess(String[][] designData)
                    {
                        TermsOfInterestContainer.this.designDataLoaded(designData);
                    }
                });
        
        queryService.getDataTableMetadata(
                new AsyncCallback<TableMetadata>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void onFailure(Throwable caught)
                    {
                        // TODO tell the user
                        caught.printStackTrace();
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    public void onSuccess(TableMetadata metadata)
                    {
                        TermsOfInterestContainer.this.dataTableMetadataLoaded(
                                metadata);
                    }
                });
    }
    
    /**
     * Determines if initialization is complete
     * @return true iff initialization is complete
     */
    public boolean isInitializationComplete()
    {
        return this.initializationComplete;
    }
    
    private ColumnModel makeGridColumnModel()
    {
        List<ColumnConfig> colConfigs = new ArrayList<ColumnConfig>();
        CheckColumnConfig selectColumn = new CheckColumnConfig(
                QueryModelUtil.SELECT_COLUMN_ID,
                "Select",
                75);
        colConfigs.add(selectColumn);
        colConfigs.add(new ColumnConfig(
                CachedPagingProxy.LABEL_PROP_STRING,
                "Term Name",
                400));
        for(ColumnConfig colConfig: colConfigs)
        {
            colConfig.setSortable(false);
            colConfig.setMenuDisabled(true);
        }
        
        return new ColumnModel(colConfigs);
    }

    private void dataTableMetadataLoaded(TableMetadata metadata)
    {
        this.dataMetadata = metadata;
        this.maybeInitializeArrayFilterAndLoadGrid();
    }

    private void designDataLoaded(String[][] designData)
    {
        this.designData = designData;
        this.maybeInitializeArrayFilterAndLoadGrid();
    }

    private void designColumnMetadataLoaded(TableColumnMetadata[] designColumnMetadata)
    {
        this.designColumnMetadata = designColumnMetadata;
        this.maybeInitializeArrayFilterAndLoadGrid();
    }

    private void maybeInitializeArrayFilterAndLoadGrid()
    {
        if(this.designData != null &&
           this.designColumnMetadata != null &&
           this.dataMetadata != null)
        {
            System.out.println("ready to initialize array filter");
            this.loadGrids();
        }
    }

    private void loadGrids()
    {
        // kick off loading all of the grids
        // TODO this should probably be incremental... all at once may
        // overwhelm some browsers
        this.pendingLoadRequests = 1 + this.annotationGrids.size();
        this.dataGrid.getStore().getLoader().load();
        for(Grid<ModelData> annotationGrid: this.annotationGrids)
        {
            annotationGrid.getStore().getLoader().load();
        }
        
        if(this.initializationCompleteListener != null)
        {
            this.initializationCompleteListener.changeOccured(this);
        }
    }
    
    /**
     * Lets this container know that the layout might have changed so that
     * we can change panel sizes if needed
     */
    public void maybeLayoutChanged()
    {
        for(final ChangeListener<TermsOfInterestContainer> layoutListener:
            this.internalMaybeLayoutChangedListeners)
        {
            // defer the layout updates in order to allow the browser breathing room
            DeferredCommand.addCommand(new Command()
            {
                /**
                 * {@inheritDoc}
                 */
                public void execute()
                {
                    layoutListener.changeOccured(TermsOfInterestContainer.this);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        
        final TabPanel tabPanel = new TabPanel();
        tabPanel.setPlain(true);
        tabPanel.setAutoHeight(true);
        
        for(int i = 0; i < this.annotationCategories.length; i++)
        {
            this.addGridTab(
                    tabPanel,
                    this.annotationCategories[i],
                    this.annotationGrids.get(i));
        }
        this.addGridTab(tabPanel, "Data Columns", this.dataGrid);
        this.add(tabPanel);
    }
    
    private void addGridTab(
            final TabPanel tabPanel,
            final String tabName,
            final Grid<ModelData> grid)
    {
        final TabItem tabItem = new TabItem(tabName);
        tabItem.setAutoWidth(true);
        
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.add(grid);
        contentPanel.setHeaderVisible(false);
        contentPanel.setBorders(false);
        
        ToolBar topToolBar = new ToolBar();
        PagingToolBar bottomToolBar = new PagingToolBar(PAGING_ROW_COUNT);
        this.addToolbarItems(topToolBar, bottomToolBar, grid);
        contentPanel.setTopComponent(topToolBar);
        contentPanel.setBottomComponent(bottomToolBar);
        
        tabItem.add(contentPanel);
        tabPanel.add(tabItem);
        tabItem.addListener(
                Events.Select,
                new Listener<ComponentEvent>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void handleEvent(ComponentEvent be)
                    {
                        if(tabItem.isRendered())
                        {
                            System.out.println(tabName + " tab selected");
                            grid.getView().refresh(true);
                        }
                    }
                });
        
        final Command resizeTabCommand = new Command()
        {
            /**
             * {@inheritDoc}
             */
            public void execute()
            {
                // getWidth is not allowed until a component is
                // rendered
                if(tabItem.isRendered())
                {
                    grid.syncSize();
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
                // defer the command to give the browser a chance
                // to deal with any other events
                DeferredCommand.addCommand(resizeTabCommand);
            }
        });
        
        this.internalMaybeLayoutChangedListeners.add(
                new ChangeListener<TermsOfInterestContainer>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void changeOccured(TermsOfInterestContainer source)
                    {
                        // defer the command to give the browser a chance
                        // to deal with any other events
                        DeferredCommand.addCommand(resizeTabCommand);
                    }
                });
    }
    
    /**
     * Adds the filter stuff allong with the "Toggle Select All" button
     * to the toolbar
     * @param topToolbar
     *          the toolbar
     * @param bottomToolBar
     *          the paging toolbar
     * @param grid
     *          the grid
     */
    @SuppressWarnings("unchecked")
    private void addToolbarItems(
            final ToolBar topToolbar,
            final PagingToolBar bottomToolBar,
            final Grid<ModelData> grid)
    {
        PagingLoader pagingLoader =
            (PagingLoader)grid.getStore().getLoader();
        bottomToolBar.bind(pagingLoader);
        
        final MatchesAllFunction<ModelData> matchesAllFunction =
            new MatchesAllFunction<ModelData>();
        final MatchesAnyFunction<ModelData> matchesAnyFunction = new MatchesAnyFunction<ModelData>();
        final CachedPagingProxy proxy = this.extractProxy(grid);
        
        proxy.setFilterFunction(matchesAllFunction);
        
        topToolbar.add(new LabelToolItem("Filter:"));
        final TextField<String> filterText = new TextField<String>();
        filterText.setEmptyText("Eg: term1 term2 ...");
        topToolbar.add(filterText);
        
        final SimpleComboBox<String> filterChoiceCombo = new SimpleComboBox<String>();
        filterChoiceCombo.setWidth(120);
        filterChoiceCombo.setAllowBlank(false);
        filterChoiceCombo.setEditable(false);
        filterChoiceCombo.add(MATCH_ALL_TERMS_FILTER);
        filterChoiceCombo.add(MATCH_ANY_TERMS_FILTER);
        filterChoiceCombo.setSimpleValue(MATCH_ALL_TERMS_FILTER);
        filterChoiceCombo.setTriggerAction(TriggerAction.ALL);
        topToolbar.add(filterChoiceCombo);
        topToolbar.add(new SeparatorToolItem());
        
        Button runFilterButton = new Button("Run Filter");
        runFilterButton.addSelectionListener(
                new SelectionListener<ButtonEvent>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        String filterValueString = filterText.getValue();
                        String filterTypeString = filterChoiceCombo.getSimpleValue();
                        if(filterTypeString == MATCH_ALL_TERMS_FILTER)
                        {
                            matchesAllFunction.updateDelimitedText(filterValueString);
                            proxy.setFilterFunction(matchesAllFunction);
                        }
                        else
                        {
                            matchesAnyFunction.updateDelimitedText(filterValueString);
                            proxy.setFilterFunction(matchesAnyFunction);
                        }
                        
                        TermsOfInterestContainer.this.reload(bottomToolBar);
                        TermsOfInterestContainer.this.fireChangeEvent();
                    }
                });
        topToolbar.add(runFilterButton);
        topToolbar.add(new SeparatorToolItem());
        
        Button selectAllButton = new Button("Toggle Select All");
        selectAllButton.addSelectionListener(
                new SelectionListener<ButtonEvent>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        TermsOfInterestContainer.this.toggleSelectAllFromGrid(grid);
                    }
                });
        
        topToolbar.add(selectAllButton);
        topToolbar.add(new SeparatorToolItem());
    }
    
    private void reload(final PagingToolBar bottomToolBar)
    {
        DeferredCommand.addCommand(new Command()
        {
            /**
             * {@inheritDoc}
             */
            public void execute()
            {
                if(bottomToolBar.getActivePage() != 1)
                {
                    bottomToolBar.setActivePage(1);
                }
                else
                {
                    bottomToolBar.refresh();
                }
            }
        });
    }

    /**
     * Called when the user clicks "toggle select all" for a grid. This function
     * only operates on the terms that pass through the user's specified filter
     * (assuming there is one). This function fires a change event
     * @param grid
     *          the grid
     */
    private void toggleSelectAllFromGrid(final Grid<ModelData> grid)
    {
        boolean anyAreNotSelected = false;
        List<ModelData> filteredModels =
            this.extractProxy(grid).getFilteredModels();
        for(ModelData currModel: filteredModels)
        {
            if(!QueryModelUtil.isSelected(currModel))
            {
                anyAreNotSelected = true;
                break;
            }
        }
        
        // if any are not selected then we should select all. otherwise
        // we should deselect all
        for(ModelData currModel: filteredModels)
        {
            currModel.set(
                    QueryModelUtil.SELECT_COLUMN_ID,
                    anyAreNotSelected);
        }
        
        grid.getView().refresh(false);
        this.fireChangeEvent();
    }
    
    /**
     * Select all from the given grid. This unlike
     * {@link #toggleSelectAllFromGrid(Grid)} this function does not care
     * if a term is filtered or not. It gets selected either way. Also
     * this function will not fire a change event
     * @param grid  the grid to select
     */
    private void selectAllFromGrid(final Grid<ModelData> grid)
    {
        List<ModelData> models = grid.getStore().getModels();
        for(ModelData currModel: models)
        {
            currModel.set(
                    QueryModelUtil.SELECT_COLUMN_ID,
                    true);
        }
        
        grid.getView().refresh(false);
    }

    /**
     * Get the terms of interest selected by the user
     * @return
     *          the terms of interest
     */
    public List<QualifiedColumnMetadata> getSelectedTerms()
    {
        Vector<QualifiedColumnMetadata> selectedTerms =
            new Vector<QualifiedColumnMetadata>();
        
        // the 1st column from the data table is probe ID and it should always
        // be a part of the terms of interest
        if(this.dataMetadata != null &&
           this.dataMetadata.getColumnMetadata() != null &&
           this.dataMetadata.getColumnMetadata().length >= 1)
        {
            TableColumnMetadata probeIdCol = this.dataMetadata.getColumnMetadata()[0];
            selectedTerms.add(new QualifiedColumnMetadata(
                    this.dataMetadata.getTableName(),
                    probeIdCol));
        }
        
        for(Grid<ModelData> annotationGrid: this.annotationGrids)
        {
            List<ModelData> filteredAnnoModels =
                this.extractProxy(annotationGrid).getFilteredModels();
            for(ModelData currAnnoModel: filteredAnnoModels)
            {
                // filter out models that are not selected
                if(QueryModelUtil.isSelected(currAnnoModel))
                {
                    selectedTerms.add(QualifiedColumnMetadataModelUtil.fromModelToPojo(
                            currAnnoModel));
                }
            }
        }
        
        selectedTerms.addAll(this.getSelectedArrays());
        
        return selectedTerms;
    }
    
    private List<QualifiedColumnMetadata> getSelectedArrays()
    {
        List<QualifiedColumnMetadata> arraysOfInterest =
            new ArrayList<QualifiedColumnMetadata>();
        List<ModelData> filteredDataModels =
            this.extractProxy(this.dataGrid).getFilteredModels();
        for(ModelData currDataModel: filteredDataModels)
        {
            if(QueryModelUtil.isSelected(currDataModel))
            {
                QualifiedColumnMetadata currTermOfInterest =
                    QualifiedColumnMetadataModelUtil.fromModelToPojo(currDataModel);
                arraysOfInterest.add(currTermOfInterest);
            }
        }
        
        return arraysOfInterest;
    }
    
    /**
     * Extract the proxy from the given model data.
     * @param grid
     *          the grid
     * @return
     *          the proxy
     * @throws ClassCastException
     *          if the grid doesn't have a {@link CachedPagingProxy} tucked
     *          inside a {@link BasePagingLoader}
     */
    @SuppressWarnings("unchecked")
    private CachedPagingProxy extractProxy(Grid<ModelData> grid)
    throws ClassCastException
    {
        BasePagingLoader loader =
            (BasePagingLoader)grid.getStore().getLoader();
        return (CachedPagingProxy)loader.getProxy();
    }
    
    /**
     * This function is called each time a grid's store is successfully loaded
     * @param store
     *          the store that has been loaded
     */
    private void modelsUploaded(Store<? extends ModelData> store)
    {
        List<? extends ModelData> models = store.getModels();
        if(models != null)
        {
            System.out.println("loading models into map");
            this.pendingLoadRequests--;
            this.loadedTermCount += models.size();
            
            boolean fireChange = false;
            for(ModelData currModel: models)
            {
                // determines if any of the models are preselected
                if(QueryModelUtil.isSelected(currModel))
                {
                    fireChange = true;
                    break;
                }
            }
            
            // if we have finished loading all terms
            if(this.pendingLoadRequests == 0)
            {
                // the + 1 adjusts for the probeset ID
                if(this.loadedTermCount + 1 <= Query.MAX_PERMITTED_TERMS)
                {
                    // we have finished loading all terms and the term count is
                    // less than the maximum so we should just select very term
                    // by default
                    this.selectAllFromGrid(this.dataGrid);
                    for(Grid<ModelData> annoGrid: this.annotationGrids)
                    {
                        this.selectAllFromGrid(annoGrid);
                    }
                    
                    fireChange = true;
                }
                else
                {
                    int dataRowCount = this.dataGrid.getStore().getModels().size();
                    
                    // the + 1 adjusts for the probeset ID
                    if(dataRowCount + 1 <= Query.MAX_PERMITTED_TERMS)
                    {
                        // there are too many terms in total but at least we
                        // can select the data terms
                        this.selectAllFromGrid(this.dataGrid);
                        fireChange = true;
                    }
                }
                
                this.initializationComplete = true;
            }
            
            store.filter(null);
            
            // we need to tell the user if there were selection updates
            if(fireChange)
            {
                System.out.println("firing change for preselected models");
                this.fireChangeEvent();
            }
        }
        else
        {
            System.out.println("models are null!");
        }
    }
    
    /**
     * This function is called each time a grid is selected/deselected
     * @param se
     *          the event
     * @param grid
     *          the affected grid
     */
    private void termsDataStoreUpdated(StoreEvent<ModelData> se, Grid<ModelData> grid)
    {
        // we don't really care about the commit actions
        if(se.getOperation() != RecordUpdate.COMMIT)
        {
            System.out.println("terms of interest store update");
            
            se.getStore().commitChanges();
            this.fireChangeEvent();
        }
    }

    /**
     * Register listeners for changes to the comparison expression
     * @param changeListener
     *          the listener to register
     */
    public void addChangeListener(ChangeListener<TermsOfInterestContainer> changeListener)
    {
        this.changeListeners.add(changeListener);
    }
    
    /**
     * Remove listener
     * @see #addChangeListener(ChangeListener)
     * @param changeListener
     *          the change listener to remove from the listener list
     */
    public void removeChangeListener(ChangeListener<TermsOfInterestContainer> changeListener)
    {
        this.changeListeners.remove(changeListener);
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
                for(ChangeListener<TermsOfInterestContainer> changeListener:
                    TermsOfInterestContainer.this.changeListeners)
                {
                    changeListener.changeOccured(TermsOfInterestContainer.this);
                }
            }
        });
    }
}

package org.jax.pubarray.gwtqueryapp.client;

import java.util.List;

import org.jax.gwtutil.client.event.ActionListener;
import org.jax.gwtutil.client.event.ChangeListener;
import org.jax.pubarray.gwtcommon.client.GxtUtil;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This is the main entry point for the GWT query application. This gets
 * loaded up as JavaScript into the query page.
 */
public class QueryApplication implements EntryPoint
{
    private QueryServiceAsync queryService = null;
    private GraphingServiceAsync graphingService = null;

    private TermsOfInterestContainer termsOfInterestTableContainer = null;
    
    private QueryFilterTable queryFilterTable = null;
    private QueryResultsContainer queryResultsContainer = null;
    private Panel queryResultsContainerPanel = null;
    
    private ProbeDetailsContainer probeDetailsContainer = null;
    private PerGeneImageContainer perGeneImageContainer = null;
    private TableColumnMetadata[] designTerms = null;
    
    /**
     * {@inheritDoc}
     */
    public void onModuleLoad()
    {
        // NOTE: this is a complete hack which is needed to avoid
        //       "Invalid memory access of location 0x8 eip=0x4a8aeb" error
        //       that occurs in dev mode otherwise. For more info on this hack
        //       please see: http://www.extjs.com/forum/showthread.php?t=87668
        @SuppressWarnings("unused") Layout junk = new AnchorLayout();
        
        // get a reference to the GWT services
        this.queryService = GWT.create(QueryService.class);
        this.graphingService = GWT.create(GraphingService.class);
        
        // TODO show some kind of load progress here
        
        this.queryService.getAnnotationCategories(
                new AsyncCallback<String[]>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void onFailure(Throwable caught)
                    {
                        caught.printStackTrace();
                        
                        GxtUtil.showMessageDialog(
                                "Error Loading Page",
                                "Failed to load page. Error message: " +
                                caught.getMessage());
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    public void onSuccess(String[] categories)
                    {
                        QueryApplication.this.initializeData(categories);
                    }
                });
    }
    
    private void initializeData(String[] annotationCategories)
    {
        // initialize the "terms of interest" section
        ChangeListener<TermsOfInterestContainer> termsOfInterestChangeListener =
            new ChangeListener<TermsOfInterestContainer>()
            {
                /**
                 * {@inheritDoc}
                 */
                public void changeOccured(TermsOfInterestContainer source)
                {
                    List<QualifiedColumnMetadata> selectedTerms =
                        source.getSelectedTerms();
                    QueryApplication.this.selectedTermsChanged(selectedTerms);
                }
            };
        this.termsOfInterestTableContainer = new TermsOfInterestContainer(
                this.queryService,
                annotationCategories,
                termsOfInterestChangeListener);
        RootPanel.get("termsOfInterestTableContainer").add(
                this.termsOfInterestTableContainer);
        
        // initialize probe filter
        this.queryFilterTable = new QueryFilterTable();
        RootPanel.get("queryFilterContainer").add(this.queryFilterTable);
        this.termsOfInterestTableContainer.addChangeListener(
                termsOfInterestChangeListener);
        
        // register for the "run query" event
        this.queryFilterTable.addActionListener(
                new ActionListener<QueryFilterTable>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void actionPerformed(QueryFilterTable source)
                    {
                        QueryApplication.this.runQuery();
                    }
                });
        
        this.queryResultsContainerPanel = RootPanel.get("queryResultsTableContainer");
        
        this.queryService.getDesignTerms(
                new AsyncCallback<TableColumnMetadata[]>()
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
                    public void onSuccess(TableColumnMetadata[] result)
                    {
                        QueryApplication.this.designTerms = result;
                        QueryApplication.this.updateProbeDetailsGraph();
                    }
                });
        
        this.perGeneImageContainer = new PerGeneImageContainer(this.queryService);
        RootPanel.get("perGeneImageContainer").add(this.perGeneImageContainer);
    }
    
    private void selectedTermsChanged(List<QualifiedColumnMetadata> selectedTerms)
    {
        this.queryFilterTable.setAttributes(selectedTerms.toArray(
                new QualifiedColumnMetadata[selectedTerms.size()]));
        if(this.queryResultsContainer == null &&
           this.termsOfInterestTableContainer.isInitializationComplete())
        {
            this.runQuery();
        }
    }
    
    private void resetQueryResults()
    {
        if(this.queryResultsContainer != null)
        {
            this.queryResultsContainer.clearChangeListeners();
            this.queryResultsContainerPanel.remove(this.queryResultsContainer);
            
            if(this.probeDetailsContainer != null)
            {
                this.probeDetailsContainer.updateProbeIntensityGraph(
                        new String[0],
                        new QualifiedColumnMetadata[0]);
            }
        }
    }

    private void runQuery()
    {
        // if there is an existing query results container we need to clear that
        // before adding the new one
        this.resetQueryResults();
        
        // build the new query results and add them
        List<QualifiedColumnMetadata> selectedTerms =
            this.termsOfInterestTableContainer.getSelectedTerms();
        Query query = new Query();
        query.setFilters(this.queryFilterTable.getFilters());
        query.setTermsOfInterest(
                selectedTerms.toArray(
                        new QualifiedColumnMetadata[selectedTerms.size()]));
        query.setOrderByColumn(this.queryFilterTable.getOrderByTerm());
        query.setSortDirection(this.queryFilterTable.getOrderByDirection());
        
        if(this.validateQuery(query))
        {
            this.queryResultsContainer = new QueryResultsContainer(
                    this.queryService,
                    query);
            this.queryResultsContainerPanel.add(this.queryResultsContainer);
            
            this.queryResultsContainer.addChangeListener(
                    new ChangeListener<QueryResultsContainer>()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        public void changeOccured(QueryResultsContainer source)
                        {
                            QueryApplication.this.updateProbeDetailsGraph();
                            QueryApplication.this.updatePerGeneImages();
                        }
                    });
        }
        
        // running a query causes a new table to be added to the page which
        // can in turn cause a new vertical scroll bar to be added to the
        // page. if the scroll bar is added then we have to update the layout
        this.termsOfInterestTableContainer.maybeLayoutChanged();
    }

    private boolean validateQuery(Query query)
    {
        if(query.getTermsOfInterest().length > Query.MAX_PERMITTED_TERMS)
        {
            // the (- 1) accounts for the probeset id which is always
            // implicitly selected
            GxtUtil.showMessageDialog(
                    "Too Many Columns Selected",
                    "Please reduce the number of selected terms to a maximum " +
                    "of " + (Query.MAX_PERMITTED_TERMS - 1) + " and try again.");
            return false;
        }
        else if(query.getTableCount() > Query.MAX_PERMITTED_TABLE_COUNT)
        {
            GxtUtil.showMessageDialog(
                    "Too Many Table Groupings Selected",
                    "Please reduce the number of table groups that you have " +
                    "selected from Step 1 to a maximum of " +
                    Query.MAX_PERMITTED_TABLE_COUNT +
                    ". Note that column selections are formatted like " +
                    "\"table_group_name: column_name\"");
            return false;
        }
        else
        {
            return true;
        }
    }

    private void updateProbeDetailsGraph()
    {
        System.out.println("updating probe details");
        if(this.queryResultsContainer != null &&
           this.termsOfInterestTableContainer != null &&
           this.designTerms != null)
        {
            if(this.probeDetailsContainer == null)
            {
                this.probeDetailsContainer = new ProbeDetailsContainer(
                        this.graphingService,
                        this.designTerms);
                RootPanel.get("probeDetailsContainer").add(this.probeDetailsContainer);
                
                this.termsOfInterestTableContainer.maybeLayoutChanged();
                this.queryResultsContainer.maybeLayoutChanged();
            }
            
            List<String> selectedProbes = this.queryResultsContainer.getSelectedProbeIds();
            List<QualifiedColumnMetadata> termsOfInterest =
                this.termsOfInterestTableContainer.getSelectedTerms();
            
            if(selectedProbes != null && termsOfInterest != null)
            {
                String[] selectedProbesArray =
                    selectedProbes.toArray(new String[selectedProbes.size()]);
                QualifiedColumnMetadata[] termsOfInterestArray = termsOfInterest.toArray(
                        new QualifiedColumnMetadata[termsOfInterest.size()]);
                
                this.probeDetailsContainer.updateProbeIntensityGraph(
                        selectedProbesArray,
                        termsOfInterestArray);
            }
        }
        else
        {
            if(this.probeDetailsContainer != null)
            {
                RootPanel.get("probeDetailsContainer").remove(this.probeDetailsContainer);
                this.probeDetailsContainer = null;
                
                if(this.termsOfInterestTableContainer != null)
                {
                    this.termsOfInterestTableContainer.maybeLayoutChanged();
                }
                
                if(this.queryResultsContainer != null)
                {
                    this.queryResultsContainer.maybeLayoutChanged();
                }
            }
        }
    }
    
    private void updatePerGeneImages()
    {
        String[] selectedProbes = new String[0];
        if(this.queryResultsContainer != null)
        {
            List<String> selectedProbeList = this.queryResultsContainer.getSelectedProbeIds();
            selectedProbes = selectedProbeList.toArray(new String[selectedProbeList.size()]);
        }
        this.perGeneImageContainer.updateSelectedGenes(selectedProbes);
    }
}

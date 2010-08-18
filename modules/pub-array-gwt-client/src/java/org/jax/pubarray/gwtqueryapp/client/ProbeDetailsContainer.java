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

import java.util.Arrays;
import java.util.List;

import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadataModelUtil;
import org.jax.pubarray.gwtqueryapp.client.ProbeIntensityGraphConfiguration.GroupedGraphType;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Image;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ProbeDetailsContainer extends LayoutContainer
{
    private static final String PROBE_INTENSITY_PLOT_IMG_PREFIX =
        "restful/graph-images/";
    private static final int DEFAULT_GRAPH_HEIGHT = 600;
    
    private static final String GROUP_GRAPH_ENUM_KEY = "GROUP_GRAPH_ENUM_KEY";
    private static final String GROUP_GRAPH_NAME_KEY = "GROUP_GRAPH_NAME_KEY";
    
    private final GraphingServiceAsync graphingService;
    
    private final LayoutContainer probeIntensityPlotContainer;
    private final Image probeIntensityPlot;
    
    private final ContentPanel mainPanel;
    
    private final ListStore<ModelData> designTermsStore;
    private final ComboBox<ModelData> orderByComboBox;
    private final CheckBox groupReplicatesCheckBox;
    private final ComboBox<ModelData> groupGraphTypeComboBox;
    
    private final ProbeIntensityGraphConfiguration graphConfiguration;
    
    private String plotKey;
    
    /**
     * Constructor
     * @param graphingService
     *          the graphing service to use
     * @param designTerms
     *          the metadata
     */
    public ProbeDetailsContainer(
            GraphingServiceAsync graphingService,
            TableColumnMetadata[] designTerms)
    {
        this.graphingService = graphingService;
        this.probeIntensityPlotContainer = new LayoutContainer();
        this.probeIntensityPlot = new Image();
        this.mainPanel = new ContentPanel();
        
        this.graphConfiguration = new ProbeIntensityGraphConfiguration();
        
        this.designTermsStore = new ListStore<ModelData>();
        List<ModelData> designTermModels =
            TableColumnMetadataModelUtil.fromPojosToModels(
                    true,
                    Arrays.asList(designTerms));
        this.designTermsStore.add(designTermModels);
        
        this.orderByComboBox = new ComboBox<ModelData>();
        this.orderByComboBox.setStore(this.designTermsStore);
        this.orderByComboBox.setDisplayField(TableColumnMetadata.NAME_PROP_STRING);
        this.orderByComboBox.setEditable(false);
        this.orderByComboBox.setAllowBlank(true);
        this.orderByComboBox.setForceSelection(true);
        this.orderByComboBox.setTypeAhead(false);
        this.orderByComboBox.setEmptyText("Select Design Term");
        this.orderByComboBox.setTriggerAction(TriggerAction.ALL);
        this.orderByComboBox.addSelectionChangedListener(
                new SelectionChangedListener<ModelData>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void selectionChanged(SelectionChangedEvent<ModelData> se)
                    {
                        TableColumnMetadata selectedDesignTerm =
                            TableColumnMetadataModelUtil.fromModelToPojo(
                                    se.getSelectedItem());
                        ProbeDetailsContainer.this.setSelectedDesignTerm(
                                selectedDesignTerm);
                        ProbeDetailsContainer.this.groupReplicatesCheckBox.setEnabled(true);
                    }
                });
        
        this.groupReplicatesCheckBox = new CheckBox("Group Arrays Using");
        this.groupReplicatesCheckBox.setValue(Boolean.FALSE, true);
        this.groupReplicatesCheckBox.setEnabled(false);
        this.groupReplicatesCheckBox.addValueChangeHandler(
                new ValueChangeHandler<Boolean>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void onValueChange(ValueChangeEvent<Boolean> event)
                    {
                        ProbeDetailsContainer.this.groupReplicatesChanged();
                    }
                });
        
        final ListStore<ModelData> groupGraphTypeStore = new ListStore<ModelData>();
        final BaseModel scatterPlotModel = new BaseModel();
        scatterPlotModel.set(GROUP_GRAPH_ENUM_KEY, GroupedGraphType.SCATTER_PLOT);
        scatterPlotModel.set(GROUP_GRAPH_NAME_KEY, "Scatter Plot");
        groupGraphTypeStore.add(scatterPlotModel);
        
        final BaseModel boxPlotModel = new BaseModel();
        boxPlotModel.set(GROUP_GRAPH_ENUM_KEY, GroupedGraphType.BOX_PLOT);
        boxPlotModel.set(GROUP_GRAPH_NAME_KEY, "Box Plot");
        groupGraphTypeStore.add(boxPlotModel);
        
        this.groupGraphTypeComboBox = new ComboBox<ModelData>();
        this.groupGraphTypeComboBox.setEnabled(false);
        this.groupGraphTypeComboBox.setStore(groupGraphTypeStore);
        this.groupGraphTypeComboBox.setDisplayField(GROUP_GRAPH_NAME_KEY);
        this.groupGraphTypeComboBox.setEditable(false);
        this.groupGraphTypeComboBox.setAllowBlank(false);
        this.groupGraphTypeComboBox.setForceSelection(true);
        this.groupGraphTypeComboBox.setTypeAhead(false);
        this.groupGraphTypeComboBox.setValue(scatterPlotModel);
        this.groupGraphTypeComboBox.setTriggerAction(TriggerAction.ALL);
        this.groupGraphTypeComboBox.addSelectionChangedListener(
                new SelectionChangedListener<ModelData>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void selectionChanged(SelectionChangedEvent<ModelData> se)
                    {
                        ProbeDetailsContainer.this.groupGraphTypeChanged();
                    }
                });
        
        this.probeIntensityPlot.addLoadHandler(new LoadHandler()
        {
            /**
             * {@inheritDoc}
             */
            public void onLoad(LoadEvent event)
            {
                ProbeDetailsContainer.this.probeIntensityPlotLoaded();
            }
        });
        this.probeIntensityPlot.addErrorHandler(new ErrorHandler()
        {
            /**
             * {@inheritDoc}
             */
            public void onError(ErrorEvent event)
            {
                ProbeDetailsContainer.this.showErrorMask(
                        "Error Loading Image");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        
        // add components to the content panel
        this.mainPanel.setHeading("Expression Details");
        this.mainPanel.setFrame(true);
        this.mainPanel.setCollapsible(true);
        this.mainPanel.setAnimCollapse(false);
        this.mainPanel.setIconStyle("icon-table");
        this.mainPanel.setAutoWidth(true);
        
        this.add(this.mainPanel);
        
        this.probeIntensityPlot.setVisible(false);
        this.probeIntensityPlotContainer.setHeight(DEFAULT_GRAPH_HEIGHT);
        this.probeIntensityPlotContainer.add(this.probeIntensityPlot);
        this.probeIntensityPlotContainer.setAutoWidth(true);
        this.mainPanel.add(this.probeIntensityPlotContainer);
        
        ToolBar toolBar = new ToolBar();
        toolBar.setSpacing(3);
        toolBar.add(new LabelToolItem("Order Arrays By:"));
        toolBar.add(this.orderByComboBox);
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterField(this.groupReplicatesCheckBox));
        toolBar.add(this.groupGraphTypeComboBox);
        this.mainPanel.setTopComponent(toolBar);
        
        Window.addResizeHandler(new ResizeHandler()
        {
            /**
             * {@inheritDoc}
             */
            public void onResize(ResizeEvent event)
            {
                ProbeDetailsContainer.this.layout();
                
                if(ProbeDetailsContainer.this.probeIntensityPlot.isVisible())
                {
                    ProbeDetailsContainer.this.updateProbeIntensityPlotURL();
                }
            }
        });
    }

    private void groupGraphTypeChanged()
    {
        ModelData selectedValue = this.groupGraphTypeComboBox.getValue();
        GroupedGraphType graphType = selectedValue.get(GROUP_GRAPH_ENUM_KEY);
        System.out.println("group replicates graph type changed: " + graphType);
        this.graphConfiguration.setGroupedGraphType(graphType);
        
        this.maybeRebuildProbeIntensityPlot();
    }

    private void groupReplicatesChanged()
    {
        Boolean value = this.groupReplicatesCheckBox.getValue();
        System.out.println("group replicates checkbox changed: " + value);
        this.graphConfiguration.setGroupReplicates(value);
        this.groupGraphTypeComboBox.setEnabled(value);
        
        this.maybeRebuildProbeIntensityPlot();
    }

    private void setSelectedDesignTerm(TableColumnMetadata selectedDesignTerm)
    {
        this.graphConfiguration.setOrderProbesBy(selectedDesignTerm);
        this.maybeRebuildProbeIntensityPlot();
    }

    /**
     * Update the probe intensity graph with new selected probes and terms of
     * interest
     * @param selectedProbes
     *          the selected probes
     * @param termsOfInterest
     *          the terms of interest
     */
    public void updateProbeIntensityGraph(
            String[] selectedProbes,
            QualifiedColumnMetadata[] termsOfInterest)
    {
        System.out.println("updating probe intensity graph");
        
        this.graphConfiguration.setProbeIds(selectedProbes);
        this.graphConfiguration.setTermsOfInterest(termsOfInterest);
        
        this.maybeRebuildProbeIntensityPlot();
    }
    
    private void maybeRebuildProbeIntensityPlot()
    {
        String[] selectedProbes = this.graphConfiguration.getProbeIds();
        
        if(selectedProbes == null || selectedProbes.length == 0)
        {
            this.probeIntensityPlotContainer.mask("No Probesets Selected");
            this.probeIntensityPlot.setVisible(false);
        }
        else
        {
            this.rebuildProbeIntensityPlot();
        }
    }
    
    private void rebuildProbeIntensityPlot()
    {
        this.graphingService.buildProbeIntensityGraph(
                this.graphConfiguration,
                new AsyncCallback<String>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void onFailure(Throwable caught)
                    {
                        caught.printStackTrace();
                        ProbeDetailsContainer.this.showErrorMask(
                                "Error: " + caught.getMessage());
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    public void onSuccess(String key)
                    {
                        System.out.println(
                                "build graph succeeded. rewriting image url " +
                                "for key: " + key);
                        ProbeDetailsContainer.this.plotKey = key;
                        ProbeDetailsContainer.this.updateProbeIntensityPlotURL();
                    }
                });
    }
    
    private void showErrorMask(String message)
    {
        this.probeIntensityPlotContainer.mask(message);
    }
    
    private void probeIntensityPlotLoaded()
    {
        System.out.println(
                "loaded probe graph of size: " +
                this.probeIntensityPlot.getWidth() + "x" +
                this.probeIntensityPlot.getHeight());
        this.probeIntensityPlotContainer.unmask();
        this.probeIntensityPlot.setVisible(true);
    }

    private void updateProbeIntensityPlotURL()
    {
        if(this.plotKey != null)
        {
            int plotWidth = this.probeIntensityPlotContainer.getWidth(true);
            int plotHeight = this.probeIntensityPlotContainer.getHeight(true);
            
            if(plotWidth >= 20 && plotHeight >= 20)
            {
                this.probeIntensityPlotContainer.mask("Loading Graph");
                this.probeIntensityPlot.setUrl(
                        PROBE_INTENSITY_PLOT_IMG_PREFIX +
                        "probe-intensity-graph-" + this.plotKey + "-" +
                        plotWidth + "x" +
                        plotHeight + ".png");
            }
        }
    }
}

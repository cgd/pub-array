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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jax.gwtutil.client.WidgetUtilities;
import org.jax.gwtutil.client.event.ActionBroadcaster;
import org.jax.gwtutil.client.event.ActionListener;
import org.jax.pubarray.gwtcommon.client.Filter;
import org.jax.pubarray.gwtcommon.client.FilterModelUtil;
import org.jax.pubarray.gwtcommon.client.GxtUtil;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadataModelUtil;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.QueryModelUtil;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadataModelUtil;
import org.jax.pubarray.gwtcommon.client.Filter.FilterCondition;
import org.jax.pubarray.gwtcommon.client.Query.SortDirection;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.DataType;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * A query filter table widget
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QueryFilterTable
extends LayoutContainer
implements ActionBroadcaster<QueryFilterTable>
{
    private static final int ATTRIBUTE_COMBO_WIDTH = 200;
    
    private static final String ATTRIBUTE_DISPLAY_PROPERTY = "ATTRIBUTE_DISPLAY_PROPERTY";
    
    private final SelectionListener<ButtonEvent> removeClickListener = new SelectionListener<ButtonEvent>()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void componentSelected(ButtonEvent e)
        {
            QueryFilterTable.this.removeRowClicked((Button)e.getSource());
        }
    };
    
    private final Button appendFilterButton;
    private final FlexTable comparisonTable;
    private final ComboBox<ModelData> orderByComboBox;
    private final ComboBox<ModelData> orderByDirectionComboBox;
    private final Button runQueryButton;
    private final ListStore<ModelData> attributeStore;
    private final List<ActionListener<QueryFilterTable>> listenerList;
    
    private boolean attributesChanged = false;
    private QualifiedColumnMetadata[] attributes;
    
    private class AutoUpdateTermsComboBox extends ComboBox<ModelData>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void expand()
        {
            // before expanding see if we need to update the terms
            QueryFilterTable.this.maybeUpdateAttributes();
            super.expand();
        }
    }
    
    /**
     * Constructor for the filter table.
     */
    public QueryFilterTable()
    {
        this.attributeStore = new ListStore<ModelData>();
        this.comparisonTable = new FlexTable();
        this.appendFilterButton = new Button();
        this.runQueryButton = new Button();
        this.orderByComboBox = new AutoUpdateTermsComboBox();
        this.orderByDirectionComboBox = new ComboBox<ModelData>();
        
        this.listenerList = new ArrayList<ActionListener<QueryFilterTable>>();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(Element parent, int index)
    {
        super.onRender(parent, index);
        
        final FlexTable addFiltersTable = new FlexTable();
        
        this.appendFilterButton.setText("Add Search Filter");
        this.appendFilterButton.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void componentSelected(ButtonEvent e)
            {
                QueryFilterTable.this.appendComparisonRow();
            }
        });
        this.appendFilterButton.setIconAlign(IconAlign.LEFT);
        this.appendFilterButton.setIcon(IconHelper.createPath(
                "images/add-16x16.png",
                16,
                16));
        
        addFiltersTable.setText(0, 0, "Search Filters:");
        addFiltersTable.setWidget(0, 1, this.appendFilterButton);
        this.add(addFiltersTable);
        
        this.add(this.comparisonTable);
        
        this.orderByComboBox.setWidth(ATTRIBUTE_COMBO_WIDTH);
        this.orderByComboBox.setStore(this.attributeStore);
        this.orderByComboBox.setDisplayField(ATTRIBUTE_DISPLAY_PROPERTY);
        this.orderByComboBox.setTypeAhead(true);
        this.orderByComboBox.setForceSelection(false);
        this.orderByComboBox.setAllowBlank(true);
        this.orderByComboBox.setEmptyText("Any Order...");
        this.orderByComboBox.setEditable(false);
        this.orderByComboBox.setTriggerAction(TriggerAction.ALL);
        
        ListStore<ModelData> orderByDirectionStore = new ListStore<ModelData>();
        ModelData ascendingOrderModel = new BaseModel();
        ascendingOrderModel.set(
                QueryModelUtil.ORDER_BY_DIRECTION_COLUMN_ID,
                SortDirection.ASCENDING);
        orderByDirectionStore.add(ascendingOrderModel);
        ModelData descendingOrderModel = new BaseModel();
        descendingOrderModel.set(
                QueryModelUtil.ORDER_BY_DIRECTION_COLUMN_ID,
                SortDirection.DESCENDING);
        orderByDirectionStore.add(descendingOrderModel);
        
        this.orderByDirectionComboBox.setStore(orderByDirectionStore);
        this.orderByDirectionComboBox.setDisplayField(
                QueryModelUtil.ORDER_BY_DIRECTION_COLUMN_ID);
        this.orderByDirectionComboBox.setTypeAhead(false);
        this.orderByDirectionComboBox.setForceSelection(true);
        this.orderByDirectionComboBox.setAllowBlank(false);
        this.orderByDirectionComboBox.setSelection(
                Collections.singletonList(ascendingOrderModel));
        this.orderByDirectionComboBox.setEditable(false);
        this.orderByDirectionComboBox.setTriggerAction(TriggerAction.ALL);
        
        final FlexTable orderAndSearchTable = new FlexTable();
        orderAndSearchTable.setText(0, 0, "Order Results By:");
        orderAndSearchTable.setWidget(0, 1, this.orderByComboBox);
        orderAndSearchTable.setWidget(0, 2, this.orderByDirectionComboBox);
        
        this.runQueryButton.setText("Search");
        this.runQueryButton.setIconAlign(IconAlign.LEFT);
        this.runQueryButton.setIcon(IconHelper.createPath(
                "images/work-16x16.png",
                16,
                16));
        orderAndSearchTable.setWidget(1, 0, this.runQueryButton);
        orderAndSearchTable.getFlexCellFormatter().setColSpan(1, 0, 2);
        
        this.add(orderAndSearchTable);
        
        this.runQueryButton.addSelectionListener(
                new SelectionListener<ButtonEvent>()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        QueryFilterTable.this.runQueryClicked();
                    }
                });
        
        // start the user out with a single empty row to give them a hint
        // how filtering is supposed to work
        this.appendComparisonRow();
    }
    
    private void runQueryClicked()
    {
        String validationMessage = this.getValidateFiltersErrorMessage();
        if(validationMessage == null)
        {
            // no errors. we're OK to notify
            this.fireActionEvent();
        }
        else
        {
            System.out.println(
                    "Filter validation failed with message: " + validationMessage);
            GxtUtil.showMessageDialog(
                    "Filter Validation Failed",
                    validationMessage);
        }
    }
    
    /**
     * Get an error message for the filters if validation fails
     * @return the message if validation fails, or null if it succeeds
     */
    @SuppressWarnings("incomplete-switch")
    private String getValidateFiltersErrorMessage()
    {
        Filter[] filters = this.getFilters();
        
        for(Filter filter: filters)
        {
            if(filter.getColumn() == null)
            {
                return "Please either select a term for each filter that you " +
                       "added or remove empty filters.";
            }
            else if(filter.getCondition() == null)
            {
                return "Please either select a filter operation for " +
                       filter.getColumn().getName() + " or remove the filter.";
            }
            else if(filter.getValue() == null || filter.getValue().trim().length() == 0)
            {
                return "The filter value for " + filter.getColumn().getName() +
                       " is empty. Please either enter a value or remove the " +
                       "filter.";
            }
            else
            {
                String trimmedValue = filter.getValue().trim();
                switch(filter.getColumn().getDataType())
                {
                    case INTEGER:
                    {
                        if(filter.getCondition() == FilterCondition.EXACTLY_MATCHES_ANY)
                        {
                            String[] tokens = trimmedValue.split("\\s+");
                            for(String token: tokens)
                            {
                                try
                                {
                                    Integer.parseInt(token);
                                }
                                catch(NumberFormatException ex)
                                {
                                    return token + " does not appear to be " +
                                           "formatted as an integer. " +
                                           "Please either enter a " +
                                           "valid integer or remove the " +
                                           filter.getColumn().getName() + " filter.";
                                }
                            }
                        }
                        else
                        {
                            try
                            {
                                Integer.parseInt(trimmedValue);
                            }
                            catch(NumberFormatException ex)
                            {
                                return trimmedValue + " does not appear to be " +
                                	   "formatted as an integer. " +
                                	   "Please either enter a " +
                                	   "valid integer or remove the " +
                                	   filter.getColumn().getName() + " filter.";
                            }
                        }
                    }
                    break;
                    
                    case REAL:
                    {
                        if(filter.getCondition() == FilterCondition.EXACTLY_MATCHES_ANY)
                        {
                            String[] tokens = trimmedValue.split("\\s+");
                            for(String token: tokens)
                            {
                                try
                                {
                                    Double.parseDouble(token);
                                }
                                catch(NumberFormatException ex)
                                {
                                    return token + " does not appear to be " +
                                           "formatted as a number. " +
                                           "Please either enter a " +
                                           "valid number or remove the " +
                                           filter.getColumn().getName() + " filter.";
                                }
                            }
                        }
                        else
                        {
                            try
                            {
                                Double.parseDouble(trimmedValue);
                            }
                            catch(NumberFormatException ex)
                            {
                                return trimmedValue + " does not appear to be " +
                                       "formatted as a number. " +
                                       "Please either enter a " +
                                       "valid number or remove the " +
                                       filter.getColumn().getName() + " filter.";
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        // if we got this far then everything checks out OK
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void addActionListener(ActionListener<QueryFilterTable> listener)
    {
        this.listenerList.add(listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeActionListener(ActionListener<QueryFilterTable> listener)
    {
        this.listenerList.remove(listener);
    }
    
    /**
     * Tell all of our listeners that the action was performed
     */
    private void fireActionEvent()
    {
        for(ActionListener<QueryFilterTable> listener: this.listenerList)
        {
            listener.actionPerformed(this);
        }
    }
    
    /**
     * Getter for the filters
     * @return
     *          the filters. an empty list is OK but a null list means that
     *          there were validation problems
     */
    public Filter[] getFilters()
    {
        int rowCount = this.comparisonTable.getRowCount();
        List<Filter> filters = new ArrayList<Filter>(rowCount);
        
        for(int row = 0; row < rowCount; row++)
        {
            Filter filter = this.getFilter(row);
            
            // if nothing is set for this filter let's just remove it from
            // consideration
            if(filter.getColumn() != null || filter.getCondition() != null ||
               (filter.getValue() != null && filter.getValue().length() >= 1))
            {
                filters.add(filter);
            }
        }
        
        return filters.toArray(new Filter[filters.size()]);
    }
    
    @SuppressWarnings("unchecked")
    private Filter getFilter(int row)
    {
        // get the filter column from the drop down
        ComboBox<ModelData> attributeComboBox =
            (ComboBox<ModelData>)this.comparisonTable.getWidget(row, 0);
        QualifiedColumnMetadata colMeta =
            this.getSelectedAttribute(attributeComboBox);
        
        // now get the filter condition
        ComboBox<ModelData> filterConditionComboBox =
            (ComboBox<ModelData>)this.comparisonTable.getWidget(row, 1);
        FilterCondition filterCondition =
            this.getFilterCondition(filterConditionComboBox);
        
        // now get the value
        TextField<String> valueTextField =
            (TextField<String>)this.comparisonTable.getWidget(row, 2);
        String value = valueTextField.getValue();
        
        return new Filter(
                colMeta,
                filterCondition,
                value);
    }
    
    /**
     * Getter for the order by term
     * @return
     *          the term that should be used to order the results or null
     *          if we don't care about ordering
     */
    public QualifiedColumnMetadata getOrderByTerm()
    {
        return this.getSelectedAttribute(this.orderByComboBox);
    }
    
    /**
     * Get the direction that we should be ordering by
     * @return
     *          the direction
     */
    public SortDirection getOrderByDirection()
    {
        ModelData model = this.orderByDirectionComboBox.getValue();
        SortDirection direction =
            model.get(QueryModelUtil.ORDER_BY_DIRECTION_COLUMN_ID);
        return direction;
    }

    /**
     * Setter for the list of attributes that we can filter
     * @param attributes
     *          the attributes
     */
    public void setAttributes(QualifiedColumnMetadata[] attributes)
    {
        boolean attributesWereEmpty =
            this.attributes == null || this.attributes.length == 0;
        
        this.attributes = attributes;
        this.attributesChanged = true;
        
        // in the case where attributes were empty before we want to force
        // a change here
        if(attributesWereEmpty && attributes != null && attributes.length >= 1)
        {
            this.maybeUpdateAttributes();
        }
    }
    
    private void maybeUpdateAttributes()
    {
        if(this.attributesChanged)
        {
            this.attributesChanged = false;
            Set<String> allColNames = new HashSet<String>();
            Set<String> colNamesWithDuplicates = new HashSet<String>();
            
            this.attributeStore.removeAll();
            for(QualifiedColumnMetadata colMeta: this.attributes)
            {
                if(!allColNames.add(colMeta.getName()))
                {
                    colNamesWithDuplicates.add(colMeta.getName());
                }
            }
            
            for(QualifiedColumnMetadata colMeta: this.attributes)
            {
                ModelData currModel = QualifiedColumnMetadataModelUtil.fromPojoToModel(
                        true,
                        colMeta);
                if(colNamesWithDuplicates.contains(colMeta.getName()))
                {
                    // if there are duplicate names we need to fully qualify the
                    // attribute using the table name
                    currModel.set(
                            ATTRIBUTE_DISPLAY_PROPERTY,
                            colMeta.getName() + " (" + colMeta.getTableName() + ")");
                }
                else
                {
                    currModel.set(
                            ATTRIBUTE_DISPLAY_PROPERTY,
                            colMeta.getName());
                }
                
                this.attributeStore.add(currModel);
            }
        }
    }

    /**
     * Append a new comparison row at the given row index
     */
    private void appendComparisonRow()
    {
        int startingRowCount = this.comparisonTable.getRowCount();
        
        // check to make sure that the user isn't doing more comparisons than
        // we would like to allow
        if(startingRowCount >= Query.MAX_PERMITTED_FILTER_COUNT)
        {
            GxtUtil.showMessageDialog(
                    "Too Many Filters",
                    "Sorry, but this table allows a maximum of " +
                    Query.MAX_PERMITTED_FILTER_COUNT + " filters.");
        }
        else
        {
            final ComboBox<ModelData> attributeComboBox = new AutoUpdateTermsComboBox();
            final ComboBox<ModelData> filterConditionComboBox = new ComboBox<ModelData>();
            final TextField<String> filterValueTextBox = new TextField<String>();
            
            // add the attribute name box
            attributeComboBox.setWidth(ATTRIBUTE_COMBO_WIDTH);
            attributeComboBox.setDisplayField(ATTRIBUTE_DISPLAY_PROPERTY);
            attributeComboBox.setStore(this.attributeStore);
            attributeComboBox.setTypeAhead(true);
            attributeComboBox.setForceSelection(true);
            attributeComboBox.setAllowBlank(false);
            attributeComboBox.setEmptyText("Select a Term...");
            attributeComboBox.setEditable(false);
            attributeComboBox.setTriggerAction(TriggerAction.ALL);
            attributeComboBox.addSelectionChangedListener(
                    new SelectionChangedListener<ModelData>()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<ModelData> se)
                        {
                            TableColumnMetadata selectedAttribute =
                                TableColumnMetadataModelUtil.fromModelToPojo(
                                        se.getSelectedItem());
                            System.out.println(
                                    "Attribute selection changed to: " +
                                    selectedAttribute);
                            
                            // update the store
                            FilterCondition selectedCondition = getFilterCondition(
                                    filterConditionComboBox);
                            ListStore<ModelData> theStore =
                                filterConditionComboBox.getStore();
                            theStore.removeAll();
                            if(selectedAttribute == null)
                            {
                                theStore.add(FilterModelUtil.getValidFilterConditionModelsForType(
                                        null));
                            }
                            else
                            {
                                DataType dataType = selectedAttribute.getDataType();
                                List<ModelData> validFilterConditionModels =
                                    FilterModelUtil.getValidFilterConditionModelsForType(
                                        dataType);
                                theStore.add(validFilterConditionModels);
                                
                                // if the curr condition is no longer valid we
                                // need to reset it
                                if(selectedCondition != null &&
                                   !validFilterConditionModels.isEmpty())
                                {
                                    List<FilterCondition> validAttributes =
                                        FilterModelUtil.getValidFilterConditionsForType(dataType);
                                    if(!validAttributes.contains(selectedCondition))
                                    {
                                        System.out.println("resetting filter value");
                                        filterConditionComboBox.setValue(
                                                validFilterConditionModels.get(0));
                                        filterConditionComboBox.repaint();
                                    }
                                }
                            }
                        }
                    });
            this.comparisonTable.setWidget(startingRowCount, 0, attributeComboBox);
            
            // add the condition operator ("==", "<" etc.) list box
            final String defaultEmptyText = "Enter a Value...";
            final String choiceEmptyText = "Eg: term1 term2 ...";
            filterConditionComboBox.setDisplayField(
                    FilterModelUtil.FILTER_CONDITION_MODEL_KEY);
            ListStore<ModelData> filterConditionStore = new ListStore<ModelData>();
            filterConditionStore.add(FilterModelUtil.getValidFilterConditionModelsForType(
                    null));
            filterConditionComboBox.setStore(filterConditionStore);
            filterConditionComboBox.setTypeAhead(true);
            filterConditionComboBox.setForceSelection(true);
            filterConditionComboBox.setAllowBlank(false);
            filterConditionComboBox.setEmptyText("Select a Filter Operation...");
            filterConditionComboBox.setEditable(false);
            filterConditionComboBox.setTriggerAction(TriggerAction.ALL);
            filterConditionComboBox.addSelectionChangedListener(
                    new SelectionChangedListener<ModelData>()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<ModelData> se)
                        {
                            FilterCondition condition = QueryFilterTable.this.getFilterCondition(
                                    filterConditionComboBox);
                            if(condition == FilterCondition.EXACTLY_MATCHES_ANY ||
                               condition == FilterCondition.PARTIALLY_MATCHES_ANY)
                            {
                                filterValueTextBox.setEmptyText(
                                        choiceEmptyText);
                            }
                            else
                            {
                                filterValueTextBox.setEmptyText(
                                        defaultEmptyText);
                            }
                        }
                    });
            this.comparisonTable.setWidget(startingRowCount, 1, filterConditionComboBox);
            
            // add the 2nd strains list box
            filterValueTextBox.setEmptyText(defaultEmptyText);
            filterValueTextBox.setAllowBlank(false);
            this.comparisonTable.setWidget(startingRowCount, 2, filterValueTextBox);
            
            // add the "remove" button
            Button removeButton = new Button(
                    "Remove Search Filter",
                    this.removeClickListener);
            removeButton.setIconAlign(IconAlign.LEFT);
            removeButton.setIcon(IconHelper.createPath(
                    "images/remove-16x16.png",
                    16,
                    16));
            
            this.comparisonTable.setWidget(startingRowCount, 3, removeButton);
            
            // propagate the change to the rest of the container
            this.layout();
        }
    }

    private QualifiedColumnMetadata getSelectedAttribute(
            ComboBox<ModelData> attributeComboBox)
    {
        ModelData modelValue = attributeComboBox.getValue();
        if(modelValue == null)
        {
            // clearSelections here is a hack. I need it because otherwise
            // GXT 2.0.1 removes the emptyText from the combo box
            attributeComboBox.clearSelections();
            return null;
        }
        else
        {
            return QualifiedColumnMetadataModelUtil.fromModelToPojo(modelValue);
        }
    }
    
    private FilterCondition getFilterCondition(
            ComboBox<ModelData> filterConditionComboBox)
    {
        ModelData filterConditionModel = filterConditionComboBox.getValue();
        if(filterConditionModel == null)
        {
            // clearSelections here is a hack. I need it because otherwise
            // GXT 2.0.1 removes the emptyText from the combo box
            filterConditionComboBox.clearSelections();
            return null;
        }
        else
        {
            return filterConditionModel.get(
                    FilterModelUtil.FILTER_CONDITION_MODEL_KEY);
        }
    }

    /**
     * Remove the comparison row at the given index
     * @param rowIndex
     *          the index of the row that we should remove
     */
    private void removeRow(int rowIndex)
    {
        if(rowIndex < 0 || rowIndex >= this.comparisonTable.getRowCount())
        {
            throw new IndexOutOfBoundsException();
        }
        
        this.comparisonTable.removeRow(rowIndex);
    }

    /**
     * One of the remove comparison row buttons was clicked
     * @param sender
     *          the actual widget that was clicked
     */
    private void removeRowClicked(Button sender)
    {
        int rowClicked = WidgetUtilities.getRowIndexOf(
                sender,
                this.comparisonTable);
        this.removeRow(rowClicked);
    }
}

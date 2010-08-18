/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
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
package org.jax.pubarray.installer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.util.gui.SwingUtilities;
import org.jax.util.gui.ValidatablePanel;
import org.jax.util.gui.WizardController;
import org.jax.util.gui.WizardDialog;
import org.jax.util.gui.WizardDialog.WizardDialogType;
import org.jax.util.io.FlatFileFormat;

/**
 * Panel that allows users to add/remove/edit annotation info
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SelectAnnotationsPanel extends ValidatablePanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7122995572999350709L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SelectAnnotationsPanel.class.getName());
    
    private final SharedDirectoryContainer startingDirectory;
    
    private DefaultTableModel annotationsMetaTableModel;

    /**
     * Constructor
     * @param startingDirectory the starting directory to use for browsing files
     */
    public SelectAnnotationsPanel(SharedDirectoryContainer startingDirectory)
    {
        this.startingDirectory = startingDirectory;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of any initialization that isn't handled by the gui builder
     */
    private void postGuiInit()
    {
        // make the buttons look pretty
        this.addTableButton.setIcon(getIcon("/images/action/add-16x16.png"));
        this.editSelectedButton.setIcon(getIcon("/images/action/edit-16x16.png"));
        this.removeSelectedButton.setIcon(getIcon("/images/action/remove-16x16.png"));
        
        this.addTableButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SelectAnnotationsPanel.this.addTable();
            }
        });
        
        this.editSelectedButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SelectAnnotationsPanel.this.editSelectedTable();
            }
        });
        
        this.removeSelectedButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SelectAnnotationsPanel.this.removeSelectedTables();
            }
        });
        
        this.annotationsMetaTableModel = new DefaultTableModel(
                new String[] {"File Name", "Table Name", "Format"},
                0)
        {
            /**
             * every serializable is supposed to have one of these
             * fantastic serial UID thingies
             */
            private static final long serialVersionUID = -6471892914094216189L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        this.annotationsMetaTable.setModel(this.annotationsMetaTableModel);
        this.annotationsMetaTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.annotationsMetaTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if(!e.getValueIsAdjusting())
                        {
                            SelectAnnotationsPanel.this.annotationsMetaTableSelectionChanged();
                        }
                    }
                });
        
        this.annotationsMetaTableSelectionChanged();
    }
    
    private void annotationsMetaTableSelectionChanged()
    {
        int selectionCount = this.annotationsMetaTable.getSelectedRowCount();
        
        this.editSelectedButton.setEnabled(selectionCount == 1);
        this.removeSelectedButton.setEnabled(selectionCount >= 1);
    }

    private void removeSelectedTables()
    {
        int[] selectedRows = this.annotationsMetaTable.getSelectedRows();
        
        // this function depends on the order of the rows. this sort may not
        // be needed but the javadoc doesn't claim any ordering property so lets
        // just do it to be on the safe side
        Arrays.sort(selectedRows);
        for(int i = selectedRows.length - 1; i >= 0; i--)
        {
            this.annotationsMetaTableModel.removeRow(selectedRows[i]);
        }
    }

    private void editSelectedTable()
    {
        if(this.annotationsMetaTable.getSelectedRowCount() == 1)
        {
            FlatFileDescriptionCell selectedCell =
                (FlatFileDescriptionCell)this.annotationsMetaTable.getValueAt(
                        this.annotationsMetaTable.getSelectedRow(),
                        0);
            this.showAddEditAnnotationDialog(
                    "Edit Annotation Table",
                    new AddEditAnnotationController(
                            selectedCell.getFlatFileDescription()));
        }
        else
        {
            throw new IllegalStateException(
                    "trying to edit an annotation table with " +
                    this.annotationsMetaTable.getSelectedRowCount() +
                    " rows selected");
        }
    }

    private void addTable()
    {
        this.showAddEditAnnotationDialog(
                "Add Annotation Table",
                new AddEditAnnotationController());
    }
    
    private void showAddEditAnnotationDialog(
            String title,
            AddEditAnnotationController controller)
    {
        final WizardDialog wizardDialog;
        Window parentWindow = SwingUtilities.getContainingWindow(this);
        if(parentWindow instanceof JDialog)
        {
            wizardDialog = new WizardDialog(
                    controller,
                    controller.getFlatFilePanel(),
                    (JDialog)parentWindow,
                    title,
                    true,
                    WizardDialogType.OK_CANCEL_DIALOG);
        }
        else if(parentWindow instanceof JFrame)
        {
            wizardDialog = new WizardDialog(
                    controller,
                    controller.getFlatFilePanel(),
                    (JFrame)parentWindow,
                    title,
                    true,
                    WizardDialogType.OK_CANCEL_DIALOG);
        }
        else
        {
            throw new IllegalStateException(
                    "this panel must be attached to a JPanel or JDialog in " +
                    "order to show the add/edit annotation table dialog");
        }
        
        wizardDialog.setVisible(true);
    }

    private static ImageIcon getIcon(String classpath)
    {
        return new ImageIcon(SelectAnnotationsPanel.class.getResource(classpath));
    }

    private class AddEditAnnotationController implements WizardController
    {
        private final SelectAndPreviewFlatFilePanel flatFilePanel;
        private final boolean isAddPanel;
        
        /**
         * Constructor for adding a new annotation flat file
         */
        public AddEditAnnotationController()
        {
            this.flatFilePanel = new SelectAndPreviewFlatFilePanel(
                    SelectAnnotationsPanel.this.startingDirectory,
                    false);
            this.isAddPanel = true;
        }
        
        /**
         * Constructor for editing an existing flat file description
         * @param descriptionToEdit it's all in the name isn't it :-)
         */
        public AddEditAnnotationController(FlatFileDescription descriptionToEdit)
        {
            this.flatFilePanel = new SelectAndPreviewFlatFilePanel(
                    descriptionToEdit,
                    SelectAnnotationsPanel.this.startingDirectory,
                    false);
            this.isAddPanel = false;
        }
        
        /**
         * Getter for the flat file panel
         * @return the flat file panel
         */
        public SelectAndPreviewFlatFilePanel getFlatFilePanel()
        {
            return this.flatFilePanel;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean cancel()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public boolean finish() throws IllegalStateException
        {
            if(this.flatFilePanel.validateData())
            {
                FlatFileDescription description =
                    this.flatFilePanel.getFlatFileDescription();
                
                // if this is an add then add it. if it's an edit then update
                // the current selection
                if(this.isAddPanel)
                {
                    SelectAnnotationsPanel.this.addFlatFileDescription(description);
                }
                else
                {
                    SelectAnnotationsPanel.this.updateSelectedFlatFileDescription(description);
                }
                
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean goNext() throws IllegalStateException
        {
            // this should never be called for this controller
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public boolean goPrevious() throws IllegalStateException
        {
            // this should never be called for this controller
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void help()
        {
            // TODO Auto-generated method stub
        }

        /**
         * {@inheritDoc}
         */
        public boolean isFinishValid()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isNextValid()
        {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isPreviousValid()
        {
            return false;
        }
    }
    
    /**
     * Create a table row for the given flat file description
     * @param description
     *          the description to create a row for
     * @return
     *          the row
     */
    private FlatFileDescriptionCell[] descriptionToTableRow(FlatFileDescription description)
    {
        return new FlatFileDescriptionCell[] {
                new FlatFileDescriptionCell(description, 0),
                new FlatFileDescriptionCell(description, 1),
                new FlatFileDescriptionCell(description, 2)};
    }
    
    private void addFlatFileDescription(FlatFileDescription description)
    {
        this.annotationsMetaTableModel.addRow(this.descriptionToTableRow(description));
    }

    private void updateSelectedFlatFileDescription(FlatFileDescription description)
    {
        if(this.annotationsMetaTable.getSelectedRowCount() == 1)
        {
            int selectedRowIndex = this.annotationsMetaTable.getSelectedRow();
            
            FlatFileDescriptionCell[] updatedRow = this.descriptionToTableRow(description);
            for(int colIndex = 0; colIndex < updatedRow.length; colIndex++)
            {
                this.annotationsMetaTableModel.setValueAt(
                        updatedRow[colIndex],
                        selectedRowIndex,
                        colIndex);
            }
        }
        else
        {
            LOG.severe(
                    "Cannot update flat file description because the selected " +
                    "row count is: " + this.annotationsMetaTable.getSelectedRowCount());
        }
    }

    private static final class FlatFileDescriptionCell
    {
        private final FlatFileDescription flatFileDescription;
        private final int columnIndex;
        
        /**
         * Constructor
         * @param flatFileDescription
         *          the description
         * @param columnIndex
         *          the column index in the table
         */
        public FlatFileDescriptionCell(
                FlatFileDescription flatFileDescription,
                int columnIndex)
        {
            if(columnIndex < 0 || columnIndex > 2)
            {
                throw new IndexOutOfBoundsException(
                        "Column index should be in the range [0, 2], not: " +
                        columnIndex);
            }
            
            this.flatFileDescription = flatFileDescription;
            this.columnIndex = columnIndex;
        }
        
        /**
         * Getter for the flat file description
         * @return the description that this class encapsulates
         */
        public FlatFileDescription getFlatFileDescription()
        {
            return this.flatFileDescription;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            // convert to a string based on the following column headers:
            // "File Name", "Table Name", "Format"
            switch(this.columnIndex)
            {
                case 0:
                {
                    File file = this.flatFileDescription.getFlatFile();
                    if(file == null)
                    {
                        return "";
                    }
                    else
                    {
                        return file.getName();
                    }
                }
                
                case 1:
                {
                    String tableName = this.flatFileDescription.getTableName();
                    if(tableName == null)
                    {
                        return "";
                    }
                    else
                    {
                        return tableName;
                    }
                }
                
                case 2:
                {
                    FlatFileFormat format = this.flatFileDescription.getFormat();
                    if(format == null)
                    {
                        return "";
                    }
                    else
                    {
                        return format.toString();
                    }
                }
                
                default:
                {
                    throw new IllegalStateException(
                            "Internal error: unexpected column index value: " +
                            this.columnIndex);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateData()
    {
        // the data is always valid in this panel
        return true;
    }
    
    /**
     * Get a list of all the flat file descriptions for annotation data
     * @return
     *          the list
     */
    public List<FlatFileDescription> getFlatFileDescriptions()
    {
        int descCount = this.annotationsMetaTableModel.getRowCount();
        List<FlatFileDescription> descriptions =
            new ArrayList<FlatFileDescription>(descCount);
        for(int i = 0; i < descCount; i++)
        {
            FlatFileDescriptionCell selectedCell =
                (FlatFileDescriptionCell)this.annotationsMetaTable.getValueAt(i, 0);
            descriptions.add(selectedCell.getFlatFileDescription());
        }
        return descriptions;
    }
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addTableButton = new javax.swing.JButton();
        editSelectedButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        annotationsMetaTable = new javax.swing.JTable();

        addTableButton.setText("Add Table...");

        editSelectedButton.setText("Edit Selected...");

        removeSelectedButton.setText("Remove Selected");

        scrollPane.setViewportView(annotationsMetaTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(addTableButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(editSelectedButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeSelectedButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addTableButton)
                    .add(editSelectedButton)
                    .add(removeSelectedButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTableButton;
    private javax.swing.JTable annotationsMetaTable;
    private javax.swing.JButton editSelectedButton;
    private javax.swing.JButton removeSelectedButton;
    // End of variables declaration//GEN-END:variables

}

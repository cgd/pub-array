/*
 * Copyright (c) 2010 The Jackson Laboratory
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
package org.jax.pubarray.builder;

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

import org.jax.pubarray.db.PerGeneImageDirectoryDescription;
import org.jax.util.gui.SwingUtilities;
import org.jax.util.gui.ValidatablePanel;
import org.jax.util.gui.WizardController;
import org.jax.util.gui.WizardDialog;
import org.jax.util.gui.WizardDialog.WizardDialogType;

/**
 * Allows the user to load dirs which contain per-probe images for your
 * pub-array application
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PerGeneImageDirectoriesPanel extends ValidatablePanel
{
    /**
     * Every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4236728119695944781L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PerGeneImageDirectoriesPanel.class.getName());
    
    private final SharedDirectoryContainer startingDirectory;
    
    private DefaultTableModel imageDirDescriptionTableModel;

    /**
     * Constructor
     * @param startingDirectory the starting directory to use for browsing files
     */
    public PerGeneImageDirectoriesPanel(SharedDirectoryContainer startingDirectory)
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
        this.addButton.setIcon(getIcon("/images/action/add-16x16.png"));
        this.editSelectedButton.setIcon(getIcon("/images/action/edit-16x16.png"));
        this.removeSelectedButton.setIcon(getIcon("/images/action/remove-16x16.png"));
        
        this.addButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                PerGeneImageDirectoriesPanel.this.addDir();
            }
        });
        
        this.editSelectedButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                PerGeneImageDirectoriesPanel.this.editSelected();
            }
        });
        
        this.removeSelectedButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                PerGeneImageDirectoriesPanel.this.removeSelected();
            }
        });
        
        this.imageDirDescriptionTableModel = new DefaultTableModel(
                new String[] {"Directory", "Name"},
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
        this.imageDirDescriptionTable.setModel(this.imageDirDescriptionTableModel);
        this.imageDirDescriptionTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.imageDirDescriptionTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if(!e.getValueIsAdjusting())
                        {
                            PerGeneImageDirectoriesPanel.this.imageDirDescriptionTableSelectionChanged();
                        }
                    }
                });
        
        this.imageDirDescriptionTableSelectionChanged();
    }
    
    private void imageDirDescriptionTableSelectionChanged()
    {
        int selectionCount = this.imageDirDescriptionTable.getSelectedRowCount();
        
        this.editSelectedButton.setEnabled(selectionCount == 1);
        this.removeSelectedButton.setEnabled(selectionCount >= 1);
    }

    private void removeSelected()
    {
        int[] selectedRows = this.imageDirDescriptionTable.getSelectedRows();
        
        // this function depends on the order of the rows. this sort may not
        // be needed but the javadoc doesn't claim any ordering property so lets
        // just do it to be on the safe side
        Arrays.sort(selectedRows);
        for(int i = selectedRows.length - 1; i >= 0; i--)
        {
            this.imageDirDescriptionTableModel.removeRow(selectedRows[i]);
        }
    }

    private void editSelected()
    {
        if(this.imageDirDescriptionTable.getSelectedRowCount() == 1)
        {
            PerGeneImageDirectoryCell selectedCell =
                (PerGeneImageDirectoryCell)this.imageDirDescriptionTable.getValueAt(
                        this.imageDirDescriptionTable.getSelectedRow(),
                        0);
            this.showAddEditPerProbeDirDialog(
                    "Edit Per-Probe Image Directory",
                    new AddEditPerProbeImageDirController(
                            selectedCell.getDescription()));
        }
        else
        {
            throw new IllegalStateException(
                    "trying to edit a per-probe image dir with " +
                    this.imageDirDescriptionTable.getSelectedRowCount() +
                    " rows selected");
        }
    }

    private void addDir()
    {
        this.showAddEditPerProbeDirDialog(
                "Add Per-Probe Image Directory",
                new AddEditPerProbeImageDirController());
    }
    
    private void showAddEditPerProbeDirDialog(
            String title,
            AddEditPerProbeImageDirController controller)
    {
        final WizardDialog wizardDialog;
        Window parentWindow = SwingUtilities.getContainingWindow(this);
        if(parentWindow instanceof JDialog)
        {
            wizardDialog = new WizardDialog(
                    controller,
                    controller.getPanel(),
                    (JDialog)parentWindow,
                    title,
                    true,
                    WizardDialogType.OK_CANCEL_DIALOG);
        }
        else if(parentWindow instanceof JFrame)
        {
            wizardDialog = new WizardDialog(
                    controller,
                    controller.getPanel(),
                    (JFrame)parentWindow,
                    title,
                    true,
                    WizardDialogType.OK_CANCEL_DIALOG);
        }
        else
        {
            throw new IllegalStateException(
                    "this panel must be attached to a JPanel or JDialog in " +
                    "order to show the add/edit per-probe image directory dialog");
        }
        
        wizardDialog.setVisible(true);
    }

    private static ImageIcon getIcon(String classpath)
    {
        return new ImageIcon(PerGeneImageDirectoriesPanel.class.getResource(classpath));
    }

    private class AddEditPerProbeImageDirController implements WizardController
    {
        private final PerGeneImageDirectoryPanel panel;
        private final boolean isAddPanel;
        
        /**
         * Constructor for adding a new dir
         */
        public AddEditPerProbeImageDirController()
        {
            this.panel = new PerGeneImageDirectoryPanel(
                    PerGeneImageDirectoriesPanel.this.startingDirectory);
            this.isAddPanel = true;
        }
        
        /**
         * Constructor for editing an existing description
         * @param descriptionToEdit it's all in the name isn't it :-)
         */
        public AddEditPerProbeImageDirController(PerGeneImageDirectoryDescription descriptionToEdit)
        {
            this.panel = new PerGeneImageDirectoryPanel(
                    descriptionToEdit,
                    PerGeneImageDirectoriesPanel.this.startingDirectory);
            this.isAddPanel = false;
        }
        
        /**
         * Getter for the panel
         * @return the panel
         */
        public PerGeneImageDirectoryPanel getPanel()
        {
            return this.panel;
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
            if(this.panel.validateData())
            {
                PerGeneImageDirectoryDescription description =
                    this.panel.getPerGeneImageDirectoryDescription();
                
                // if this is an add then add it. if it's an edit then update
                // the current selection
                if(this.isAddPanel)
                {
                    PerGeneImageDirectoriesPanel.this.addDescription(description);
                }
                else
                {
                    PerGeneImageDirectoriesPanel.this.updateSelectedDescription(description);
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
     * Create a table row for the given description
     * @param description
     *          the description to create a row for
     * @return
     *          the row
     */
    private PerGeneImageDirectoryCell[] descriptionToTableRow(PerGeneImageDirectoryDescription description)
    {
        return new PerGeneImageDirectoryCell[] {
                new PerGeneImageDirectoryCell(description, 0),
                new PerGeneImageDirectoryCell(description, 1)};
    }
    
    private void addDescription(PerGeneImageDirectoryDescription description)
    {
        this.imageDirDescriptionTableModel.addRow(this.descriptionToTableRow(description));
    }

    private void updateSelectedDescription(PerGeneImageDirectoryDescription description)
    {
        if(this.imageDirDescriptionTable.getSelectedRowCount() == 1)
        {
            int selectedRowIndex = this.imageDirDescriptionTable.getSelectedRow();
            
            PerGeneImageDirectoryCell[] updatedRow = this.descriptionToTableRow(description);
            for(int colIndex = 0; colIndex < updatedRow.length; colIndex++)
            {
                this.imageDirDescriptionTableModel.setValueAt(
                        updatedRow[colIndex],
                        selectedRowIndex,
                        colIndex);
            }
        }
        else
        {
            LOG.severe(
                    "Cannot update because the selected " +
                    "row count is: " + this.imageDirDescriptionTable.getSelectedRowCount());
        }
    }

    private static final class PerGeneImageDirectoryCell
    {
        private final PerGeneImageDirectoryDescription description;
        private final int columnIndex;
        
        /**
         * Constructor
         * @param description
         *          the description
         * @param columnIndex
         *          the column index in the table
         */
        public PerGeneImageDirectoryCell(
                PerGeneImageDirectoryDescription description,
                int columnIndex)
        {
            if(columnIndex < 0 || columnIndex > 1)
            {
                throw new IndexOutOfBoundsException(
                        "Column index should be in the range [0, 1], not: " +
                        columnIndex);
            }
            
            this.description = description;
            this.columnIndex = columnIndex;
        }
        
        /**
         * Getter for the description
         * @return the description that this class encapsulates
         */
        public PerGeneImageDirectoryDescription getDescription()
        {
            return this.description;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            // convert to a string based on the following column headers:
            // "Directory", "Name", "Description"
            switch(this.columnIndex)
            {
                case 0:
                {
                    File dir = this.description.getDirectory();
                    if(dir == null)
                    {
                        return "";
                    }
                    else
                    {
                        return dir.getName();
                    }
                }
                
                case 1:
                {
                    String name = this.description.getName();
                    if(name == null)
                    {
                        return "";
                    }
                    else
                    {
                        return name;
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
     * Get a list of all the descriptions
     * @return
     *          the list
     */
    public List<PerGeneImageDirectoryDescription> getDescriptions()
    {
        int descCount = this.imageDirDescriptionTableModel.getRowCount();
        List<PerGeneImageDirectoryDescription> descriptions =
            new ArrayList<PerGeneImageDirectoryDescription>(descCount);
        for(int i = 0; i < descCount; i++)
        {
            PerGeneImageDirectoryCell selectedCell =
                (PerGeneImageDirectoryCell)this.imageDirDescriptionTable.getValueAt(i, 0);
            descriptions.add(selectedCell.getDescription());
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

        addButton = new javax.swing.JButton();
        editSelectedButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        imageDirDescriptionTable = new javax.swing.JTable();

        addButton.setText("Add Per-Gene Image Directory...");

        editSelectedButton.setText("Edit Selected...");

        removeSelectedButton.setText("Remove Selected");

        scrollPane.setViewportView(imageDirDescriptionTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
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
                    .add(addButton)
                    .add(editSelectedButton)
                    .add(removeSelectedButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editSelectedButton;
    private javax.swing.JTable imageDirDescriptionTable;
    private javax.swing.JButton removeSelectedButton;
    // End of variables declaration//GEN-END:variables

}

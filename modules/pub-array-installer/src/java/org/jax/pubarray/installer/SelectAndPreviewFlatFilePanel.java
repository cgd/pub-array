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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;

import org.jax.util.gui.FlatFileTable;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.gui.ValidatablePanel;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileFormat;
import org.jax.util.io.FlatFileReader;
import org.jax.util.io.IllegalFormatException;

/**
 * The panel for microarray data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SelectAndPreviewFlatFilePanel extends ValidatablePanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 918341149952842045L;
    
    private static final Logger LOG = Logger.getLogger(
            SelectAndPreviewFlatFilePanel.class.getName());
    
    private static final int MAX_PREVIEW_ROW_COUNT = 100;
    
    private final FlatFileTable flatFileTable;

    private final SharedDirectoryContainer startingDirectory;
    
    private final boolean isDesignFile;
    
    private boolean synchTableNameToFileName;

    /**
     * Default constructor
     * @param startingDirectory
     *          the starting dir to use when browsing for files
     * @param isDesignFile
     *          if this panel is for loading a design file (we only show the
     *          "match using ..." controls for the design file)
     */
    public SelectAndPreviewFlatFilePanel(
            SharedDirectoryContainer startingDirectory,
            boolean isDesignFile)
    {
        this(null, startingDirectory, isDesignFile);
    }
    
    /**
     * Constructor. Turns the parameters into a {@link FlatFileDescription}
     * and passes it to
     * {@link #SelectAndPreviewFlatFilePanel(FlatFileDescription, SharedDirectoryContainer, boolean)}
     * @param flatFile
     *          see {@link FlatFileDescription#getFlatFile()}
     * @param tableName
     *          see {@link FlatFileDescription#getTableName()}
     * @param format
     *          see {@link FlatFileDescription#getFormat()}
     * @param startingDirectory
     *          the starting dir to use when browsing for files
     * @param isDesignFile
     *          if this panel is for loading a design file (we only show the
     *          "match using ..." controls for the design file)
     */
    public SelectAndPreviewFlatFilePanel(
            File flatFile,
            String tableName,
            FlatFileFormat format,
            SharedDirectoryContainer startingDirectory,
            boolean isDesignFile)
    {
        this(new FlatFileDescription(flatFile, tableName, format), startingDirectory, isDesignFile);
    }
    
    /**
     * Constructor
     * @param flatFileDescription
     *          the flat file description to start out with (null is allowed)
     * @param startingDirectory
     *          the starting dir to use when browsing for files
     * @param isDesignFile
     *          if this panel is for loading a design file (we only show the
     *          "match using ..." controls for the design file)
     */
    public SelectAndPreviewFlatFilePanel(
            FlatFileDescription flatFileDescription,
            SharedDirectoryContainer startingDirectory,
            boolean isDesignFile)
    {
        this.isDesignFile = isDesignFile;
        this.flatFileTable = new FlatFileTable(MAX_PREVIEW_ROW_COUNT);
        this.startingDirectory = startingDirectory;
        this.initComponents();
        this.postGuiInit(flatFileDescription);
    }

    private void postGuiInit(FlatFileDescription flatFileDescription)
    {
        String tableName = flatFileDescription == null ?
                null :
                flatFileDescription.getTableName();
        this.synchTableNameToFileName = tableName == null;
        if(!this.synchTableNameToFileName)
        {
            this.tableNameTextField.setText(tableName);
        }
        
        this.browseFilesButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SelectAndPreviewFlatFilePanel.this.browseFiles();
            }
        });
        this.browseFilesButton.setIcon(new ImageIcon(
                SelectAndPreviewFlatFilePanel.class.getResource(
                        "/images/action/browse-16x16.png")));
        
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.TAB_DELIMITED_UNIX);
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.CSV_UNIX);
        this.fileFormatComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    try
                    {
                        SelectAndPreviewFlatFilePanel.this.refreshTable();
                        SelectAndPreviewFlatFilePanel.this.refreshMatchUsingItems();
                    }
                    catch(Exception ex)
                    {
                        String title = "Error Loading Flat File";
                        LOG.log(Level.SEVERE,
                                title,
                                ex);
                        MessageDialogUtilities.error(
                                SelectAndPreviewFlatFilePanel.this,
                                ex.getMessage(),
                                title);
                    }
                }
            }
        });
        
        this.fileTextField.getDocument().addDocumentListener(
                new SimplifiedDocumentListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void anyUpdate(DocumentEvent e)
                    {
                        SelectAndPreviewFlatFilePanel.this.selectedFileChanged();
                    }
                });
        
        this.tableNameTextField.getDocument().addDocumentListener(
                new SimplifiedDocumentListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void anyUpdate(DocumentEvent e)
                    {
                        SelectAndPreviewFlatFilePanel.this.tableNameChanged();
                    }
                });
        
        this.dataPreviewScrollPane.setViewportView(this.flatFileTable);
        
        if(flatFileDescription != null)
        {
            if(flatFileDescription.getFormat() != null)
            {
                this.fileFormatComboBox.setSelectedItem(
                        flatFileDescription.getFormat());
            }
            
            if(flatFileDescription.getFlatFile() != null)
            {
                this.fileTextField.setText(flatFileDescription.getFlatFile().getPath());
            }
        }
        
        if(!this.isDesignFile)
        {
            this.matchUsingLabel.setVisible(false);
            this.matchUsingComboBox.setVisible(false);
        }
        else
        {
            try
            {
                this.refreshMatchUsingItems();
            }
            catch(Exception ex)
            {
                // we really should never get here because the file field is
                // blank and no IO or parsing should be attempted at this point
                LOG.log(Level.SEVERE,
                        "Unexpected exception",
                        ex);
                MessageDialogUtilities.error(
                        this,
                        ex.getMessage(),
                        "Unexpected Error");
            }
        }
    }

    private void tableNameChanged()
    {
        if(this.synchTableNameToFileName)
        {
            // We should only continue to sync file names if the user hasn't
            // explicitly entered a different name
            String tableNameText = this.tableNameTextField.getText();
            String currPrettyName = this.getPrettyNameForCurrFile();
            
            // this is my imperfect attempt to distinguish between events
            // that are triggered programmatically vs events that are triggered
            // by the user. The reason we care is that we need to stop synch'ing
            // the table and file names when the user decides to type in an
            // explicit table name
            this.synchTableNameToFileName =
                tableNameText.length() == 0 ||
                tableNameText.equals(currPrettyName);
        }
    }

    private void selectedFileChanged()
    {
        if(this.synchTableNameToFileName)
        {
            this.synchTableName();
        }
        
        try
        {
            this.refreshTable();
            this.refreshMatchUsingItems();
        }
        catch(Exception ex)
        {
            String title = "Error Loading Flat File";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.error(
                    this,
                    ex.getMessage(),
                    title);
        }
    }

    private void synchTableName()
    {
        this.tableNameTextField.setText(this.getPrettyNameForCurrFile());
    }
    
    private String getPrettyNameForCurrFile()
    {
        String fileName = this.fileTextField.getText().trim();
        if(fileName.length() >= 1)
        {
            return this.toPrettyName(new File(fileName));
        }
        else
        {
            return "";
        }
    }

    private FlatFileReader getFlatFileReader()
    {
        String fileString = this.fileTextField.getText();
        if(fileString != null && fileString.trim().length() >= 1)
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(
                        fileString));
                return new FlatFileReader(br, this.getSelectedFormat());
            }
            catch(IOException ex)
            {
                // don't care. this doesn't necessarily mean that there is a
                // problem. the user could be in the middle of typing out
                // a file name
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    private void browseFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(this.startingDirectory.getDirectory());
        
        int chooserResult = fileChooser.showOpenDialog(this);
        if(chooserResult == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            this.startingDirectory.setDirectory(file.getParentFile());
            this.fileTextField.setText(file.getPath());
        }
    }
    
    private void refreshTable() throws IOException, IllegalFormatException
    {
        FlatFileReader ffr = this.getFlatFileReader();
        if(ffr == null)
        {
            this.flatFileTable.clearTable();
        }
        else
        {
            if(this.dataPreviewScrollPane.getComponentCount() == 0)
            {
                this.dataPreviewScrollPane.add(this.flatFileTable);
            }
            
            try
            {
                this.flatFileTable.loadTable(
                        ffr,
                        true,
                        this.dataPreviewScrollPane.getWidth());
            }
            finally
            {
                ffr.close();
            }
        }
    }
    
    private void refreshMatchUsingItems() throws IOException, IllegalFormatException
    {
        FlatFileReader ffr = this.getFlatFileReader();
        this.matchUsingLabel.setEnabled(ffr != null);
        this.matchUsingComboBox.setEnabled(ffr != null);
        this.matchUsingComboBox.removeAllItems();
        if(ffr == null)
        {
            this.matchUsingComboBox.addItem("No Valid File Selected");
        }
        else
        {
            try
            {
                // Start by adding an item for matching purely on the row
                // ordering
                this.matchUsingComboBox.addItem("Row Ordering");
                
                String[] headerRow = ffr.readRow();
                for(String headerItem: headerRow)
                {
                    this.matchUsingComboBox.addItem(headerItem);
                }
            }
            finally
            {
                ffr.close();
            }
        }
    }

    /**
     * Create a string which is a "pretty" table name for the given file name
     * @param file  the file that we want a pretty name for
     * @return      the pretty name
     */
    private String toPrettyName(File file)
    {
        String prettyName = file.getName();

        // remove extension
        int lastDotIndex = prettyName.lastIndexOf('.');
        if(lastDotIndex >= 1)
        {
            prettyName = prettyName.substring(0, lastDotIndex);
        }

        return prettyName.replace('_', ' ');
    }
    
    /**
     * A convenience function for getting the selected flat file format from
     * the selection combo box
     * @return
     *          the format
     */
    public FlatFileFormat getSelectedFormat()
    {
        return (FlatFileFormat)this.fileFormatComboBox.getSelectedItem();
    }
    
    /**
     * Getter for the selected file
     * @return  the selected file
     */
    public File getSelectedFile()
    {
        return new File(this.fileTextField.getText().trim());
    }
    
    /**
     * Getter for the table name
     * @return  the table name
     */
    public String getTableName()
    {
        return this.tableNameTextField.getText().trim();
    }
    
    /**
     * Getter for the flat file description
     * @return
     *          the flat file description
     */
    public FlatFileDescription getFlatFileDescription()
    {
        return new FlatFileDescription(
                this.getSelectedFile(),
                this.getTableName(),
                this.getSelectedFormat());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateData()
    {
        String errorMessage = null;
        if(this.fileTextField.getText().trim().length() == 0)
        {
            errorMessage = "Please select a file before continuing.";
        }
        else if(!this.getSelectedFile().exists())
        {
            errorMessage =
                "The selected file \"" + this.getSelectedFile().getPath() +
                "\" does not exist!";
        }
        else if(this.getTableName().length() == 0)
        {
            errorMessage = "Please enter a table name before proceeding.";
        }
        
        if(errorMessage != null)
        {
            MessageDialogUtilities.warn(
                    this,
                    errorMessage,
                    "Validation Failed");
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Getter for the design column that should be matched (returns null if
     * either no selection is made or the "order by rows" selection is made)
     * @return  the column name or null
     */
    public String getDesignColumnToMatch()
    {
        // if 0 is selected it means that the user wants to use row ordering
        // instead of matching on a design column so we should return null
        if(this.matchUsingComboBox.getSelectedIndex() >= 1)
        {
            return (String)this.matchUsingComboBox.getSelectedItem();
        }
        else
        {
            return null;
        }
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

        javax.swing.JLabel fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        browseFilesButton = new javax.swing.JButton();
        javax.swing.JLabel tableNameLabel = new javax.swing.JLabel();
        tableNameTextField = new javax.swing.JTextField();
        javax.swing.JLabel fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel dataPreviewLabel = new javax.swing.JLabel();
        dataPreviewScrollPane = new javax.swing.JScrollPane();
        matchUsingLabel = new javax.swing.JLabel();
        matchUsingComboBox = new javax.swing.JComboBox();

        fileLabel.setText("File Location:");

        browseFilesButton.setText("Browse...");

        tableNameLabel.setText("Table Name:");

        fileFormatLabel.setText("Format:");

        dataPreviewLabel.setText("File Preview (First 100 Lines Only):");

        matchUsingLabel.setText("Match Design Rows to Data Columns Using:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dataPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                    .add(dataPreviewLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fileLabel)
                            .add(fileFormatLabel)
                            .add(tableNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseFilesButton))
                            .add(tableNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(matchUsingLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(matchUsingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileLabel)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseFilesButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tableNameLabel)
                    .add(tableNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileFormatLabel)
                    .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataPreviewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(matchUsingLabel)
                    .add(matchUsingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseFilesButton;
    private javax.swing.JScrollPane dataPreviewScrollPane;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JComboBox matchUsingComboBox;
    private javax.swing.JLabel matchUsingLabel;
    private javax.swing.JTextField tableNameTextField;
    // End of variables declaration//GEN-END:variables

}

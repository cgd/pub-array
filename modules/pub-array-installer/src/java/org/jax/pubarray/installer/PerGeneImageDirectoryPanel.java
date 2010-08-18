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
package org.jax.pubarray.installer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;

import org.jax.pubarray.db.PerGeneImageDirectoryDescription;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.gui.ValidatablePanel;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PerGeneImageDirectoryPanel extends ValidatablePanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -7425104283335169210L;

    private final SharedDirectoryContainer sharedBrowseDirectory;
    
    private boolean synchNameToDirName;

    /**
     * Default constructor
     * @param sharedBrowseDirectory
     *          the starting dir to use when browsing for files
     */
    public PerGeneImageDirectoryPanel(SharedDirectoryContainer sharedBrowseDirectory)
    {
        this(null, sharedBrowseDirectory);
    }
    
    /**
     * Constructor
     * @param perGeneImageDesc
     *          the image dir description
     * @param sharedBrowseDirectory
     *          the starting dir to use when browsing for files
     */
    public PerGeneImageDirectoryPanel(
            PerGeneImageDirectoryDescription perGeneImageDesc,
            SharedDirectoryContainer sharedBrowseDirectory)
    {
        this.sharedBrowseDirectory = sharedBrowseDirectory;
        this.initComponents();
        this.postGuiInit(perGeneImageDesc);
    }

    private void postGuiInit(PerGeneImageDirectoryDescription perGeneImageDesc)
    {
        String name = perGeneImageDesc == null ?
                null :
                perGeneImageDesc.getName();
        this.synchNameToDirName = name == null;
        if(!this.synchNameToDirName)
        {
            this.nameTextField.setText(name);
        }
        
        this.browseDirsButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                PerGeneImageDirectoryPanel.this.browseFiles();
            }
        });
        this.browseDirsButton.setIcon(new ImageIcon(
                PerGeneImageDirectoryPanel.class.getResource(
                        "/images/action/browse-16x16.png")));
        
        this.dirTextField.getDocument().addDocumentListener(
                new SimplifiedDocumentListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void anyUpdate(DocumentEvent e)
                    {
                        PerGeneImageDirectoryPanel.this.selectedFileChanged();
                    }
                });
        
        this.nameTextField.getDocument().addDocumentListener(
                new SimplifiedDocumentListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void anyUpdate(DocumentEvent e)
                    {
                        PerGeneImageDirectoryPanel.this.nameChanged();
                    }
                });
        
        if(perGeneImageDesc != null)
        {
            this.dirTextField.setText(perGeneImageDesc.getDirectory().getPath());
        }
    }

    private void nameChanged()
    {
        if(this.synchNameToDirName)
        {
            // We should only continue to sync file names if the user hasn't
            // explicitly entered a different name
            String nameText = this.nameTextField.getText();
            String currPrettyName = this.getPrettyNameForCurrDir();
            
            // this is my imperfect attempt to distinguish between events
            // that are triggered programmatically vs events that are triggered
            // by the user. The reason we care is that we need to stop synch'ing
            // the table and file names when the user decides to type in an
            // explicit table name
            this.synchNameToDirName =
                nameText.length() == 0 ||
                nameText.equals(currPrettyName);
        }
    }

    private void selectedFileChanged()
    {
        if(this.synchNameToDirName)
        {
            this.synchTableName();
        }
    }

    private void synchTableName()
    {
        this.nameTextField.setText(this.getPrettyNameForCurrDir());
    }
    
    private String getPrettyNameForCurrDir()
    {
        String fileName = this.dirTextField.getText().trim();
        if(fileName.length() >= 1)
        {
            return this.toPrettyName(new File(fileName));
        }
        else
        {
            return "";
        }
    }

    private void browseFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Gene Image Directory");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(this.sharedBrowseDirectory.getDirectory());
        
        int chooserResult = fileChooser.showOpenDialog(this);
        if(chooserResult == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            this.sharedBrowseDirectory.setDirectory(file.getParentFile());
            this.dirTextField.setText(file.getPath());
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
     * Getter for the selected image directory
     * @return  the selected directory
     */
    public File getSelectedDirectory()
    {
        return new File(this.dirTextField.getText().trim());
    }
    
    /**
     * Getter for the table name
     * @return  the table name
     */
    public String getGraphName()
    {
        return this.nameTextField.getText().trim();
    }
    
    /**
     * Getter for the per-gene image directory description
     * @return  the per-gene image directory description
     */
    public PerGeneImageDirectoryDescription getPerGeneImageDirectoryDescription()
    {
        return new PerGeneImageDirectoryDescription(
                this.getSelectedDirectory(),
                this.getGraphName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateData()
    {
        String errorMessage = null;
        if(this.dirTextField.getText().trim().length() == 0)
        {
            errorMessage = "Please select an image directory before continuing.";
        }
        else if(!this.getSelectedDirectory().isDirectory())
        {
            errorMessage =
                "The selected file \"" + this.getSelectedDirectory().getPath() +
                "\" is not a directory!";
        }
        else if(this.getGraphName().length() == 0)
        {
            errorMessage = "Please enter a name before proceeding.";
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
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel dirLabel = new javax.swing.JLabel();
        dirTextField = new javax.swing.JTextField();
        browseDirsButton = new javax.swing.JButton();
        javax.swing.JLabel nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        dirLabel.setText("Image Directory:");

        browseDirsButton.setText("Browse...");

        nameLabel.setText("Name/Short Description:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dirLabel)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(dirTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseDirsButton))
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dirLabel)
                    .add(dirTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseDirsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseDirsButton;
    private javax.swing.JTextField dirTextField;
    private javax.swing.JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}

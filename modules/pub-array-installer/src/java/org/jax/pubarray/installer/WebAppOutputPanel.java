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
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.ValidatablePanel;
import org.jax.util.io.FileChooserExtensionFilter;

/**
 * The panel that allows the user to decide which file the web application
 * will be written to
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class WebAppOutputPanel extends ValidatablePanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2756635631928584436L;
    
    private final SharedDirectoryContainer startingDirectory;
    
    private final MultiTaskProgressPanel progressPanel;
    
    /**
     * Constructor
     * @param startingDirectory
     *          the starting directory to use when browsing for files
     */
    public WebAppOutputPanel(SharedDirectoryContainer startingDirectory)
    {
        this.startingDirectory = startingDirectory;
        this.progressPanel = new MultiTaskProgressPanel();
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Getter for the progress panel
     * @return the progress panel
     */
    public MultiTaskProgressPanel getProgressPanel()
    {
        return this.progressPanel;
    }
    
    /**
     * Take care of the initialization that the GUI builder doesn't handle for
     * us
     */
    private void postGuiInit()
    {
        this.browseWebAppFilesButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                WebAppOutputPanel.this.browseWebAppFiles();
            }
        });
        this.browseWebAppFilesButton.setIcon(new ImageIcon(
                WebAppOutputPanel.class.getResource(
                        "/images/action/browse-16x16.png")));
    }

    private void browseWebAppFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select WAR File");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(this.startingDirectory.getDirectory());
        fileChooser.setFileFilter(new FileChooserExtensionFilter(
                "war",
                "WAR File"));
        
        int chooserResult = fileChooser.showOpenDialog(this);
        if(chooserResult == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            this.startingDirectory.setDirectory(file.getParentFile());
            this.webAppFileTextField.setText(file.getPath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateData()
    {
        String errorMessage = null;
        
        String fileName = this.webAppFileTextField.getText().trim();
        if(fileName.length() == 0)
        {
            errorMessage = "Please enter a file name before proceeding.";
        }
        else if(!fileName.toLowerCase().endsWith(".war"))
        {
            errorMessage = "The output file must end with a \".war\" extension.";
        }
        else
        {
            File file = this.getWebApplicationFile();
            if(file.exists())
            {
                String title = "Replace Existing File?";
                String question =
                    "The selected file \"" + fileName +
                    "\" already exists. Do you want to replace this file?";
                
                return MessageDialogUtilities.ask(this, question, title);
            }
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
     * Gets the WAR file that the user chose
     * @return  the WAR file
     */
    public File getWebApplicationFile()
    {
        String fileName = this.webAppFileTextField.getText().trim();
        return new File(fileName);
    }
    
    /**
     * Use the experiment name to create a WAR file name
     * @param experimentName the experiment name
     */
    public void updateFileWithExperimentName(String experimentName)
    {
        File outFile = new File(
                this.getDefaultDir(),
                this.experimentNameToWarFileName(experimentName));
        this.webAppFileTextField.setText(outFile.getPath());
    }
    
    private File getDefaultDir()
    {
        return new File(TypeSafeSystemProperties.getWorkingDirectory());
    }
    
    private String experimentNameToWarFileName(String experimentName)
    {
        int nameLen = experimentName.length();
        StringBuilder warFileName = new StringBuilder();
        for(int i = 0; i < nameLen; i++)
        {
            char currChar = experimentName.charAt(i);
            if(Character.isLetterOrDigit(currChar))
            {
                warFileName.append(Character.toLowerCase(currChar));
            }
            else if(Character.isWhitespace(currChar))
            {
                warFileName.append('-');
            }
            
            // else skip this character. it isn't something that we want to
            // try to put into a file name
        }
        
        // fall back to calling the file pub-array.war
        if(warFileName.length() == 0)
        {
            warFileName.append("pub-array.war");
        }
        else
        {
            warFileName.append(".war");
        }
        
        return warFileName.toString();
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
        java.awt.GridBagConstraints gridBagConstraints;

        javax.swing.JLabel webAppFileLabel = new javax.swing.JLabel();
        browseWebAppFilesButton = new javax.swing.JButton();
        webAppFileTextField = new javax.swing.JTextField();
        javax.swing.JPanel lowerPanel = new javax.swing.JPanel();
        javax.swing.JPanel progressPanelHandle = this.progressPanel;

        webAppFileLabel.setText("Save As:");

        browseWebAppFilesButton.setText("Browse...");

        lowerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        lowerPanel.add(progressPanelHandle, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lowerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(webAppFileLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(webAppFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseWebAppFilesButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(webAppFileLabel)
                    .add(browseWebAppFilesButton)
                    .add(webAppFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lowerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseWebAppFilesButton;
    private javax.swing.JTextField webAppFileTextField;
    // End of variables declaration//GEN-END:variables

}

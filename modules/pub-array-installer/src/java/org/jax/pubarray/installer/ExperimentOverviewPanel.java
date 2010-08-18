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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.ValidatablePanel;


/**
 * A panel that lets the user name and describe the experiment that they are
 * creating
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExperimentOverviewPanel extends ValidatablePanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5583698476985448727L;
    
    /**
     * The formats that can be used for description text
     */
    public enum DescriptionFormat
    {
        /**
         * Plain text format
         */
        PLAIN_TEXT
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Plain Text";
            }
        },
        
        /**
         * For HTML fragments
         */
        HTML_FRAGMENT
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "HTML Fragment";
            }
        }
    }
    
    private static final Logger LOG = Logger.getLogger(
            ExperimentOverviewPanel.class.getName());
    
    private final SharedDirectoryContainer startingDirectory;
    
    /**
     * Constructor
     * @param startingDirectory
     *          the starting point to use when browsing for file
     */
    public ExperimentOverviewPanel(SharedDirectoryContainer startingDirectory)
    {
        this.startingDirectory = startingDirectory;
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * Take care of the initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.loadDescriptionFromFileButton.setIcon(new ImageIcon(
                SelectAndPreviewFlatFilePanel.class.getResource(
                        "/images/action/browse-16x16.png")));
        this.loadDescriptionFromFileButton.addActionListener(
                new ActionListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void actionPerformed(ActionEvent e)
                    {
                        ExperimentOverviewPanel.this.browseDescriptionFiles();
                    }
                });
        this.descriptionFormatComboBox.addItem(DescriptionFormat.PLAIN_TEXT);
        this.descriptionFormatComboBox.addItem(DescriptionFormat.HTML_FRAGMENT);
    }

    private void browseDescriptionFiles()
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
            this.loadDescriptionFromFile(file);
        }
    }

    private void loadDescriptionFromFile(File file)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int currChar;
            StringBuilder sb = new StringBuilder();
            while((currChar = reader.read()) != -1)
            {
                sb.append((char)currChar);
            }
            
            this.descriptionTextArea.setText(sb.toString());
        }
        catch(IOException ex)
        {
            String title = "Failed to Read Description File";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.error(
                    this,
                    ex.getMessage(),
                    title);
        }
    }
    
    /**
     * Getter for the name that the user chose for this experiment
     * @return
     *          the name
     */
    public String getExperimentName()
    {
        return this.experimentNameTextField.getText().trim();
    }
    
    /**
     * Getter for the description that the description wrote for the
     * experiment
     * @return the description
     */
    public String getDescription()
    {
        String rawDescText = this.descriptionTextArea.getText();
        switch(this.getDescriptionFormat())
        {
            case HTML_FRAGMENT:
            {
                return rawDescText;
            }
            
            case PLAIN_TEXT:
            {
                // since it's plain text we need to escape special chars
                String safeDescText = rawDescText.replace("&", "&amp;");
                safeDescText = safeDescText.replace("\"", "&quot;");
                safeDescText = safeDescText.replace("'", "&apos;");
                safeDescText = safeDescText.replace("<", "&lt;");
                safeDescText = safeDescText.replace(">", "&gt;");
                
                // also let's insert a break when we see two consecutive
                // newlines
                safeDescText = safeDescText.replace("\n\n", "\n\n<br/>");
                
                return safeDescText;
            }
            
            default: throw new IllegalStateException(
                    "Internal Error: Unrecognized format: " +
                    this.getDescriptionFormat());
        }
    }
    
    /**
     * Getter for the description format that the user chose for the
     * experiment
     * @return the format
     */
    private DescriptionFormat getDescriptionFormat()
    {
        return (DescriptionFormat)this.descriptionFormatComboBox.getSelectedItem();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateData()
    {
        String experimentName = this.getExperimentName();
        if(experimentName.length() == 0)
        {
            MessageDialogUtilities.warn(
                    this,
                    "Please enter an experiment name before continuing.",
                    "No Experiment Name");
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

        javax.swing.JLabel experimentNameLabel = new javax.swing.JLabel();
        experimentNameTextField = new javax.swing.JTextField();
        javax.swing.JLabel experimentDescriptionLabel = new javax.swing.JLabel();
        loadDescriptionFromFileButton = new javax.swing.JButton();
        javax.swing.JScrollPane descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        javax.swing.JLabel descriptionFormatLabel = new javax.swing.JLabel();
        descriptionFormatComboBox = new javax.swing.JComboBox();

        experimentNameLabel.setText("Experiment Name:");

        experimentDescriptionLabel.setText("Description:");

        loadDescriptionFromFileButton.setText("Load From File...");

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        descriptionFormatLabel.setText("Description Format:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(experimentDescriptionLabel)
                            .add(experimentNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(experimentNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                            .add(loadDescriptionFromFileButton)))
                    .add(layout.createSequentialGroup()
                        .add(descriptionFormatLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(descriptionFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(experimentNameLabel)
                    .add(experimentNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(experimentDescriptionLabel)
                    .add(loadDescriptionFromFileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descriptionScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(descriptionFormatLabel)
                    .add(descriptionFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox descriptionFormatComboBox;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JTextField experimentNameTextField;
    private javax.swing.JButton loadDescriptionFromFileButton;
    // End of variables declaration//GEN-END:variables

}

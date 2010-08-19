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

package org.jax.pubarray.builder;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.JPanel;

import org.jax.pubarray.db.CandidateDatabaseManager;
import org.jax.pubarray.db.ConnectionManager;
import org.jax.pubarray.db.ExperimentMetadata;
import org.jax.pubarray.db.HSQLDBConnectionManager;
import org.jax.pubarray.db.PerGeneImageDirectoryDescription;
import org.jax.pubarray.db.PersistenceManager;
import org.jax.util.concurrent.SafeAWTInvoker;
import org.jax.util.concurrent.SimpleLongRunningTask;
import org.jax.util.gui.BroadcastingWizardController;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.ValidatablePanel;
import org.jax.util.gui.WizardEventSupport;
import org.jax.util.gui.WizardListener;
import org.jax.util.io.FileUtilities;
import org.jax.util.io.FlatFileReader;
import org.jax.util.io.IllegalFormatException;

/**
 * The pub array wizard controller. Also contains panels for all the different
 * wizard steps.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PubArrayWizardController implements BroadcastingWizardController
{
    private static final Logger LOG = Logger.getLogger(
            PubArrayWizardController.class.getName());
    
    private volatile boolean isFinishing = false;
    
    // this is the name that we should use for the database that we construct
    private static final String DATABASE_NAME = "pub-array-db";
    
    // this is the WAR resource that contains everything but the database.
    // When this resource is combined with the database that we create then
    // we have a real web application
    private static final String WAR_TEMPLATE_RESOURCE = "/pub-array-gwt-server-1.0.war";
    
    private final WizardEventSupport wizardEventSupport;
    
    private final ValidatablePanel[] wizardPanels;
    
    private final ExperimentOverviewPanel experimentOverviewPanel;
    private final SelectAndPreviewFlatFilePanel microarrayDesignPanel;
    private final SelectAndPreviewFlatFilePanel microarrayDataPanel;
    private final SelectAnnotationsPanel annotationsPanel;
    private final PerGeneImageDirectoriesPanel perGeneImageDirectoriesPanel;
    private final WebAppOutputPanel webAppOutputPanel;
    private final SimpleLongRunningTask buildWARTask;
    
    private Component parentComponent;
    
    private int currPanelIndex = 0;
    
    private boolean webAppFileNameInitialized = false;
    
    /**
     * Constructor
     */
    public PubArrayWizardController()
    {
        this.wizardEventSupport = new WizardEventSupport(this);
        
        final SharedDirectoryContainer startingDirectory =
            new SharedDirectoryContainer();
        
        this.experimentOverviewPanel = new ExperimentOverviewPanel(
                startingDirectory);
        this.microarrayDesignPanel = new SelectAndPreviewFlatFilePanel(
                null,
                "Experiment Design Data",
                null,
                startingDirectory,
                true);
        this.microarrayDataPanel = new SelectAndPreviewFlatFilePanel(
                null,
                "Microarray Intensity Data",
                null,
                startingDirectory,
                false);
        this.annotationsPanel = new SelectAnnotationsPanel(
                startingDirectory);
        this.perGeneImageDirectoriesPanel = new PerGeneImageDirectoriesPanel(
                startingDirectory);
        this.webAppOutputPanel = new WebAppOutputPanel(
                startingDirectory);
        
        // for tracking the build WAR task
        this.buildWARTask = new SimpleLongRunningTask();
        this.buildWARTask.setTaskName("Building WAR File");
        
        this.wizardPanels = new ValidatablePanel[] {
                this.experimentOverviewPanel,
                this.microarrayDesignPanel,
                this.microarrayDataPanel,
                this.annotationsPanel,
                this.perGeneImageDirectoriesPanel,
                this.webAppOutputPanel};
    }
    
    /**
     * Setter for the parent component. This component will be used as the
     * parent for any message dialogs that need to be displayed by this
     * controller
     * @param parentComponent the parent component
     */
    public void setParentComponent(Component parentComponent)
    {
        this.parentComponent = parentComponent;
    }
    
    /**
     * Getter for the parent component
     * @return the parent component
     */
    public Component getParentComponent()
    {
        return this.parentComponent;
    }
    
    /**
     * Getter for the wizard panels. You should be able to hand these over
     * to a {@link org.jax.util.gui.WizardFlipPanel}.
     * @return the panels
     */
    public JPanel[] getWizardPanels()
    {
        return this.wizardPanels;
    }

    /**
     * {@inheritDoc}
     */
    public void addWizardListener(WizardListener wizardListener)
    {
        this.wizardEventSupport.addWizardListener(wizardListener);
    }

    /**
     * {@inheritDoc}
     */
    public void removeWizardListener(WizardListener wizardListener)
    {
        this.wizardEventSupport.addWizardListener(wizardListener);
    }

    /**
     * {@inheritDoc}
     */
    public boolean cancel()
    {
        this.wizardEventSupport.fireWizardCancelled();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean finish() throws IllegalStateException
    {
        if(this.isFinishValid())
        {
            try
            {
                this.isFinishing = SafeAWTInvoker.safeCallAndWait(
                        new Callable<Boolean>()
                        {
                            /**
                             * {@inheritDoc}
                             */
                            public Boolean call() throws Exception
                            {
                                return PubArrayWizardController.this.validateCurrPanel();
                            }
                        });
                
                if(this.isFinishing)
                {
                    this.wizardEventSupport.fireWizardValidityChanged();
                    
                    this.buildWARTask.setWorkUnitsCompleted(0);
                    this.webAppOutputPanel.getProgressPanel().addTaskToTrack(
                            this.buildWARTask,
                            true);
                    this.buildWebApplication();
                    this.buildWARTask.setWorkUnitsCompleted(1);
                    
                    this.wizardEventSupport.fireWizardFinished();
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch(Exception ex)
            {
                String title = "Failed to Build Web Application";
                LOG.log(Level.SEVERE,
                        title,
                        ex);
                MessageDialogUtilities.error(
                        this.parentComponent,
                        ex.getMessage(),
                        title);
                return false;
            }
        }
        else
        {
            throw new IllegalStateException("calling finish is not valid");
        }
    }
    
    private boolean validateCurrPanel()
    {
        return this.wizardPanels[this.currPanelIndex].validateData();
    }
    
    private void buildWebApplication()
    throws IllegalFormatException, IOException, SQLException
    {
        // create the database in a temporary directory
        File dbTempDir = FileUtilities.createTempDir();
        
        try
        {
            this.buildDatabase(dbTempDir);
            
            // OK we have our DB... now merge it with the WAR template and
            // presto! ... we have a web app
            this.buildWARTask.setTaskName(
                    "Packaging Up Web Application Into WAR File (Final Step)");
            ZipInputStream inWARTemplate = new ZipInputStream(
                    PubArrayWizardController.class.getResourceAsStream(
                            WAR_TEMPLATE_RESOURCE));
            File outWARFile = this.webAppOutputPanel.getWebApplicationFile();
            ZipOutputStream warOut = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(outWARFile)));
            FileUtilities.unzipToZip(
                    inWARTemplate,
                    warOut);
            FileUtilities.zipRecursive(
                    dbTempDir,
                    warOut,
                    "WEB-INF/classes/");
            this.zipPerProbeImages(warOut);
            warOut.flush();
            warOut.close();
        }
        finally
        {
            // clean up!
            FileUtilities.recursiveDelete(dbTempDir);
        }
    }
    
    private void zipPerProbeImages(ZipOutputStream warOut) throws IOException
    {
        List<PerGeneImageDirectoryDescription> perProbeImageDirDesc =
            this.perGeneImageDirectoriesPanel.getDescriptions();
        int count = perProbeImageDirDesc.size();
        for(int i = 0; i < count; i++)
        {
            FileUtilities.zipRecursive(
                    perProbeImageDirDesc.get(i).getDirectory(),
                    warOut,
                    PerGeneImageDirectoryDescription.PER_PROBE_IMAGE_PREFIX + i + "/");
        }
    }

    /**
     * Builds the database by reading in all of the files that the user
     * provided to the wizard
     * @param dbDirectory
     *          the directory to write the database to
     * @throws IllegalFormatException
     *          if any of the files that we're parsing are not properly
     *          formatted
     * @throws IOException
     *          if we run into trouble reading/writing data
     * @throws SQLException
     *          if JDBC doesn't like what we're trying to do
     */
    private final void buildDatabase(File dbDirectory)
    throws IllegalFormatException, IOException, SQLException
    {
        final CandidateDatabaseManager candidateDatabaseManager =
            new CandidateDatabaseManager();
        
        // the experiment metadata
        candidateDatabaseManager.setExperimentMetadata(new ExperimentMetadata(
                this.experimentOverviewPanel.getExperimentName(),
                this.experimentOverviewPanel.getDescription()));
        
        // the design file
        final FlatFileDescription designFileDesc =
            this.microarrayDesignPanel.getFlatFileDescription();
        LOG.info("Loading design file: " + designFileDesc.getFlatFile().getPath());
        this.buildWARTask.setTaskName(
                "Loading Design File (" +
                designFileDesc.getFlatFile().getName() + ")");
        final FlatFileReader designFlatFileReader = designFileDesc.createReader();
        candidateDatabaseManager.uploadDesignFile(
                designFileDesc.getTableName(),
                designFlatFileReader);
        designFlatFileReader.close();
        candidateDatabaseManager.setMatchDesignAndDataOnFactor(
                this.microarrayDesignPanel.getDesignColumnToMatch());
        
        // the data file
        final FlatFileDescription dataFileDesc =
            this.microarrayDataPanel.getFlatFileDescription();
        this.buildWARTask.setTaskName(
                "Loading Data File (" +
                dataFileDesc.getFlatFile().getName() + ")");
        LOG.info("Loading data file: " + dataFileDesc.getFlatFile().getPath());
        final FlatFileReader dataFlatFileReader = dataFileDesc.createReader();
        candidateDatabaseManager.uploadDataFile(
                dataFileDesc.getTableName(),
                dataFlatFileReader);
        dataFlatFileReader.close();
        
        // the annotation files
        List<FlatFileDescription> annotationDescriptions =
            this.annotationsPanel.getFlatFileDescriptions();
        for(FlatFileDescription currAnnoDesc: annotationDescriptions)
        {
            this.buildWARTask.setTaskName(
                    "Loading Annotation File (" +
                    currAnnoDesc.getFlatFile().getName() + ")");
            LOG.info(
                    "Loading annotation file: " +
                    currAnnoDesc.getFlatFile().getAbsolutePath());
            FlatFileReader currAnnoFFR = currAnnoDesc.createReader();
            candidateDatabaseManager.uploadAnnotationFile(
                    currAnnoDesc.getTableName(),
                    currAnnoDesc.getTableName(),
                    currAnnoFFR);
            currAnnoFFR.close();
        }
        
        // the image dirs
        candidateDatabaseManager.setPerGeneImageDirectories(
                this.perGeneImageDirectoriesPanel.getDescriptions());
        
        this.buildWARTask.setTaskName("Building Database From Flat Files");
        String dbPath = dbDirectory.getPath() + "/" + DATABASE_NAME;
        ConnectionManager connectionMgr = new HSQLDBConnectionManager(
                dbPath,
                false);
        Connection connection = connectionMgr.createConnection();
        PersistenceManager persistenceMgr = new PersistenceManager();
        
        LOG.info("Writing DB to: " + dbPath);
        persistenceMgr.writeCandidatesToDatabase(
                connection,
                candidateDatabaseManager);
        connection.commit();
        connectionMgr.shutdownDatabase(connection);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean goNext() throws IllegalStateException
    {
        if(this.isNextValid() && this.validateCurrPanel())
        {
            this.currPanelIndex++;
            
            // The first time the user goes to the panel where they select a
            // web app file name we want to help them by suggesting a name
            // based on the experiment name that they chose
            if(this.wizardPanels[this.currPanelIndex] == this.webAppOutputPanel &&
               !this.webAppFileNameInitialized)
            {
                String experimentName = this.experimentOverviewPanel.getExperimentName();
                this.webAppOutputPanel.updateFileWithExperimentName(experimentName);
                this.webAppFileNameInitialized = true;
            }
            
            this.wizardEventSupport.fireWizardGoNextSucceeded();
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
    public boolean goPrevious() throws IllegalStateException
    {
        if(this.isPreviousValid())
        {
            this.currPanelIndex--;
            this.wizardEventSupport.fireWizardGoPreviousSucceeded();
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
    public void help()
    {
        try
        {
            BasicService basicService =
                (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
            if(basicService.isWebBrowserSupported())
            {
                // get the code base making sure that there is a trailing '/'
                URL codeBaseURL = basicService.getCodeBase();
                String codeBaseString = codeBaseURL.toString();
                if(!codeBaseString.endsWith("/"))
                {
                    codeBaseString = codeBaseString + "/";
                }
                
                basicService.showDocument(new URL(codeBaseString + "help.html"));
            }
            else
            {
                this.saySorryButNoHelpForYou();
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Failed to show help",
                    ex);
            this.saySorryButNoHelpForYou();
        }
    }
    
    private void saySorryButNoHelpForYou()
    {
        MessageDialogUtilities.inform(
                this.parentComponent,
                "Sorry, but the help service does not appear to be " +
                "available. Please go to the PubArray website for help " +
                "documentation.",
                "Help Service Missing");
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFinishValid()
    {
        return this.currPanelIndex == this.wizardPanels.length - 1 && !this.isFinishing;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNextValid()
    {
        return this.currPanelIndex < this.wizardPanels.length - 1 && !this.isFinishing;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPreviousValid()
    {
        return this.currPanelIndex > 0 && !this.isFinishing;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final String prefix = "PubArray Web Application Builder: ";
        ValidatablePanel currPanel = this.wizardPanels[this.currPanelIndex];
        if(currPanel == this.experimentOverviewPanel)
        {
            return prefix + "Experiment Overview";
        }
        else if(currPanel == this.microarrayDesignPanel)
        {
            return prefix + "Load Design File";
        }
        else if(currPanel == this.microarrayDataPanel)
        {
            return prefix + "Load Data File";
        }
        else if(currPanel == this.annotationsPanel)
        {
            return prefix + "Load Annotations and Statistics (Optional)";
        }
        else if(currPanel == this.perGeneImageDirectoriesPanel)
        {
            return prefix + "Load Per-Gene Image Directories (Optional)";
        }
        else if(currPanel == this.webAppOutputPanel)
        {
            return prefix + "Create WAR File";
        }
        else
        {
            throw new IllegalStateException(
                    "Internal Error: Failed to match " + currPanel +
                    " to a known panel!");
        }
    }
}

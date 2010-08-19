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

package org.jax.pubarray.commandline;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jax.pubarray.builder.PubArrayWizardController;
import org.jax.pubarray.db.CandidateDatabaseManager;
import org.jax.pubarray.db.ConnectionManager;
import org.jax.pubarray.db.ExperimentMetadata;
import org.jax.pubarray.db.HSQLDBConnectionManager;
import org.jax.pubarray.db.PersistenceManager;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FileUtilities;
import org.jax.util.io.FlatFileReader;
import org.jax.util.io.IllegalFormatException;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ImportDataMain
{
    private final CandidateDatabaseManager candidateDatabaseManager;
    private final PersistenceManager persistenceManager;
    
    private final File designFile;
    private final File dataFile;
    private final List<AnnotationDirectory> annotationDirs;
    private final File warFile;
    
    // this is the name that we should use for the database that we construct
    private static final String DATABASE_NAME = "pub-array-db";
    
    // this is the WAR resource that contains everything but the database.
    // When this resource is combined with the database that we create then
    // we have a real web application
    private static final String WAR_TEMPLATE_RESOURCE = "/pub-array-gwt-server-1.0.war";
    
    /**
     * Constructor
     * @param experimentMetadata
     *          the experiment metadata
     * @param warFile
     *          the WAR file that we should create
     * @param designFile
     *          the design file to import
     * @param dataFile
     *          the data file to import
     * @param annotationDirs
     *          the annotation dirs to import
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public ImportDataMain(
            ExperimentMetadata experimentMetadata,
            File warFile,
            File designFile,
            File dataFile,
            List<AnnotationDirectory> annotationDirs) throws SQLException
    {
        this.warFile = warFile;
        this.designFile = designFile;
        this.dataFile = dataFile;
        this.annotationDirs = annotationDirs;
        
        this.candidateDatabaseManager = new CandidateDatabaseManager();
        this.candidateDatabaseManager.setExperimentMetadata(experimentMetadata);
        this.persistenceManager = new PersistenceManager();
    }

    /**
     * Do the import
     * @throws SQLException if JDBC doesn't like what we're doing
     * @throws IOException  if we're thrown one
     * @throws IllegalFormatException
     *                      if the given files aren't properly formatted
     */
    public void importData() throws SQLException, IOException, IllegalFormatException
    {
        File tempDir = FileUtilities.createTempDir();
        try
        {
            System.out.println("Importing design: " + this.toPrettyName(this.designFile));
            this.candidateDatabaseManager.uploadDesignFile(
                    this.toPrettyName(this.designFile),
                    this.fileToReader(this.designFile));
            
            System.out.println("Importing data: " + this.toPrettyName(this.dataFile));
            this.candidateDatabaseManager.uploadDataFile(
                    this.toPrettyName(this.dataFile),
                    this.fileToReader(this.dataFile));
            
            for(AnnotationDirectory annotationDir: this.annotationDirs)
            {
                this.importAnnotationDir(annotationDir);
            }
            
            String dbPath = tempDir.getPath() + "/" + DATABASE_NAME;
            System.out.println("Writing to database path: " + dbPath);
            final ConnectionManager connectionManager = new HSQLDBConnectionManager(
                    dbPath,
                    false);
            final Connection connection = connectionManager.createConnection();
            
            this.persistenceManager.writeCandidatesToDatabase(
                    connection,
                    this.candidateDatabaseManager);
            
            connection.commit();
            connectionManager.shutdownDatabase(connection);
            
            ZipInputStream inWARTemplate = new ZipInputStream(
                    PubArrayWizardController.class.getResourceAsStream(
                            WAR_TEMPLATE_RESOURCE));
            ZipOutputStream warOut = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(this.warFile)));
            FileUtilities.unzipToZip(
                    inWARTemplate,
                    warOut);
            FileUtilities.zipRecursive(
                    tempDir,
                    warOut,
                    "WEB-INF/classes/");
            warOut.flush();
            warOut.close();
        }
        finally
        {
            FileUtilities.recursiveDelete(tempDir);
        }
    }
    
    private void importAnnotationDir(AnnotationDirectory annotationDir) throws IllegalFormatException, IOException
    {
        int fileNumber = 1;
        for(File annoFile: annotationDir.getDirectory().listFiles())
        {
            System.out.println(
                    annotationDir.getDirectory() + " (Category:" +
                    annotationDir.getCategoryName() + 
                    ") uploading annotation file number " + fileNumber +
                    ": " + this.toPrettyName(annoFile));
            this.candidateDatabaseManager.uploadAnnotationFile(
                    annotationDir.getCategoryName(),
                    this.toPrettyName(annoFile),
                    this.fileToReader(annoFile));
            fileNumber++;
        }
    }

    private FlatFileReader fileToReader(File file) throws FileNotFoundException
    {
        return new FlatFileReader(
                new BufferedReader(new FileReader(file)),
                CommonFlatFileFormat.TAB_DELIMITED_UNIX);
    }
    
    private String toPrettyName(File file)
    {
        String prettyName = file.getName();
        
        // remove extension
        int lastDotIndex = prettyName.lastIndexOf('.');
        if(lastDotIndex >= 1)
        {
            prettyName = prettyName.substring(0, lastDotIndex);
        }
        
        // TODO this may be too aggressive. It's good for now though
        return prettyName.replace('.', ' ').replace('_', ' ');
    }
    
    private static class AnnotationDirectory
    {
        private final File directory;
        
        private final String categoryName;

        /**
         * Constructor
         * @param directory
         *          the dir file
         * @param categoryName
         */
        public AnnotationDirectory(File directory, String categoryName)
        {
            this.directory = directory;
            this.categoryName = categoryName;
        }
        
        /**
         * Getter for the category
         * @return the categoryName
         */
        public String getCategoryName()
        {
            return this.categoryName;
        }
        
        /**
         * Getter for the dir file
         * @return the directory
         */
        public File getDirectory()
        {
            return this.directory;
        }
    }
    
    /**
     * Importer application entry point
     * @param args don't care
     * @throws IOException if we have a problem reading or writing data
     * @throws SQLException if we have a problem with JDBC
     * @throws IllegalFormatException if we find formatting problems in the data
     */
    public static void main(String[] args) throws IOException, SQLException, IllegalFormatException
    {
        BufferedReader bufferedIn = new BufferedReader(
                new InputStreamReader(System.in));
        
        System.out.println("Enter WAR File Name: ");
        String warFile = bufferedIn.readLine();
        
        System.out.println("Name Your Experiment: ");
        String experimentName = bufferedIn.readLine();
        
        System.out.println("Describe Your Experiment (use '\' for line continuation): ");
        StringBuilder experimentDescription = new StringBuilder();
        boolean keepDescribing;
        do
        {
            String currLine = bufferedIn.readLine();
            
            keepDescribing = currLine.endsWith("\\");
            if(keepDescribing)
            {
                experimentDescription.append(currLine.substring(
                        0,
                        currLine.length() - 1));
                experimentDescription.append('\n');
            }
            else
            {
                experimentDescription.append(currLine);
            }
        } while(keepDescribing);
        
        System.out.println("Enter Design File Location: ");
        File designFile = new File(bufferedIn.readLine());
        
        System.out.println("Enter Data File Location: ");
        File dataFile = new File(bufferedIn.readLine());
        
        List<AnnotationDirectory> annotationDirs = new ArrayList<AnnotationDirectory>();
        String currAnnotationDirString = null;
        String currAnnotationCategoryString = null;
        do
        {
            System.out.println("Enter an Annotation Directory (Empty When Complete)");
            currAnnotationDirString = bufferedIn.readLine();
            
            if(currAnnotationDirString != null && currAnnotationDirString.trim().length() > 0)
            {
                File currAnnotationDir = new File(currAnnotationDirString);
                
                System.out.println("Enter an Annotation Category Name (Empty For Default)");
                currAnnotationCategoryString = bufferedIn.readLine().trim();
                currAnnotationCategoryString = currAnnotationCategoryString.trim();
                if(currAnnotationCategoryString.length() == 0)
                {
                    currAnnotationCategoryString = null;
                }
                
                annotationDirs.add(new AnnotationDirectory(
                        currAnnotationDir,
                        currAnnotationCategoryString));
            }
        } while(currAnnotationDirString != null && currAnnotationDirString.length() > 0);
        
        ImportDataMain importer = new ImportDataMain(
                new ExperimentMetadata(
                        experimentName,
                        experimentDescription.toString()),
                new File(warFile),
                designFile,
                dataFile,
                annotationDirs);
        importer.importData();
    }
}

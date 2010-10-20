/*
 * Copyright (c) 2010 The Jackson Laboratory
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

package org.jax.pubarray.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.DataType;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.TypeEvidence;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileFormat;
import org.jax.util.io.FlatFileReader;
import org.jax.util.io.FlatFileWriter;
import org.jax.util.io.IllegalFormatException;

/**
 * A candidate database is for temporarily storing our pub-array tables
 * until we decide to write them to the database. If you add this class to your
 * web.xml file as a servlet listener it will add itself to the servlet context
 * using its class name as the key
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CandidateDatabaseManager
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            CandidateDatabaseManager.class.getName());
    
    /*package-protected*/ static final FlatFileFormat STORAGE_FORMAT =
        CommonFlatFileFormat.CSV_RFC_4180;
    
    private ExperimentMetadata experimentMetadata = new ExperimentMetadata("", "");
    private CandidateTable designTable = new CandidateTable();
    private CandidateTable dataTable = new CandidateTable();
    private Map<String, CandidateTable> annotationTables =
        Collections.synchronizedMap(new HashMap<String, CandidateTable>());
    private List<String> orderedAnnotationTableKeys =
        Collections.synchronizedList(new ArrayList<String>());
    private String matchDesignAndDataOnFactor = null;

    private List<PerGeneImageDirectoryDescription> perGeneImageDirectoryDescriptions;
    
    /**
     * Constructor
     */
    public CandidateDatabaseManager()
    {
    }
    
    /**
     * This value is the design factor used to match up the design rows with
     * the data header names. A null value for this property indicates that
     * matching should be done on ordering instead of on any design factor
     * @return the design factor or null if we should match on order
     */
    public String getMatchDesignAndDataOnFactor()
    {
        return this.matchDesignAndDataOnFactor;
    }
    
    /**
     * Setter for the design factor to match design and data on
     * @see #getMatchDesignAndDataOnFactor()
     * @param matchDesignAndDataOnFactor
     *          the factor to use or null if we should just rely on order
     */
    public void setMatchDesignAndDataOnFactor(String matchDesignAndDataOnFactor)
    {
        this.matchDesignAndDataOnFactor = matchDesignAndDataOnFactor;
    }
    
    /**
     * Getter for the experiment's metadata
     * @return the experiment's metadata
     */
    public ExperimentMetadata getExperimentMetadata()
    {
        return this.experimentMetadata;
    }
    
    /**
     * Setter for the experiment's metadata
     * @param experimentMetadata the experiments metadata
     */
    public void setExperimentMetadata(
            ExperimentMetadata experimentMetadata)
    {
        this.experimentMetadata = experimentMetadata;
    }
    
    /**
     * Write the given source flat file to the sink flat file (with the
     * exception of the header row) and at the same time infer column
     * metadata
     * @param minPermissibleColumnCount
     *          the minimum number of columns that the input is required
     *          to have
     * @param source
     *          the source to read from
     * @param sink
     *          the sink to write to
     * @return
     *          the inferred metadata
     * @throws IllegalFormatException
     *          if the formating is bad
     * @throws IOException
     *          if we have trouble reading or writing data
     */
    private TableColumnMetadata[] inferMetadataAndWriteSourceToSink(
            int minPermissibleColumnCount,
            FlatFileReader source,
            FlatFileWriter sink)
    throws IllegalFormatException, IOException
    {
        // use the 1st row to initialize the column metadata
        String[] currRow = source.readRow();
        if(currRow == null)
        {
            throw new IllegalFormatException(
                    "The given file appears to be empty");
        }
        else if(currRow.length == 0 && minPermissibleColumnCount > 0)
        {
            throw new IllegalFormatException(
                    "Failed to parse any columns in given file");
        }
        else if(currRow.length < minPermissibleColumnCount)
        {
            throw new IllegalFormatException(
                    "Only detected one column in the given input file " +
                    "where at least two are required. The first table " +
                    "cell contents were parsed as: \"" + currRow[0] + "\"");
        }
        
        TableColumnMetadata[] colMetadata =
            new TableColumnMetadata[currRow.length];
        for(int colIndex = 0; colIndex < colMetadata.length; colIndex++)
        {
            colMetadata[colIndex] = new TableColumnMetadata(currRow[colIndex]);
        }
        
        // okey dokey, now we're getting into the contents of
        // the table. note that rowNum only matters for informing the
        // user about what we found in the file and where we found it
        for(int rowNum = 1;
            (currRow = source.readRow()) != null;
            rowNum++)
        {
            if(currRow.length != colMetadata.length)
            {
                throw new IllegalFormatException(
                        "The column count of " + currRow.length +
                        " found at row number " + rowNum + " does not " +
                        "match the column count of " + colMetadata.length +
                        " found in all of the previous rows.");
            }
            
            // do best effort type inference on the given row
            for(int colIndex = 0; colIndex < currRow.length; colIndex++)
            {
                TableColumnMetadata currColMeta = colMetadata[colIndex];
                String currCellContents = currRow[colIndex];
                
                // make sure we keep track of the max string length
                if(currCellContents.length() > currColMeta.getLongestStringLength())
                {
                    currColMeta.setLongestStringLength(currCellContents.length());
                }
                
                // loop through the type inference logic
                // TODO be smarter about empty strings
                boolean doneInferringType = false;
                while(!doneInferringType)
                {
                    DataType currInferredType = currColMeta.getDataType();
                    
                    if(currInferredType == null)
                    {
                        // start off with the most restrictive formatting
                        // possible (an integer)
                        currColMeta.setDataType(DataType.INTEGER);
                        currColMeta.setTypeInferenceEvidence(
                                new TypeEvidence(
                                        currCellContents,
                                        rowNum));
                    }
                    else if(currInferredType == DataType.INTEGER)
                    {
                        // see if this conforms to the integer format
                        if(currCellContents.trim().length() == 0 ||
                           TableColumnMetadata.isParsableAsInteger(currCellContents))
                        {
                            doneInferringType = true;
                        }
                        else
                        {
                            // jump to the next less restrictive data type
                            currColMeta.setDataType(DataType.REAL);
                            currColMeta.setTypeInferenceEvidence(
                                    new TypeEvidence(
                                            currCellContents,
                                            rowNum));
                        }
                    }
                    else if(currInferredType == DataType.REAL)
                    {
                        // see if this conforms to the real format
                        if(currCellContents.trim().length() >= 1 &&
                           !TableColumnMetadata.isParsableAsReal(currCellContents))
                        {
                            // parse as text which allows any format
                            currColMeta.setDataType(DataType.TEXT);
                            currColMeta.setTypeInferenceEvidence(
                                    new TypeEvidence(
                                            currCellContents,
                                            rowNum));
                        }
                        
                        // either way we're done
                        doneInferringType = true;
                    }
                    else
                    {
                        assert currInferredType == DataType.TEXT;
                        doneInferringType = true;
                    }
                }
            }
            
            // write the row to temporary storage (we don't want to hold
            // all of this in memory after all!)
            sink.writeRow(currRow);
        }
        
        sink.flush();
        
        return colMetadata;
    }
    
    /**
     * Load the design data from the reader into temporary storage
     * @param tableName
     *          the table name to use
     * @param flatFileReader
     *          the flat file reader to load
     * @throws IOException if there is a problem with IO
     * @throws IllegalFormatException if there is a problem with data formatting
     */
    public void uploadDesignFile(
            String tableName,
            FlatFileReader flatFileReader)
    throws IllegalFormatException, IOException
    {
        File designFile = File.createTempFile("design", null);
        FlatFileWriter flatFileWriter = new FlatFileWriter(
                new FileWriter(designFile),
                STORAGE_FORMAT);
        TableColumnMetadata[] colMetadata = this.inferMetadataAndWriteSourceToSink(
                2,
                flatFileReader,
                flatFileWriter);
        this.designTable = new CandidateTable(
                colMetadata,
                tableName,
                designFile);
    }
    
    /**
     * Getter for the design table. This should never be null
     * @return the design table
     */
    public CandidateTable getDesignTable()
    {
        return this.designTable;
    }
    
    /**
     * Load the data from the reader into temporary storage
     * @param tableName
     *          the table name to use
     * @param flatFileReader
     *          the flat file reader to load
     * @throws IOException
     *          if there is a problem reading or writing data
     * @throws IllegalFormatException
     *          if there is a problem with the way the data is formatted
     */
    public void uploadDataFile(
            String tableName,
            FlatFileReader flatFileReader) throws IllegalFormatException, IOException
    {
        File dataFile = null;
        TableColumnMetadata[] colMetadata = null;
        
        dataFile = File.createTempFile("data", "csv");
        FlatFileWriter flatFileWriter = new FlatFileWriter(
                new FileWriter(dataFile),
                STORAGE_FORMAT);
        
        // infer column data types at the same time that we write the
        // table to a temporary location on disk
        colMetadata = this.inferMetadataAndWriteSourceToSink(
                2,
                flatFileReader,
                flatFileWriter);
        
        // we require that everything after the ID column is a number of
        // some kind
        for(int colIndex = 1; colIndex < colMetadata.length; colIndex++)
        {
            if(colMetadata[colIndex].getDataType() == DataType.TEXT)
            {
                TypeEvidence evidence =
                    colMetadata[colIndex].getTypeInferenceEvidence();
                throw new IllegalFormatException(
                        "Error parsing data file. Expected all data " +
                        "input to be numeric, but found the following " +
                        "non-numeric value at row=" +
                        evidence.getRowNumber() +
                        ", column=" + (colIndex + 1) + ": \"" +
                        evidence.getCellContents() + "\"");
            }
        }
        
        this.dataTable = new CandidateTable(
                colMetadata,
                tableName,
                dataFile);
    }
    
    /**
     * Getter for the data table. This should never be null.
     * @return the data table
     */
    public CandidateTable getDataTable()
    {
        return this.dataTable;
    }
    
    /**
     * Load the annotations from the reader into temporary storage
     * @param categoryName
     *          an optional category grouping for the annotation
     * @param tableName
     *          the client side file name for the upload
     * @param flatFileReader
     *          the flat file reader to load
     * @throws IOException
     *          if we have problems reading or writing data
     * @throws IllegalFormatException
     *          if we find a problem with how the data is formatted
     */
    public void uploadAnnotationFile(
            String categoryName,
            String tableName,
            FlatFileReader flatFileReader)
    throws IllegalFormatException, IOException
    {
        if(categoryName == null)
        {
            categoryName = "Annotation Data";
        }
        
        File annotationFile = null;
        TableColumnMetadata[] colMetadata = null;
        
        annotationFile = File.createTempFile("annotation", "csv");
        FlatFileWriter flatFileWriter = new FlatFileWriter(
                new FileWriter(annotationFile),
                STORAGE_FORMAT);
        colMetadata = this.inferMetadataAndWriteSourceToSink(
                2,
                flatFileReader,
                flatFileWriter);
        
        CandidateTable annotationTable = new CandidateTable(
                colMetadata,
                categoryName,
                tableName,
                annotationFile);
        this.annotationTables.put(tableName, annotationTable);
        this.orderedAnnotationTableKeys.add(tableName);
    }
    
    /**
     * Getter for the annotation tables map
     * @return the mapping of annotation group names to annotation tables
     */
    public Map<String, CandidateTable> getAnnotationTables()
    {
        return this.annotationTables;
    }
    
    /**
     * Getter for the ordered annotation table keys
     * @return the orderedAnnotationTableKeys
     */
    public List<String> getOrderedAnnotationTableKeys()
    {
        return this.orderedAnnotationTableKeys;
    }

    /**
     * Upload the given per-probe image dir
     * @param perGeneImageDirectoryDescriptions the gene image dirs
     */
    public void setPerGeneImageDirectories(
            List<PerGeneImageDirectoryDescription> perGeneImageDirectoryDescriptions)
    {
        this.perGeneImageDirectoryDescriptions = perGeneImageDirectoryDescriptions;
    }
    
    /**
     * Getter for the gene image dirs
     * @return the gene image dirs
     */
    public List<PerGeneImageDirectoryDescription> getPerGeneImageDirectoryDescriptions()
    {
        return this.perGeneImageDirectoryDescriptions;
    }
}

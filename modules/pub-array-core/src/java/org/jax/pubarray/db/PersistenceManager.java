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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.jax.pubarray.gwtcommon.client.Filter;
import org.jax.pubarray.gwtcommon.client.GeneImageMetadata;
import org.jax.pubarray.gwtcommon.client.QualifiedColumnMetadata;
import org.jax.pubarray.gwtcommon.client.Query;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata;
import org.jax.pubarray.gwtcommon.client.TableData;
import org.jax.pubarray.gwtcommon.client.TableMetadata;
import org.jax.pubarray.gwtcommon.client.Filter.FilterCondition;
import org.jax.pubarray.gwtcommon.client.Query.SortDirection;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.DataType;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.io.FlatFileReader;
import org.jax.util.io.FlatFileWriter;
import org.jax.util.io.IllegalFormatException;

/**
 * Class responsible for PubArray's database IO
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PersistenceManager
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PersistenceManager.class.getName());
    
    private static final Set<String> EXTENSION_SET;
    static
    {
        EXTENSION_SET = new HashSet<String>();
        
        EXTENSION_SET.add("png");
        
        EXTENSION_SET.add("jpg");
        EXTENSION_SET.add("jpe");
        EXTENSION_SET.add("jpeg");
        
        EXTENSION_SET.add("gif");
    }
    
    // the column prefix used for all of the columns in the "normal" tables
    // (ie the non-meta tables)
    private static final String COL_NAME_PREFIX = "COL_";
    
    private static final String PER_GENE_IMAGE_CATEGORY_TABLE_NAME = "PER_GENE_IMAGE_CATEGORY_TABLE";
    private static final String PER_GENE_IMAGE_TABLE_NAME = "PER_GENE_IMAGE_TABLE";
    private static final String PER_GENE_CATEGORY_ID_COL_NAME = "PER_GENE_CATEGORY_ID";
    private static final String PER_GENE_CATEGORY_NAME_COL_NAME = "PER_GENE_CATEGORY_NAME";
    private static final String GENE_IMAGE_ID_COL_NAME = "GENE_IMAGE_ID";
    private static final String GENE_IMAGE_EXTENSION_COL_NAME = "GENE_IMAGE_EXTENSION";
    
    // experiment level metadata
    private static final String EXPERIMENT_METADATA_TABLE_NAME = "EXPERIMENT_DESCRIPTION";
    private static final String EXPERIMENT_NAME_COL = "EXPERIMENT_NAME";
    private static final String EXPERIMENT_DESCRIPTION_COL = "EXPERIMENT_DESCRIPTION";
    
    // metatable stuff
    private static final String TABLE_META_TABLE = "TABLE_META_TABLE";
    private static final String SQL_TABLE_NAME_COL = "SQL_TABLE_NAME";
    private static final String LOGICAL_TABLE_NAME_COL = "LOGICAL_TABLE_NAME";
    private static final String LOGICAL_TABLE_CATEGORY_COL = "LOGICAL_TABLE_CATEGORY";
    
    // annotation table stuff
    private static final String ANNOTATION_TABLE_NAME_PREFIX = "ANNOTATIONS_";

    private static final String DESIGN_TABLE_NAME = "EXPERIMENT_DESIGN";
    private static final String DATA_COL_INDEX_LOGICAL_NAME = "DATA_COLUMN_INDEX";
    private static final String DATA_TABLE_NAME = "EXPERIMENT_DATA";
    private static final String DATA_TABLE_LOGICAL_NAME = "Microarray Experiment Data";
    
    // metacolumn stuff
    private static final String COL_METADATA_TABLE_SUFFIX = "_COL_METADATA";
    private static final String COL_NUM_META_COL = "COL_NUMBER";
    private static final String COL_NAME_META_COL = "COL_NAME";
    private static final String COL_TYPE_META_COL = "COL_TYPE";
    private static final String COL_DESCRIPTION_META_COL = "COL_DESCRIPTION";
    
    /**
     * This is like {@link Query} except that it uses real table and column
     * names instead of the user friendly names that we present to the
     * client side interface.
     */
    private class NormalizedQuery
    {
        private final Query query;
        private final Set<String> joinTables;
        
        /**
         * Constructor
         * @param query
         *          the query with real table and column names rather than
         *          the client-side table names
         * @param joinTables
         *          a listing of all of the tables that need to be joined
         *          together
         */
        public NormalizedQuery(Query query, Set<String> joinTables)
        {
            this.query = query;
            this.joinTables = joinTables;
        }
        
        /**
         * Getter for the join tables
         * @return the joinTables
         */
        public Set<String> getJoinTables()
        {
            return this.joinTables;
        }
        
        /**
         * Getter for the query information
         * @return the query
         */
        public Query getQuery()
        {
            return this.query;
        }
    }
    
    /**
     * Write the given candidate database to the real database
     * @param connection
     *          the connection to use
     * @param candidateDatabase
     *          the candidates to write to DB
     * @throws SQLException
     *          if we get an exception from JDBC
     * @throws IOException
     *          if we we have trouble reading one of our cached table files
     * @throws IllegalFormatException
     *          if we find formatting problems in any of the candidate
     *          database's tables
     */
    public void writeCandidatesToDatabase(
            Connection connection,
            CandidateDatabaseManager candidateDatabase)
    throws SQLException, IOException, IllegalFormatException
    {
        this.createTableMetaTable(connection);
        
        // build tables with the candidate data
        this.createExperimentMetadataTable(
                connection,
                candidateDatabase.getExperimentMetadata());
        
        this.buildDesignTable(connection, candidateDatabase);
        
        CandidateTable dataTable = candidateDatabase.getDataTable();
        this.buildTable(
                connection,
                DATA_TABLE_NAME,
                dataTable.getMetadata(),
                dataTable.readFile());
        
        Map<String, CandidateTable> annotationTblMap =
            candidateDatabase.getAnnotationTables();
        List<String> annotationTblKeys =
            candidateDatabase.getOrderedAnnotationTableKeys();
        int annotationTableIndex = 0;
        for(String annotationTblKey: annotationTblKeys)
        {
            CandidateTable annotationTable = annotationTblMap.get(annotationTblKey);
            if(annotationTable == null)
            {
                throw new NullPointerException(
                        "Candidate annotation table key " + annotationTblKey +
                        " is null. Valid key strings are: " +
                        SequenceUtilities.toString(annotationTblMap.keySet()));
            }
            
            String tableName =
                ANNOTATION_TABLE_NAME_PREFIX + annotationTableIndex;
            this.buildTable(
                    connection,
                    tableName,
                    annotationTable.getMetadata(),
                    annotationTable.readFile());
            
            this.insertTableMetadata(
                    connection,
                    tableName,
                    annotationTblKey,
                    annotationTable.getCategoryName());
            
            annotationTableIndex++;
        }
        
        this.buildPerProbeImageTables(
                connection,
                candidateDatabase.getPerGeneImageDirectoryDescriptions());
    }

    private void buildPerProbeImageTables(
            Connection connection,
            List<PerGeneImageDirectoryDescription> perProbeImageDescs)
    throws SQLException, IOException
    {
        this.dropTableNamed(connection, PER_GENE_IMAGE_CATEGORY_TABLE_NAME);
        this.dropTableNamed(connection, PER_GENE_IMAGE_TABLE_NAME);
        
        int maxCategoryNameLength = 1;
        int maxGeneIdLength = 1;
        
        for(PerGeneImageDirectoryDescription currDesc: perProbeImageDescs)
        {
            if(maxCategoryNameLength < currDesc.getName().length())
            {
                maxCategoryNameLength = currDesc.getName().length();
            }
            
            for(String maybeImgFilename : currDesc.getDirectory().list())
            {
                if(this.getImageExtension(maybeImgFilename) != null)
                {
                    String geneId = this.getImageGeneId(maybeImgFilename);
                    if(maxGeneIdLength < geneId.length())
                    {
                        maxGeneIdLength = geneId.length();
                    }
                }
            }
        }
        
        int maxImageExtensionLength = 1;
        for(String currExt: EXTENSION_SET)
        {
            if(maxImageExtensionLength < currExt.length())
            {
                maxImageExtensionLength = currExt.length();
            }
        }
        
        // create the META table
        String createCategoryIdString = PER_GENE_CATEGORY_ID_COL_NAME + " INTEGER";
        String createCategoryNameString =
            PER_GENE_CATEGORY_NAME_COL_NAME + " VARCHAR(" + maxCategoryNameLength + ")";
        String createMetaTblString =
            "CREATE TABLE " + PER_GENE_IMAGE_CATEGORY_TABLE_NAME + " (" +
            createCategoryIdString + ", " +
            createCategoryNameString +
            ", PRIMARY KEY(" + PER_GENE_CATEGORY_ID_COL_NAME + "))";
        
        String createGeneIDString =
            GENE_IMAGE_ID_COL_NAME + " VARCHAR(" + maxGeneIdLength + ")";
        String createGeneExtString =
            GENE_IMAGE_EXTENSION_COL_NAME + " VARCHAR(" + maxImageExtensionLength + ")";
        LOG.info("Creating meta image table with: " + createMetaTblString);
        
        Statement createMetaTblStmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        createMetaTblStmt.executeUpdate(createMetaTblString);
        createMetaTblStmt.close();
        
        // create the image table
        String createImgTblString =
            "CREATE TABLE " + PER_GENE_IMAGE_TABLE_NAME + " (" +
            SequenceUtilities.toString(Arrays.asList(
                    createGeneIDString,
                    createGeneExtString,
                    createCategoryIdString)) +
            ", PRIMARY KEY(" +
            SequenceUtilities.toString(Arrays.asList(
                    GENE_IMAGE_ID_COL_NAME,
                    GENE_IMAGE_EXTENSION_COL_NAME,
                    PER_GENE_CATEGORY_ID_COL_NAME)) + "))";
        LOG.info("Creating image table with: " + createImgTblString);
        
        Statement createImgTblStmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        createImgTblStmt.executeUpdate(createImgTblString);
        createImgTblStmt.close();
        
        // fill the tables
        PreparedStatement insertMetaTblStmt = connection.prepareStatement(
                "INSERT INTO " + PER_GENE_IMAGE_CATEGORY_TABLE_NAME +
                " VALUES(?, ?)",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        PreparedStatement insertImgTblStmt = connection.prepareStatement(
                "INSERT INTO " + PER_GENE_IMAGE_TABLE_NAME +
                " VALUES(?, ?, ?)",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        int descCount = perProbeImageDescs.size();
        for(int descIndex = 0; descIndex < descCount; descIndex++)
        {
            PerGeneImageDirectoryDescription currDesc = perProbeImageDescs.get(descIndex);
            int colIndex = 1;
            
            insertMetaTblStmt.setInt(
                    colIndex,
                    descIndex);
            colIndex++;
            
            insertMetaTblStmt.setString(
                    colIndex,
                    currDesc.getName());
            colIndex++;
            
            insertMetaTblStmt.executeUpdate();
            
            for(String maybeImgFilename : currDesc.getDirectory().list())
            {
                String extension = this.getImageExtension(maybeImgFilename);
                if(extension != null)
                {
                    // TODO allow URL decoding for names
                    int imgColIndex = 1;
                    
                    insertImgTblStmt.setString(
                            imgColIndex,
                            this.getImageGeneId(maybeImgFilename));
                    imgColIndex++;
                    
                    insertImgTblStmt.setString(
                            imgColIndex,
                            extension);
                    imgColIndex++;
                    
                    insertImgTblStmt.setInt(
                            imgColIndex,
                            descIndex);
                    imgColIndex++;
                    
                    insertImgTblStmt.executeUpdate();
                }
            }
        }
        insertMetaTblStmt.close();
        insertImgTblStmt.close();
    }
    
    /**
     * Get the extension for the given file
     * @param fileName  the file name
     * @return          the extension or null
     */
    private String getImageExtension(String fileName)
    {
        String[] dotSplit = fileName.split("\\.");
        if(dotSplit.length >= 2)
        {
            String extension = dotSplit[dotSplit.length - 1];
            if(EXTENSION_SET.contains(extension))
            {
                return extension;
            }
            else
            {
                return null;
            }
                
        }
        else
        {
            return null;
        }
    }
    
    private String getImageGeneId(String fileName)
    {
        String[] dotSplit = fileName.split("\\.");
        if(dotSplit.length >= 2)
        {
            String extension = dotSplit[dotSplit.length - 1];
            return fileName.substring(0, fileName.length() - (extension.length() + 1));
        }
        else
        {
            return null;
        }
    }

    /**
     * Create the candidate design table
     * @param connection
     *          the connection to use
     * @param candidateDatabase
     *          the candidate database
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     * @throws IllegalFormatException
     *          if there is a formatting problem with the input files
     * @throws IOException
     *          if there is a problem reading the data files
     */
    private void buildDesignTable(
            Connection connection,
            CandidateDatabaseManager candidateDatabase)
    throws IOException, IllegalFormatException, SQLException
    {
        final String designFactorToMatch =
            candidateDatabase.getMatchDesignAndDataOnFactor();
        final CandidateTable candidateDesignTable =
            candidateDatabase.getDesignTable();
        
        // add the data column index to the metadata
        TableColumnMetadata[] originalDesignMetadata =
            candidateDesignTable.getMetadata();
        TableColumnMetadata[] newDesignMetadata =
            new TableColumnMetadata[originalDesignMetadata.length + 1];
        newDesignMetadata[0] = new TableColumnMetadata(
                DATA_COL_INDEX_LOGICAL_NAME,
                DataType.INTEGER,
                null);
        for(int i = 0; i < originalDesignMetadata.length; i++)
        {
            newDesignMetadata[i + 1] = originalDesignMetadata[i];
        }
        
        this.buildEmptyTable(
                connection,
                DESIGN_TABLE_NAME,
                newDesignMetadata);
        
        // prepare a statement using the given metadata
        PreparedStatement insertStatement = this.prepareInsert(
                connection,
                DESIGN_TABLE_NAME,
                newDesignMetadata);
        
        final FlatFileReader designTableContents = candidateDesignTable.readFile();
        TableColumnMetadata[] dataTableHeader =
            candidateDatabase.getDataTable().getMetadata();
        String[] currRow = null;
        
        int indexOfDesignFactorToMatch = -1;
        if(designFactorToMatch != null)
        {
            TableColumnMetadata[] designTableHeader =
                candidateDatabase.getDesignTable().getMetadata();
            for(int i = 0; i < designTableHeader.length; i++)
            {
                if(designTableHeader[i].getName().equals(designFactorToMatch))
                {
                    indexOfDesignFactorToMatch = i;
                    break;
                }
            }
            
            if(indexOfDesignFactorToMatch == -1)
            {
                throw new IllegalFormatException(
                        "Attempting to match design rows to data columns but " +
                        "failed to find design column named: " +
                        designFactorToMatch);
            }
        }
        
        Set<Integer> uniqueDataColumnMatches = new HashSet<Integer>();
        int designRowIndex;
        for(designRowIndex = 0;
            (currRow = designTableContents.readRow()) != null;
            designRowIndex++)
        {
            final String[] rowWithIndex = new String[currRow.length + 1];
            if(designFactorToMatch == null)
            {
                // since the design to match is null we'll use the row
                // ordering instead
                rowWithIndex[0] = Integer.toString(designRowIndex);
            }
            else
            {
                String designValueToMatch = currRow[indexOfDesignFactorToMatch];
                
                // we should skip the 1st column because it is the probeset ID
                int matchingDataColIndex = -1;
                for(int dataColIndex = 1; dataColIndex < dataTableHeader.length; dataColIndex++)
                {
                    if(dataTableHeader[dataColIndex].getName().equals(designValueToMatch))
                    {
                        matchingDataColIndex = dataColIndex;
                        break;
                    }
                }
                
                if(matchingDataColIndex == -1)
                {
                    throw new IllegalFormatException(
                            "Failed to find a data table header name matching " +
                            "the design table value of: \"" +
                            designValueToMatch + "\"");
                }
                else
                {
                    if(!uniqueDataColumnMatches.add(matchingDataColIndex))
                    {
                        throw new IllegalFormatException(
                                "Found duplicate value of " + designValueToMatch +
                                " in the design file. The design factor that we " +
                                "match data columns on must not contain any " +
                                "duplicate values.");
                    }
                    rowWithIndex[0] = Integer.toString(matchingDataColIndex);
                }
            }
            
            for(int designColIndex = 0; designColIndex < currRow.length; designColIndex++)
            {
                rowWithIndex[designColIndex + 1] = currRow[designColIndex];
            }
            
            this.insertRow(
                    insertStatement,
                    newDesignMetadata,
                    rowWithIndex);
        }
        
        // the number of rows in the design file should match the columns in
        // the data file - 1 (because of the probeset column)
        if(designRowIndex != dataTableHeader.length - 1)
        {
            throw new IllegalFormatException(
                    "The number of arrays described in the design file does " +
                    "not match the number of arrays described by the data file.");
        }
        
        insertStatement.close();
    }

    /**
     * Update the experiment-level metadata
     * @param connection
     *          the connection to use
     * @param experimentMetadata
     *          the experiment's metadata
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void createExperimentMetadataTable(
            Connection connection,
            ExperimentMetadata experimentMetadata)
    throws SQLException
    {
        this.dropTableNamed(connection, EXPERIMENT_METADATA_TABLE_NAME);
        
        StringBuilder createBuilder = new StringBuilder("CREATE TABLE ");
        createBuilder.append(EXPERIMENT_METADATA_TABLE_NAME);
        createBuilder.append(" (");
        
        // TODO I have to use LONGVARCHAR for now because HSQLDB doesn't have
        // a proper CLOB type
        createBuilder.append(EXPERIMENT_NAME_COL);
        createBuilder.append(" LONGVARCHAR NOT NULL, ");
        
        createBuilder.append(EXPERIMENT_DESCRIPTION_COL);
        createBuilder.append(" LONGVARCHAR NOT NULL, ");
        
        createBuilder.append("PRIMARY KEY(");
        createBuilder.append(EXPERIMENT_NAME_COL);
        createBuilder.append("))");
        
        // OK, do the create
        String createString = createBuilder.toString();
        
        LOG.info("creating experiment description table with: " + createString);
        
        Statement createStatement = connection.createStatement();
        createStatement.executeUpdate(createString);
        createStatement.close();
        
        if(experimentMetadata != null)
        {
            // the table is created now fill it with the only row that it will
            // ever contain
            PreparedStatement insertDescStatement = connection.prepareStatement(
                    "INSERT INTO " + EXPERIMENT_METADATA_TABLE_NAME + " VALUES (?, ?)",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            
            int colIndex = 1;
            
            insertDescStatement.setString(
                    colIndex,
                    experimentMetadata.getExperimentName());
            colIndex++;
            
            insertDescStatement.setString(
                    colIndex,
                    experimentMetadata.getExperimentDescription());
            colIndex++;
            
            insertDescStatement.executeUpdate();
            insertDescStatement.close();
        }
    }
    
    /**
     * Get the experiment metadata
     * @param connection
     *          the database connection to use
     * @return  the experiment metadata
     * @throws SQLException if JDBC doesn't like what we're doing
     */
    public ExperimentMetadata getExperimentMetadata(Connection connection) throws SQLException
    {
        Statement queryStatement = connection.createStatement();
        ResultSet queryResults = queryStatement.executeQuery(
                "SELECT * FROM " + EXPERIMENT_METADATA_TABLE_NAME);
        if(queryResults.next())
        {
            String name = queryResults.getString(EXPERIMENT_NAME_COL);
            String desc = queryResults.getString(EXPERIMENT_DESCRIPTION_COL);
            
            return new ExperimentMetadata(name, desc);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Create the metatable to represent the table level metadata
     * @param connection
     *          the connection to use
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void createTableMetaTable(Connection connection)
    throws SQLException
    {
        // TODO be smarter about cleanup. this is very bogus
        this.dropTableNamed(connection, TABLE_META_TABLE);
        
        // create the meta-table with column numbers (PK) and column names
        StringBuilder createBuilder = new StringBuilder("CREATE TABLE ");
        createBuilder.append(TABLE_META_TABLE);
        createBuilder.append(" (");
        
        createBuilder.append(SQL_TABLE_NAME_COL);
        createBuilder.append(" VARCHAR(64) NOT NULL, ");
        
        createBuilder.append(LOGICAL_TABLE_NAME_COL);
        createBuilder.append(" VARCHAR(512) NOT NULL, ");
        
        createBuilder.append(LOGICAL_TABLE_CATEGORY_COL);
        createBuilder.append(" VARCHAR(512) NOT NULL, ");
        
        createBuilder.append("PRIMARY KEY(");
        createBuilder.append(SQL_TABLE_NAME_COL);
        createBuilder.append("))");
        
        // OK, do the create
        String createString = createBuilder.toString();
        
        LOG.info("creating table meta-table with: " + createString);
        
        Statement createStatement = connection.createStatement();
        createStatement.executeUpdate(createString);
        createStatement.close();
    }
    
    /**
     * Insert the given data into the table metadata table
     * @param connection
     *          the DB connection to use
     * @param sqlTableName
     *          the true SQL name of the table
     * @param logicalTableName
     *          the logical name of the table (this is what you want a user
     *          to see)
     * @param logicalTableCategory
     *          the logical category of the table (this is a high level grouping
     *          like "annotations" or "statistics"
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void insertTableMetadata(
            Connection connection,
            String sqlTableName,
            String logicalTableName,
            String logicalTableCategory) throws SQLException
    {
        PreparedStatement fillMetaStatement = connection.prepareStatement(
                "INSERT INTO " + TABLE_META_TABLE + " VALUES (?, ?, ?)",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        
        int metaColIndex = 1;
        
        fillMetaStatement.setString(
                metaColIndex,
                sqlTableName);
        metaColIndex++;
        
        fillMetaStatement.setString(
                metaColIndex,
                logicalTableName);
        metaColIndex++;
        
        fillMetaStatement.setString(
                metaColIndex,
                logicalTableCategory);
        metaColIndex++;
        
        fillMetaStatement.executeUpdate();
        fillMetaStatement.close();
    }

    /**
     * Build the named database table using the given column metadata and
     * flat file data
     * @param connection
     *          the database connection
     * @param tableName
     *          the name of the table
     * @param columnMetadata
     *          metadata about the table columns
     * @param tableContents
     *          the flat file to get the table contents from
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     * @throws IllegalFormatException
     *          if we find bad formatting in the table contents
     * @throws IOException
     *          if we have trouble reading the data
     */
    private void buildTable(
            Connection connection,
            String tableName,
            TableColumnMetadata[] columnMetadata,
            FlatFileReader tableContents)
    throws SQLException, IOException, IllegalFormatException
    {
        this.buildEmptyTable(
                connection,
                tableName,
                columnMetadata);
        this.fillTable(
                connection,
                tableName,
                columnMetadata,
                tableContents);
    }
    
    /**
     * Just like
     * {@link #buildTable(Connection, String, TableColumnMetadata[], FlatFileReader)}
     * except there are no table contents to fill in
     * @param connection
     *          the database connection
     * @param tableName
     *          the name of the table
     * @param columnMetadata
     *          metadata about the table columns
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void buildEmptyTable(
            Connection connection,
            String tableName,
            TableColumnMetadata[] columnMetadata)
    throws SQLException
    {
        // build the metadata table
        String metaTableName = tableName + COL_METADATA_TABLE_SUFFIX;
        this.dropTableNamed(connection, metaTableName);
        this.createColumnMetaTable(
                connection,
                metaTableName,
                columnMetadata);
        this.fillColumnMetaTable(
                connection,
                metaTableName,
                columnMetadata);
        
        // Create the table which will hold the data
        this.dropTableNamed(connection, tableName);
        this.createTable(
                connection,
                tableName,
                columnMetadata);
    }
    
    /**
     * Create the metatable to represent the column metadata
     * @param connection
     *          the connection to use
     * @param metaTableName
     *          the meta table name
     * @param columnMetadata
     *          the column metadata
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void createColumnMetaTable(
            Connection connection,
            String metaTableName,
            TableColumnMetadata[] columnMetadata)
    throws SQLException
    {
        // what's the longest column name/description? We'll need this for
        // our VARCHAR
        int longestColNameLength = 1;
        int longestColDescLength = 1;
        for(int colIndex = 0; colIndex < columnMetadata.length; colIndex++)
        {
            TableColumnMetadata currMetaCol = columnMetadata[colIndex];
            
            int currNameLen = currMetaCol.getName().length();
            if(currNameLen > longestColNameLength)
            {
                longestColNameLength = currNameLen;
            }
            
            if(currMetaCol.getDescription() != null)
            {
                int currDescLen = currMetaCol.getDescription().length();
                if(currDescLen > longestColDescLength)
                {
                    longestColDescLength = currDescLen;
                }
            }
        }
        
        int longestColTypeLength = 1;
        for(DataType currDataType: DataType.values())
        {
            int currLen = currDataType.name().length();
            if(currLen > longestColTypeLength)
            {
                longestColTypeLength = currLen;
            }
        }
        
        // create the meta-table with column numbers (PK) and column names
        StringBuilder createBuilder = new StringBuilder("CREATE TABLE ");
        createBuilder.append(metaTableName);
        createBuilder.append(" (");
        
        createBuilder.append(COL_NUM_META_COL);
        createBuilder.append(" INTEGER, ");
        
        createBuilder.append(COL_NAME_META_COL);
        createBuilder.append(" VARCHAR(");
        createBuilder.append(Math.max(longestColNameLength, 1));
        createBuilder.append(") NOT NULL, ");
        
        createBuilder.append(COL_TYPE_META_COL);
        createBuilder.append(" VARCHAR(");
        createBuilder.append(Math.max(longestColTypeLength, 1));
        createBuilder.append(") NOT NULL, ");
        
        createBuilder.append(COL_DESCRIPTION_META_COL);
        createBuilder.append(" VARCHAR(");
        createBuilder.append(Math.max(longestColDescLength, 1));
        createBuilder.append("), ");
        
        createBuilder.append("PRIMARY KEY(");
        createBuilder.append(COL_NUM_META_COL);
        createBuilder.append("))");
        
        // OK, do the create
        String createString = createBuilder.toString();
        
        LOG.info("creating column meta-table with: " + createString);
        
        Statement createStatement = connection.createStatement();
        createStatement.executeUpdate(createString);
        createStatement.close();
    }

    /**
     * Fill the given meta-table with the data contained in the given
     * metadata array
     * @param connection
     *          the connection to use
     * @param metaTableName
     *          the meta table name
     * @param columnMetadata
     *          the column metadata
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void fillColumnMetaTable(
            Connection connection,
            String metaTableName,
            TableColumnMetadata[] columnMetadata)
    throws SQLException
    {
        PreparedStatement fillMetaStatement = connection.prepareStatement(
                "INSERT INTO " + metaTableName + " VALUES (?, ?, ?, ?)",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        
        for(int colIndex = 0; colIndex < columnMetadata.length; colIndex++)
        {
            TableColumnMetadata currColMetadata = columnMetadata[colIndex];
            int metaColIndex = 1;
            
            fillMetaStatement.setInt(
                    metaColIndex,
                    colIndex);
            metaColIndex++;
            
            fillMetaStatement.setString(
                    metaColIndex,
                    currColMetadata.getName());
            metaColIndex++;
            
            fillMetaStatement.setString(
                    metaColIndex,
                    currColMetadata.getDataType().name());
            metaColIndex++;
            
            fillMetaStatement.setString(
                    metaColIndex,
                    currColMetadata.getDescription());
            metaColIndex++;
            
            fillMetaStatement.executeUpdate();
        }
        
        fillMetaStatement.close();
    }

    /**
     * Drop table with the given table name
     * @param connection
     *          the connection to use
     * @param tableName
     *          the name of the table to drop
     * @return
     *          true if the table was dropped and false if there was nothing to
     *          do
     * @throws SQLException
     *          if we get an exception from JDBC
     */
    private boolean dropTableNamed(Connection connection, String tableName)
    throws SQLException
    {
        LOG.info("dropping " + tableName);
        ResultSet tableResults = this.getTableNamed(connection, tableName);
        try
        {
            if(tableResults.next())
            {
                // the table does exist. we can drop it
                LOG.info("dropping " + tableName);
                tableResults.close();
                
                Statement dropStatement = connection.createStatement(
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                dropStatement.executeUpdate(
                        "DROP TABLE " + tableName);
                dropStatement.close();
                
                LOG.info("done dropping " + tableName);
                
                return true;
            }
            else
            {
                LOG.info(
                        "nothing to do because table is missing: " +
                        tableName);
                return false;
            }
        }
        finally
        {
            // Note: JDBC allows closing a result set that has already been
            //       closed, so we're safe
            tableResults.close();
        }
    }
    
    /**
     * Creates an empty SQL table with the given table name
     * @param connection
     *          the connection to use
     * @param tableName
     *          the name of the new table
     * @param columnMetadata
     *          the metadata that describes the column types
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void createTable(
            Connection connection,
            String tableName,
            TableColumnMetadata[] columnMetadata)
    throws SQLException
    {
        LOG.info("creating table named: " + tableName);
        
        if(columnMetadata.length == 0)
        {
            throw new IllegalArgumentException(
                    "refusing to create a table with 0 columns!");
        }
        
        Statement createTblStmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
        
        StringBuilder createBuilder = new StringBuilder("CREATE TABLE ");
        createBuilder.append(tableName);
        createBuilder.append(" (");
        for(int colIndex = 0; colIndex < columnMetadata.length; colIndex++)
        {
            // the column name
            createBuilder.append(COL_NAME_PREFIX);
            createBuilder.append(colIndex);
            createBuilder.append(' ');
            
            // the column type
            TableColumnMetadata currColMeta = columnMetadata[colIndex];
            switch(currColMeta.getDataType())
            {
                case TEXT:
                {
                    // create a varchar capable of holding the longest
                    // string in this column
                    createBuilder.append("VARCHAR(");
                    createBuilder.append(Math.max(currColMeta.getLongestStringLength(), 1));
                    createBuilder.append(')');
                }
                break;
                
                case REAL:
                {
                    createBuilder.append("DOUBLE PRECISION");
                }
                break;
                
                case INTEGER:
                {
                    createBuilder.append("INTEGER");
                }
                break;
            }
            
            createBuilder.append(", ");
        }
        createBuilder.append(" PRIMARY KEY (");
        createBuilder.append(COL_NAME_PREFIX);
        createBuilder.append(0);
        createBuilder.append("))");
        
        String createString = createBuilder.toString();
        
        LOG.info("creating table with: " + createString);
        
        createTblStmt.executeUpdate(createString);
        createTblStmt.close();
        
        LOG.info("successfully created table named: " + tableName);
    }
    
    /**
     * Fill in the table using the given table contents
     * @param connection
     *          the database connection
     * @param tableName
     *          the name of the table
     * @param columnMetadata
     *          metadata about the table columns
     * @param tableContents
     *          the flat file to get the table contents from
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     * @throws IllegalFormatException
     *          if we run into a formatting problem when we're reading the
     *          table
     * @throws IOException 
     */
    private void fillTable(
            Connection connection,
            String tableName,
            TableColumnMetadata[] columnMetadata,
            FlatFileReader tableContents)
    throws SQLException, IOException, IllegalFormatException
    {
        // prepare a statement using the given metadata
        PreparedStatement insertStatement = this.prepareInsert(
                connection,
                tableName,
                columnMetadata);
        
        String[] currRow = null;
        while((currRow = tableContents.readRow()) != null)
        {
            this.insertRow(
                    insertStatement,
                    columnMetadata,
                    currRow);
        }
        
        insertStatement.close();
    }

    /**
     * Insert a row into the table using the given prepared statement
     * @param insertStatement
     *          the prepared statement that came from
     *          {@link #prepareInsert(Connection, String, TableColumnMetadata[])}
     * @param columnMetadata
     *          the metadata for this row. this will tell us which data
     *          types to use
     * @param rowToInsert
     *          the string values for the current row. we'll turn these into
     *          whatever data type is required by the column metadata
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private void insertRow(
            PreparedStatement insertStatement,
            TableColumnMetadata[] columnMetadata,
            String[] rowToInsert)
    throws SQLException
    {
        if(columnMetadata.length != rowToInsert.length)
        {
            throw new IllegalArgumentException(
                    "the length of the column metadata " +
                    SequenceUtilities.toString(Arrays.asList(columnMetadata)) +
                    " and row to insert " +
                    SequenceUtilities.toString(Arrays.asList(rowToInsert)) +
                    " do not match");
        }
        
        // fill in all of the values for the prepared statement paying attention
        // to the type metadata
        for(int colIndex = 0; colIndex < columnMetadata.length; colIndex++)
        {
            switch(columnMetadata[colIndex].getDataType())
            {
                case TEXT:
                {
                    insertStatement.setString(
                            colIndex + 1,
                            rowToInsert[colIndex]);
                }
                break;
                
                case INTEGER:
                {
                    String trimmedVal = rowToInsert[colIndex].trim();
                    if(trimmedVal.length() >= 1)
                    {
                        int intValue = Integer.parseInt(trimmedVal);
                        insertStatement.setInt(
                                colIndex + 1,
                                intValue);
                    }
                    else
                    {
                        insertStatement.setNull(
                                colIndex + 1,
                                Types.INTEGER);
                    }
                }
                break;
                
                case REAL:
                {
                    String trimmedVal = rowToInsert[colIndex].trim();
                    if(trimmedVal.length() >= 1)
                    {
                        double realValue = Double.parseDouble(trimmedVal);
                        insertStatement.setDouble(
                                colIndex + 1,
                                realValue);
                    }
                    else
                    {
                        insertStatement.setNull(
                                colIndex + 1,
                                Types.DOUBLE);
                    }
                }
                break;
            }
        }
        
        insertStatement.executeUpdate();
    }

    /**
     * Prepare an insert statement
     * @param connection
     *          the connection to use
     * @param tableName
     *          the table name to use
     * @param columnMetadata
     *          the metadata describing the table
     * @return
     *          the prepared statement for the insert
     * @throws SQLException
     *          if JDBC doesn't like our prepared statement
     */
    private PreparedStatement prepareInsert(
            Connection connection,
            String tableName,
            TableColumnMetadata[] columnMetadata)
    throws SQLException
    {
        StringBuilder insertStatementBuilder = new StringBuilder();
        
        insertStatementBuilder.append("INSERT INTO ");
        insertStatementBuilder.append(tableName);
        insertStatementBuilder.append(" VALUES (");
        for(int colIndex = 0; colIndex < columnMetadata.length; colIndex++)
        {
            if(colIndex >= 1)
            {
                insertStatementBuilder.append(", ");
            }
            insertStatementBuilder.append('?');
        }
        insertStatementBuilder.append(')');
        
        return connection.prepareStatement(
                insertStatementBuilder.toString(),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Get the result set for the given table
     * @param connection
     *          the DB connection to use
     * @param tableName
     *          the name of the table that we're looking for
     * @return
     *          the result set from
     *          {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * @throws SQLException
     *          if we get one from JDBC
     */
    private ResultSet getTableNamed(Connection connection, String tableName)
    throws SQLException
    {
        return connection.getMetaData().getTables(
                null,       // catalog
                null,       // schemaPattern
                tableName,  // tableNamePattern
                null);      // types
    }
    
    /**
     * Getter for the data table's column metadata
     * @param connection
     *          the DB connection to use
     * @return
     *          the metadata for the main data table
     * @throws SQLException
     *          if JDBC doesn't like us
     */
    public TableColumnMetadata[] getDataTableColumnMetadata(Connection connection) throws SQLException
    {
        return this.getColumnMetadata(
                connection,
                DATA_TABLE_NAME + COL_METADATA_TABLE_SUFFIX);
    }
    
    /**
     * Getter for the data table's metadata
     * @param connection
     *          the DB connection to use
     * @return
     *          the metadata for the main data table
     * @throws SQLException
     *          if JDBC doesn't like us
     */
    public TableMetadata getDataTableMetadata(Connection connection) throws SQLException
    {
        TableColumnMetadata[] colMeta = this.getDataTableColumnMetadata(connection);
        return new TableMetadata(
                DATA_TABLE_LOGICAL_NAME,
                DATA_TABLE_NAME,
                colMeta);
    }
    
    /**
     * Getter for the metadata
     * @param metaTableName
     *          the name of the meta table
     * @return
     *          the metadata
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private TableColumnMetadata[] getColumnMetadata(
            Connection connection,
            String metaTableName) throws SQLException
    {
        Statement metaQuery = connection.createStatement();
        ResultSet metaQueryResults =
            metaQuery.executeQuery(
                    "SELECT * FROM " + metaTableName +
                    " ORDER BY " + COL_NUM_META_COL);
        List<TableColumnMetadata> metadataList =
            new ArrayList<TableColumnMetadata>();
        
        while(metaQueryResults.next())
        {
            TableColumnMetadata currMetadata = new TableColumnMetadata(
                    metaQueryResults.getString(COL_NAME_META_COL),
                    DataType.valueOf(metaQueryResults.getString(COL_TYPE_META_COL)),
                    metaQueryResults.getString(COL_DESCRIPTION_META_COL));
            metadataList.add(currMetadata);
        }
        metaQueryResults.close();
        
        return metadataList.toArray(
                new TableColumnMetadata[metadataList.size()]);
    }

    /**
     * Run a query on the query using the given filters (AND together the conditions)
     * @param connection
     *          the DB connection to use
     * @param query
     *          the filters to apply
     * @param rowOffset
     *          the row offset to use (how far should we skip forward)
     * @param rowCount
     *          the row count (what is the max row count that we should return)
     * @return
     *          the table that gets returned from the query
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public TableData<Object> executeQuery(
            Connection connection,
            Query query,
            int rowOffset,
            int rowCount)
    throws SQLException
    {
        NormalizedQuery normalizedQuery = this.normalizeQuery(
                connection,
                query);
        return this.executeNormalizedQuery(
                connection,
                normalizedQuery,
                rowOffset,
                rowCount);
    }
    
    /**
     * Performs a query and writes the results to the given flat file writer
     * @param connection
     *          the DB connection to use
     * @param query
     *          the query to perform
     * @param flatFile
     *          the flat file to write to
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     * @throws IOException
     *          if the flat file writer doesn't like what we're doing
     */
    public void writeQueryResultsToFlatFile(
            Connection connection,
            Query query,
            FlatFileWriter flatFile)
    throws SQLException, IOException
    {
        NormalizedQuery normalizedQuery = this.normalizeQuery(
                connection,
                query);
        PreparedStatement queryStmt = this.prepareNormalizedQueryStatement(
                connection,
                normalizedQuery);
        ResultSet queryResults = queryStmt.executeQuery();
        
        int columnCount = queryResults.getMetaData().getColumnCount();
        while(queryResults.next())
        {
            String[] currRowStrings = new String[columnCount];
            for(int column = 0; column < columnCount; column++)
            {
                // JDBC uses 1-based indices... weird
                Object currCell = queryResults.getObject(column + 1);
                if(currCell == null)
                {
                    currRowStrings[column] = "";
                }
                else
                {
                    currRowStrings[column] = currCell.toString();
                }
            }
            flatFile.writeRow(currRowStrings);
        }
        
        queryResults.close();
        queryStmt.close();
    }

    /**
     * Generates a normalized query from the given query
     * @param connection
     *          the database connection to use
     * @param query
     *          the query to normalize
     * @return
     *          the normalized query
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private NormalizedQuery normalizeQuery(Connection connection, Query query) throws SQLException
    {
        List<TableMetadata> allTblMeta = this.getAnnotationTableMetadata(
                connection,
                true);
        allTblMeta.add(this.getDataTableMetadata(connection));
        
        QualifiedColumnMetadata[] oldTerms = query.getTermsOfInterest();
        QualifiedColumnMetadata[] newTerms = new QualifiedColumnMetadata[oldTerms.length];
        for(int i = 0; i < oldTerms.length; i++)
        {
            newTerms[i] = this.normalizeColumn(oldTerms[i], allTblMeta);
        }
        
        final QualifiedColumnMetadata newOrderBy;
        if(query.getOrderByColumn() == null)
        {
            newOrderBy = null;
        }
        else
        {
            newOrderBy = this.normalizeColumn(
                    query.getOrderByColumn(),
                    allTblMeta);
        }
        
        Filter[] oldFilters = query.getFilters();
        Filter[] newFilters = new Filter[oldFilters.length];
        for(int i = 0; i < oldFilters.length; i++)
        {
            newFilters[i] = this.normalizeFilter(
                    oldFilters[i],
                    allTblMeta);
        }
        
        Set<String> joinTables = this.extractJoinTableIds(
                query,
                allTblMeta);
        
        Query normQuery = new Query();
        normQuery.setFilters(newFilters);
        normQuery.setOrderByColumn(newOrderBy);
        normQuery.setSortDirection(query.getSortDirection());
        normQuery.setTermsOfInterest(newTerms);
        
        return new NormalizedQuery(
                normQuery,
                joinTables);
    }
    
    private Filter normalizeFilter(
            Filter filter,
            List<TableMetadata> tableMetaList)
    {
        QualifiedColumnMetadata normCol = this.normalizeColumn(
                filter.getColumn(),
                tableMetaList);
        if(normCol == null)
        {
            return null;
        }
        else
        {
            return new Filter(
                    normCol,
                    filter.getCondition(),
                    filter.getValue());
        }
    }

    private QualifiedColumnMetadata normalizeColumn(
            QualifiedColumnMetadata column,
            List<TableMetadata> tableMetaList)
    {
        TableMetadata table = this.getTableWithLogicalName(
                tableMetaList,
                column.getTableName());
        
        int index = TableColumnMetadata.getIndexOfColumnNamed(
                column.getName(),
                table.getColumnMetadata());
        if(index == -1)
        {
            return null;
        }
        else
        {
            return new QualifiedColumnMetadata(
                    table.getTableId(),
                    COL_NAME_PREFIX + index,
                    table.getColumnMetadata()[index].getDataType(),
                    null);
        }
    }
    
    /**
     * Extract the SQL join table names using the given table metadata
     * @param query
     *          the query
     * @param tableMetaList
     *          the metadata
     * @return
     *          the list of tables that should be joined together
     */
    private Set<String> extractJoinTableIds(
            Query query,
            List<TableMetadata> tableMetaList)
    {
        Set<String> tablesToJoin = new HashSet<String>();
        
        for(QualifiedColumnMetadata term: query.getTermsOfInterest())
        {
            TableMetadata tableMetaForTerm = this.getTableWithLogicalName(
                    tableMetaList,
                    term.getTableName());
            tablesToJoin.add(tableMetaForTerm.getTableId());
        }
        
        return tablesToJoin;
    }
    
    private TableMetadata getTableWithLogicalName(
            List<TableMetadata> tableMetaList,
            String logicalName)
    {
        for(TableMetadata table: tableMetaList)
        {
            if(table.getTableName().equals(logicalName))
            {
                return table;
            }
        }
        
        return null;
    }

    /**
     * Run a query using the given filters (AND together the conditions)
     * @param connection
     *          the DB connection to use
     * @param normalizedQuery
     *          the query
     * @param rowOffset
     *          the row offset to use (how far should we skip forward)
     * @param rowCount
     *          the row count (what is the max row count that we should return)
     * @return
     *          the table that gets returned from the query
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private TableData<Object> executeNormalizedQuery(
            Connection connection,
            NormalizedQuery normalizedQuery,
            int rowOffset,
            int rowCount)
    throws SQLException
    {
        if(rowOffset < 0)
        {
            throw new IllegalArgumentException(
                    "Negative row offsets are not allowed: " + rowCount);
        }
        
        PreparedStatement queryStmt = this.prepareNormalizedQueryStatement(
                connection,
                normalizedQuery);
        ResultSet queryResults = queryStmt.executeQuery();
        
        Object[][] data = this.queryResultsToObjectArrays(
                queryResults,
                0,
                rowOffset,
                rowCount);
        queryResults.last();
        int totalRowCount = queryResults.getRow();
        
        return new TableData<Object>(
                data,
                rowOffset,
                totalRowCount);
    }
    
    private Object[][] queryResultsToObjectArrays(
            ResultSet queryResults,
            int columnOffset,
            int rowOffset,
            int maxRowCount)
    throws SQLException
    {
        if(rowOffset > 0)
        {
            queryResults.absolute(rowOffset);
        }
        
        List<Object[]> data = new ArrayList<Object[]>();
        int colCount = queryResults.getMetaData().getColumnCount();
        for(int row = 0; queryResults.next() && row < maxRowCount; row++)
        {
            Object[] currRowData = new Object[colCount - columnOffset];
            for(int col = columnOffset; col < colCount; col++)
            {
                // we have to use wacky 1-based JDBC column indices
                currRowData[col - columnOffset] = queryResults.getObject(col + 1);
            }
            
            data.add(currRowData);
        }
        return data.toArray(new Object[data.size()][]);
    }

    /**
     * Prepare a query statement using the given parameters
     * @param connection
     *          the DB connection
     * @param normalizedQuery
     *          the normalized query
     * @return
     *          the prepared statement
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    private PreparedStatement prepareNormalizedQueryStatement(
            Connection connection,
            NormalizedQuery normalizedQuery)
    throws SQLException
    {
        String queryStmtString = this.normalizedQueryToSelectString(
                normalizedQuery);
        LOG.info("Performing query: " + queryStmtString.toString());
        
        PreparedStatement queryStmt = connection.prepareStatement(
                queryStmtString.toString(),
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        
        // fill in the '?' values for the prepared statement
        Filter[] filters = normalizedQuery.getQuery().getFilters();
        int statementParamIndex = 1;
        for(int filterIndex = 0; filterIndex < filters.length; filterIndex++)
        {
            Filter currFilter = filters[filterIndex];
            DataType dataType = currFilter.getColumn().getDataType();
            String valueStr = currFilter.getValue();
            FilterCondition condition = currFilter.getCondition();
            
            if(condition == FilterCondition.EXACTLY_MATCHES_ANY ||
               condition == FilterCondition.PARTIALLY_MATCHES_ANY)
            {
                StringTokenizer st = new StringTokenizer(valueStr);
                while(st.hasMoreTokens())
                {
                    String value = st.nextToken();
                    injectValueIntoStatement(
                            statementParamIndex,
                            condition,
                            dataType,
                            value,
                            queryStmt);
                    statementParamIndex++;
                }
            }
            else
            {
                this.injectValueIntoStatement(
                        statementParamIndex,
                        condition,
                        dataType,
                        valueStr,
                        queryStmt);
                statementParamIndex++;
            }
        }
        
        return queryStmt;
    }
    
    /**
     * Inject the given value into an SQL statement
     * @param parameterIndex
     *          the 1-based injection index
     * @param condition
     *          the filter condition (only care if it's a LIKE condition)
     * @param valueType
     *          the type of value that we're injecting
     * @param valueString
     *          the value string to inject
     * @param statement
     *          the statement that we inject that value into
     * @throws SQLException
     *          if JDBC doesn't like what we're trying to do
     * @throws NumberFormatException
     *          if we try to inject a number that is not formatted correctly
     */
    private void injectValueIntoStatement(
            int parameterIndex,
            FilterCondition condition,
            DataType valueType,
            String valueString,
            PreparedStatement statement) throws NumberFormatException, SQLException
    {
        switch(valueType)
        {
            case INTEGER:
            {
                statement.setInt(
                        parameterIndex,
                        Integer.parseInt(valueString.trim()));
            }
            break;
            
            case REAL:
            {
                statement.setDouble(
                        parameterIndex,
                        Double.parseDouble(valueString.trim()));
            }
            break;
            
            case TEXT:
            {
                // all strings should be trimmed and converted to upper case
                // (all comparisons are case insensitive)
                valueString = valueString.trim().toUpperCase();
                
                // for partial matching we need wildcards at the start and end
                if(condition == FilterCondition.PARTIALLY_MATCHES_ANY)
                {
                    valueString = '%' + valueString + '%';
                }
                
                statement.setString(parameterIndex, valueString);
            }
            break;
        }
    }
    
    private String normalizedQueryToSelectString(NormalizedQuery normalizedQuery)
    {
        // select all of the terms of interest
        StringBuilder queryStmtString = new StringBuilder("SELECT ");
        QualifiedColumnMetadata[] terms =
            normalizedQuery.getQuery().getTermsOfInterest();
        for(int i = 0; i < terms.length; i++)
        {
            if(i >= 1)
            {
                queryStmtString.append(", ");
            }
            queryStmtString.append(terms[i].getQualifiedName());
        }
        
        // the from tables
        queryStmtString.append(" FROM ");
        
        String[] tableNames =
            normalizedQuery.getJoinTables().toArray(new String[0]);
        if(tableNames.length == 0)
        {
            throw new IllegalArgumentException(
                    "bad query: no table names specified");
        }
        
        queryStmtString.append(
                SequenceUtilities.toString(Arrays.asList(tableNames)));
        
        Filter[] filters = normalizedQuery.getQuery().getFilters();
        if(tableNames.length >= 2 || filters.length >= 1)
        {
            queryStmtString.append(" WHERE ");
            
            // table joining
            final String firstColStr = '.' + COL_NAME_PREFIX + 0;
            for(int i = 1; i < tableNames.length; i++)
            {
                if(i >= 2)
                {
                    queryStmtString.append(" AND ");
                }
                queryStmtString.append('(');
                queryStmtString.append(tableNames[0]);
                queryStmtString.append(firstColStr);
                queryStmtString.append('=');
                queryStmtString.append(tableNames[i]);
                queryStmtString.append(firstColStr);
                queryStmtString.append(')');
            }
            
            if(tableNames.length >= 2 && filters.length >= 1)
            {
                queryStmtString.append(" AND ");
            }
            
            // filtering using the JDBC '?' notation for variable statement
            // parameters
            for(int i = 0; i < filters.length; i++)
            {
                if(i >= 1)
                {
                    queryStmtString.append(" AND ");
                }
                queryStmtString.append('(');
                
                String columnVal = filters[i].getColumn().getQualifiedName();
                
                // if it's text we always use upper so that we're case
                // insensitive
                if(filters[i].getColumn().getDataType() == DataType.TEXT)
                {
                    columnVal = "UPPER(" + columnVal + ')';
                }
                
                FilterCondition condition = filters[i].getCondition();
                if(condition == FilterCondition.EXACTLY_MATCHES_ANY ||
                   condition == FilterCondition.PARTIALLY_MATCHES_ANY)
                {
                    // chain the list of conditions together with an OR
                    StringTokenizer st = new StringTokenizer(filters[i].getValue());
                    int tokenCount = st.countTokens();
                    for(int j = 0; j < tokenCount; j++)
                    {
                        if(j >= 1)
                        {
                            queryStmtString.append(" OR ");
                        }
                        queryStmtString.append(columnVal);
                        queryStmtString.append(this.filterConditionsToSQLOpString(condition));
                        queryStmtString.append('?');
                    }
                }
                else
                {
                    queryStmtString.append(columnVal);
                    queryStmtString.append(this.filterConditionsToSQLOpString(condition));
                    queryStmtString.append('?');
                }
                
                queryStmtString.append(')');
            }
        }
        
        // ordering ...
        QualifiedColumnMetadata orderBy =
            normalizedQuery.getQuery().getOrderByColumn();
        if(orderBy != null)
        {
            queryStmtString.append(" ORDER BY ");
            queryStmtString.append(orderBy.getQualifiedName());
            
            SortDirection sortDirection =
                normalizedQuery.getQuery().getSortDirection();
            if(sortDirection != null)
            {
                switch(sortDirection)
                {
                    case ASCENDING: queryStmtString.append(" ASC");
                    break;
                    
                    case DESCENDING: queryStmtString.append(" DESC");
                    break;
                }
            }
        }
        
        return queryStmtString.toString();
    }
    
    /**
     * Take a condition and turn it into an SQL operator string
     * @param condition
     *          the filter condition
     * @return
     *          the string for the SQL operator
     */
    private String filterConditionsToSQLOpString(FilterCondition condition)
    {
        switch(condition)
        {
            case EQUAL_TO:              return " = ";
            case GREATER_THAN:          return " > ";
            case LESS_THAN:             return " < ";
            case EXACTLY_MATCHES_ANY:   return " = ";
            case PARTIALLY_MATCHES_ANY: return " LIKE ";
            
            default: throw new IllegalArgumentException(
                    "unexpected condition type " + condition.name());
        }
    }
    
    /**
     * Getter for the design table metadata
     * @param connection
     *          the DB connection to use
     * @return
     *          the metadata for the design table
     * @throws SQLException
     *          if JDBC doesn't like us
     */
    public TableColumnMetadata[] getDesignTableColumnMetadata(Connection connection)
    throws SQLException
    {
        TableColumnMetadata[] allColsMetadata = this.getColumnMetadata(
                connection,
                DESIGN_TABLE_NAME + COL_METADATA_TABLE_SUFFIX);
        
        // hide the 1st column from the user. the first column is used to map
        // the design row to the data column which the caller shouldn't care
        // about
        TableColumnMetadata[] allBut1stColMetadata =
            new TableColumnMetadata[allColsMetadata.length - 1];
        for(int i = 0; i < allBut1stColMetadata.length; i++)
        {
            allBut1stColMetadata[i] = allColsMetadata[i + 1];
        }
        return allBut1stColMetadata;
    }

    /**
     * Get the table data for the design
     * @param connection
     *          the connection to use
     * @return
     *          the design data
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public TableData<Object> getDesignData(Connection connection) throws SQLException
    {
        String selectStmtString =
            "SELECT * FROM " + DESIGN_TABLE_NAME + " ORDER BY " + COL_NAME_PREFIX + 0;
        LOG.info("Selecting all design data using: " + selectStmtString);
        
        Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        ResultSet results = statement.executeQuery(
                selectStmtString);
        
        // extracts all of the data except the 1st column which is only used
        // to map design rows to their respective data columns which is
        // something that the calller shouldn't care about
        Object[][] data = this.queryResultsToObjectArrays(
                results,
                1,
                0,
                Integer.MAX_VALUE);
        
        results.close();
        statement.close();
        
        return new TableData<Object>(
                data,
                0,
                data.length);
    }
    
    /**
     * Get the given design data column (null means that there is no column)
     * @param connection
     *          the connection to use
     * @param columnMetadata
     *          the column metadata
     * @return
     *          the list of values or null if there are none
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public List<Comparable> getDesignDataColumn(
            Connection connection,
            TableColumnMetadata columnMetadata)
            throws SQLException
    {
        int index = TableColumnMetadata.getIndexOfColumnNamed(
                columnMetadata.getName(),
                this.getDesignTableColumnMetadata(connection));
        if(index == -1)
        {
            return null;
        }
        else
        {
            // using index + 1 here because getDesignTableColumnMetadata(...)
            // strips out the 1st column from its results
            String columnSelectStatementString =
                "SELECT " + COL_NAME_PREFIX + (index + 1) + " FROM " +
                DESIGN_TABLE_NAME;
            
            Statement selectStmt = connection.createStatement();
            ResultSet results =
                selectStmt.executeQuery(columnSelectStatementString);
            
            List<Comparable> data = new ArrayList<Comparable>();
            while(results.next())
            {
                data.add((Comparable)results.getObject(1));
            }
            
            results.close();
            selectStmt.close();
            
            return data;
        }
    }
    
    /**
     * Get metadata for all of the annotation tables
     * @param connection
     *          the connection to use
     * @param includeIdColumn
     *          should we include the ID column
     * @return
     *          the metadata list
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public List<TableMetadata> getAnnotationTableMetadata(
            Connection connection,
            boolean includeIdColumn)
    throws SQLException
    {
        return this.getAnnotationTableMetadata(
                connection,
                null,
                includeIdColumn);
    }
    
    /**
     * Get metadata for all of the annotation tables
     * @param connection
     *          the connection to use
     * @param category
     *          the category to get (null means all)
     * @param includeIdColumn
     *          should we include the ID column
     * @return
     *          the metadata list
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public List<TableMetadata> getAnnotationTableMetadata(
            Connection connection,
            String category,
            boolean includeIdColumn)
    throws SQLException
    {
        String queryString = "SELECT * FROM " + TABLE_META_TABLE;
        if(category != null)
        {
            // tack on a where clause for the category string
            queryString +=
                " WHERE " + LOGICAL_TABLE_CATEGORY_COL + " = ?";
        }
        
        PreparedStatement tblMetaTblSelectStmt = connection.prepareStatement(
                queryString);
        if(category != null)
        {
            tblMetaTblSelectStmt.setString(1, category);
        }
        
        ResultSet tblMetaTblResults = tblMetaTblSelectStmt.executeQuery();
        
        List<TableMetadata> tableMetadataList = new ArrayList<TableMetadata>();
        while(tblMetaTblResults.next())
        {
            TableMetadata currTblMeta = new TableMetadata();
            currTblMeta.setTableId(tblMetaTblResults.getString(SQL_TABLE_NAME_COL));
            currTblMeta.setTableName(tblMetaTblResults.getString(LOGICAL_TABLE_NAME_COL));
            tableMetadataList.add(currTblMeta);
        }
        tblMetaTblResults.close();
        tblMetaTblSelectStmt.close();
        
        for(TableMetadata currMeta: tableMetadataList)
        {
            TableColumnMetadata[] colMeta = this.getColumnMetadata(
                    connection,
                    currMeta.getTableId() + COL_METADATA_TABLE_SUFFIX);
            
            if(!includeIdColumn)
            {
                TableColumnMetadata[] colMetaWithoutId =
                    new TableColumnMetadata[colMeta.length - 1];
                for(int i = 0; i < colMetaWithoutId.length; i++)
                {
                    colMetaWithoutId[i] = colMeta[i + 1];
                }
                colMeta = colMetaWithoutId;
            }
            
            currMeta.setColumnMetadata(colMeta);
        }
        
        return tableMetadataList;
    }
    
    /**
     * Get a list containing all of the available table categories
     * @param connection
     *          the SQL connection to use
     * @return
     *          the list of category names
     * @throws SQLException
     *          if JDBC doesn't like what we're doing
     */
    public List<String> getAnnotationTableCategories(Connection connection)
    throws SQLException
    {
        String queryString =
            "SELECT DISTINCT " + LOGICAL_TABLE_CATEGORY_COL + " FROM " +
            TABLE_META_TABLE;
        Statement queryStatement = connection.createStatement();
        ResultSet results = queryStatement.executeQuery(queryString);
        
        List<String> categories = new ArrayList<String>();
        while(results.next())
        {
            categories.add(results.getString(1));
        }
        
        results.close();
        queryStatement.close();
        
        return categories;
    }

    /**
     * Gets the data row for the given probe. This includes the probe ID
     * plus all of the probe values
     * @param connection
     *          the connection
     * @param probeId
     *          the probe ID
     * @return
     *          the data
     * @throws SQLException
     *          if JDBC doesn't like what we're trying to do
     */
    public double[] getDataRowForProbeID(
            Connection connection,
            String probeId)
    throws SQLException
    {
        // build the query
        String probeSelectStatementString =
            "SELECT * from " + DATA_TABLE_NAME + " WHERE " +
            COL_NAME_PREFIX + 0 + " = ?";
        PreparedStatement probeSelectStatement =
            connection.prepareStatement(probeSelectStatementString);
        probeSelectStatement.setString(1, probeId);
        
        // run the query
        ResultSet results = probeSelectStatement.executeQuery();
        try
        {
            if(results.next())
            {
                int columnCount = results.getMetaData().getColumnCount();
                double[] dataRow = new double[columnCount - 1];
                
                // there are two reasons for the weird indexing
                // 1) JDBC uses 1-based indexing
                // 2) we're skipping over the primary key
                for(int sqlCol = 2; sqlCol <= columnCount; sqlCol++)
                {
                    dataRow[sqlCol - 2] = results.getDouble(sqlCol);
                }
                
                return dataRow;
            }
            else
            {
                // could not find the given probe
                return null;
            }
        }
        finally
        {
            probeSelectStatement.close();
            results.close();
        }
    }

    /**
     * Getter for the gene image metadata for the given gene ID
     * @param connection
     *          the connection
     * @param geneId
     *          the gene ID
     * @return
     *          the matadata
     * @throws SQLException
     *          if JDBC doesn't like what we're trying to do
     */
    public GeneImageMetadata[] getGeneImageMetadata(
            Connection connection,
            String geneId) throws SQLException
    {
        // build the query
        String selectStatementString =
            "SELECT " +
            PER_GENE_IMAGE_TABLE_NAME + ".*, " +
            PER_GENE_IMAGE_CATEGORY_TABLE_NAME + "." + PER_GENE_CATEGORY_NAME_COL_NAME +
            
            " FROM " +
            PER_GENE_IMAGE_TABLE_NAME + ", " +
            PER_GENE_IMAGE_CATEGORY_TABLE_NAME +
            
            " WHERE " +
            PER_GENE_IMAGE_TABLE_NAME + "." + PER_GENE_CATEGORY_ID_COL_NAME + " = " +
            PER_GENE_IMAGE_CATEGORY_TABLE_NAME + "." + PER_GENE_CATEGORY_ID_COL_NAME + " AND " +
            GENE_IMAGE_ID_COL_NAME + " = ?" +
            
            " ORDER BY " +
            GENE_IMAGE_ID_COL_NAME + ", " +  PER_GENE_CATEGORY_ID_COL_NAME;
        
        LOG.info(selectStatementString);
        LOG.info("Gene ID: " + geneId);
        
        PreparedStatement selectStatement =
            connection.prepareStatement(selectStatementString);
        selectStatement.setString(1, geneId);
        
        // run the query
        ResultSet results = selectStatement.executeQuery();
        try
        {
            List<GeneImageMetadata> metadata = new ArrayList<GeneImageMetadata>();
            while(results.next())
            {
                String extension = results.getString(GENE_IMAGE_EXTENSION_COL_NAME);
                int catId = results.getInt(PER_GENE_CATEGORY_ID_COL_NAME);
                String catName = results.getString(PER_GENE_CATEGORY_NAME_COL_NAME);
                
                String imgPath =
                    PerGeneImageDirectoryDescription.PER_PROBE_IMAGE_PREFIX +
                    catId + "/" + geneId + "." + extension;
                
                metadata.add(new GeneImageMetadata(geneId, catName, imgPath));
            }
            
            return metadata.toArray(new GeneImageMetadata[metadata.size()]);
        }
        finally
        {
            selectStatement.close();
            results.close();
        }
    }
}

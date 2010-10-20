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

package org.jax.pubarray.gwtcommon.client;

import java.io.Serializable;

/**
 * Data describing a column in a table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TableColumnMetadata implements Serializable
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7151236098449666678L;
    
    /**
     * Describes the type of data that a particular column holds
     */
    public enum DataType
    {
        /**
         * Data will be string objects
         */
        TEXT
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Text";
            }
        },
        
        /**
         * Data will be {@link Double}s
         */
        REAL
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Real Number";
            }
        },
        
        /**
         * Data will be {@link Integer}s
         */
        INTEGER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Integer";
            }
        }
    }
    
    /**
     * Class used to hold evidence used for inferring a column's type.
     * @see TableColumnMetadata#getTypeInferenceEvidence()
     */
    public static class TypeEvidence implements Serializable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -1844736203113625024L;
        
        private String cellContents;
        
        private int rowNumber;
        
        /**
         * Constructor
         */
        public TypeEvidence()
        {
        }

        /**
         * Constructor
         * @param cellContents
         *          see {@link #getCellContents()}
         * @param rowNumber
         *          see {@link #getRowNumber()}
         */
        public TypeEvidence(String cellContents, int rowNumber)
        {
            this.cellContents = cellContents;
            this.rowNumber = rowNumber;
        }
        
        /**
         * Getter for the string that was found in the table cell
         * @return the cell contents for this evidence
         */
        public String getCellContents()
        {
            return this.cellContents;
        }
        
        /**
         * Setter for the cell contents
         * @param cellContents the cellContents to set
         */
        public void setCellContents(String cellContents)
        {
            this.cellContents = cellContents;
        }
        
        /**
         * Getter for the row number where the evidence was found
         * @return the row number
         */
        public int getRowNumber()
        {
            return this.rowNumber;
        }
        
        /**
         * Setter for the row number
         * @param rowNumber
         *          the row number to set
         */
        public void setRowNumber(int rowNumber)
        {
            this.rowNumber = rowNumber;
        }
    }
    
    /**
     * The property string for name
     */
    public static final String NAME_PROP_STRING = "tableColumnName";
    private String name;
    
    /**
     * The property string for data type
     */
    public static final String DATA_TYPE_PROP_STRING = "dataType";
    private DataType dataType;

    /**
     * the property string for the description
     */
    public static final String DESCRIPTION_PROP_STRING = "description";
    private String description;
    
    /**
     * the property string for the inference evidence
     */
    public static final String TYPE_EVIDENCE_PROP_STRING = "typeInferenceEvidence";
    private TypeEvidence typeInferenceEvidence = null;
    
    /**
     * the property string for the longest string length
     */
    public static final String LONGEST_STRING_LEN_PROP_STRING = "longestStringLength";
    private int longestStringLength = -1;
    
    /**
     * Default constructor. Sets name datatype and description to null
     */
    public TableColumnMetadata()
    {
    }
    
    /**
     * Constructor, Sets description and datatype to null
     * @param name
     *          see {@link #getName()}
     */
    public TableColumnMetadata(String name)
    {
        this.setName(name);
    }
    
    /**
     * Constructor
     * @param name
     *          see {@link #getName()}
     * @param dataType
     *          see {@link #getDataType()}
     * @param description 
     *          see {@link #getDescription()}
     */
    public TableColumnMetadata(
            String name,
            DataType dataType,
            String description)
    {
        this.setName(name);
        this.setDataType(dataType);
        this.setDescription(description);
    }
    
    /**
     * Constructor
     * @param name
     *          see {@link #getName()}
     * @param dataType
     *          see {@link #getDataType()}
     * @param description 
     *          see {@link #getDescription()}
     * @param typeInferenceEvidence
     *          see {@link #getTypeInferenceEvidence()}
     * @param longestStringLength 
     *          see {@link #getLongestStringLength()}
     */
    public TableColumnMetadata(
            String name,
            DataType dataType,
            String description,
            TypeEvidence typeInferenceEvidence,
            int longestStringLength)
    {
        this.setName(name);
        this.setDataType(dataType);
        this.setDescription(description);
        this.setTypeInferenceEvidence(typeInferenceEvidence);
        this.setLongestStringLength(longestStringLength);
    }
    
    /**
     * Getter for the name of this column
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Setter for the name of this column
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Getter for the description of this column
     * @return
     *          the description of this column
     */
    public String getDescription()
    {
        return this.description;
    }
    
    /**
     * Setter for the description of this column
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * Getter for the type of data that this column holds
     * @return the type of data
     */
    public DataType getDataType()
    {
        return this.dataType;
    }
    
    /**
     * Setter for the type of data that this column holds
     * @param dataType
     *          the type of data this column holds
     */
    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }
    
    /**
     * Getter for the length of the longest string
     * @return the length
     */
    public int getLongestStringLength()
    {
        return this.longestStringLength;
    }
    
    /**
     * Setter for the length of the longest string
     * @param longestStringLength
     *          the length of the longest string
     */
    public void setLongestStringLength(int longestStringLength)
    {
        this.longestStringLength = longestStringLength;
    }
    
    /**
     * Getter for the type inference evidence. This is only used if the
     * {@link DataType} of this column was inferred using an inference
     * algorithm. If that is the case it should contain an example of the
     * most specific evidence of the given inferred type. For instance if
     * {@link DataType#REAL} is inferred this might be "0.033" or if
     * {@link DataType#TEXT} it might be "hello world!"
     * @return
     *          the evidence or null if there is no relevant evidence
     */
    public TypeEvidence getTypeInferenceEvidence()
    {
        return this.typeInferenceEvidence;
    }
    
    /**
     * Setter for the type inference evidence
     * @see #getTypeInferenceEvidence()
     * @param typeInferenceEvidence
     *          the evidence
     */
    public void setTypeInferenceEvidence(TypeEvidence typeInferenceEvidence)
    {
        this.typeInferenceEvidence = typeInferenceEvidence;
    }
    
    /**
     * Returns true iff the given string could be parsed as a real (ie double)
     * value. Used for type inference and error checking.
     * @see #getTypeInferenceEvidence()
     * @param cellContents
     *          the string to test
     * @return
     *          true iff it's parsable
     */
    public static boolean isParsableAsReal(String cellContents)
    {
        // it's kind of lame to use a try-catch for this but it's the most
        // robust way I could find
        try
        {
            Double.parseDouble(cellContents.trim());
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
    
    /**
     * Returns true iff the given string could be parsed as an integer
     * value. Used for type inference and error checking.
     * @see #getTypeInferenceEvidence()
     * @param cellContents
     *          the string to test
     * @return
     *          true iff it's parsable
     */
    public static boolean isParsableAsInteger(String cellContents)
    {
        // it's kind of lame to use a try-catch for this but it's the most
        // robust way I could find
        try
        {
            Integer.parseInt(cellContents.trim());
            return true;
        }
        catch(NumberFormatException ex)
        {
            return false;
        }
    }
    
    /**
     * Get the column with the given name
     * @param name
     *          the name we're looking for
     * @param metadata
     *          the column metadata to search through
     * @return
     *          the 1st column with a name that matches the given name
     *          or null if we can't find one
     */
    public static TableColumnMetadata getColumnNamed(String name, TableColumnMetadata[] metadata)
    {
        int index = getIndexOfColumnNamed(name, metadata);
        
        if(index >= 0)
        {
            return metadata[index];
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Returns the index of the column with the given name or -1 if it can't
     * be found
     * @param name
     *          the name we're looking for
     * @param metadata
     *          the metadata we're searching through
     * @return
     *          the index
     */
    public static int getIndexOfColumnNamed(String name, TableColumnMetadata[] metadata)
    {
        for(int i = 0; i < metadata.length; i++)
        {
            if(metadata[i].getName().equals(name))
            {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Col Metadata[name: ");
        sb.append(this.name);
        sb.append(", dataType: ");
        sb.append(this.dataType);
        sb.append(", description: ");
        sb.append(this.description);
        sb.append(", longestStringLength: ");
        sb.append(this.longestStringLength);
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof TableColumnMetadata)
        {
            TableColumnMetadata colMeta = (TableColumnMetadata)obj;
            return this.name == colMeta.name ||
                   this.name.equals(colMeta.name);
        }
        else
        {
            return false;
        }
    }
}

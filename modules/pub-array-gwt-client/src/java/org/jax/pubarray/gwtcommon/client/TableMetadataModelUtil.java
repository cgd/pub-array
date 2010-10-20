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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Utility class for converting between {@link TableData} and the Ext GWT
 * {@link ModelData} type.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TableMetadataModelUtil
{
    /**
     * Convert the given data to a list of model objects
     * @param minimal
     *          if true then we only copy name and type over ignoring the
     *          other properties
     * @param metadata
     *          the table data to convert
     * @return
     *          the models
     */
    public static List<ModelData> tableMetadataArrayToModels(boolean minimal, TableMetadata[] metadata)
    {
        List<ModelData> models = new ArrayList<ModelData>();
        for(TableMetadata currTableMeta: metadata)
        {
            models.addAll(tableMetadataToModels(minimal, currTableMeta));
        }
        return models;
    }
    
    /**
     * Convert the given metadata into a list of model objects
     * @param minimal
     *          if true then we only copy name and type over ignoring the
     *          other properties
     * @param metadata
     *          the metadata
     * @return
     *          the models
     */
    public static List<ModelData> tableMetadataToModels(boolean minimal, TableMetadata metadata)
    {
        TableColumnMetadata[] colMetadata = metadata.getColumnMetadata();
        List<ModelData> models = new ArrayList<ModelData>(colMetadata.length);
        for(TableColumnMetadata currColMeta: colMetadata)
        {
            // get the column model ...
            ModelData model = TableColumnMetadataModelUtil.fromPojoToModel(
                    minimal,
                    currColMeta);
            
            // ... and add the table name and ID to it
            model.set(TableMetadata.TABLE_ID_PROP_STRING, metadata.getTableId());
            model.set(TableMetadata.TABLE_NAME_PROP_STRING, metadata.getTableName());
            models.add(model);
        }
        return models;
    }
}

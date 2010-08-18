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

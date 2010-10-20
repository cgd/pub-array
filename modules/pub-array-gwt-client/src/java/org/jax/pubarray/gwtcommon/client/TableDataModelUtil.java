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


import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.Model;

/**
 * Utility class for converting between {@link TableData} and the Ext GWT
 * {@link Model} type.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TableDataModelUtil
{
    /**
     * Convert the given data to a list of model objects
     * @param <D>
     *          the type of data in the given table
     * @param columnMetadata
     *          the metadata for the columns
     * @param data
     *          the table data to convert
     * @return
     *          the models
     */
    public static <D> List<Model> tableDataToModels(
            QualifiedColumnMetadata[] columnMetadata,
            TableData<D> data)
    {
        List<Model> models = new ArrayList<Model>(data.getData().length);
        for(Object[] row: data.getData())
        {
            Model model = new BaseModel();
            model.set(QueryModelUtil.SELECT_COLUMN_ID, Boolean.FALSE);
            for(int i = 0; i < row.length; i++)
            {
                // TODO see QueryResultsContainer.onRender(...) for detailed
                // comment on this replace(...)
                model.set(
                        columnMetadata[i].getQualifiedName().replace('.', '+'),
                        row[i]);
            }
            models.add(model);
        }
        return models;
    }
    
    /**
     * Extract a probe ID from the given model
     * @param columnMetadata
     *          the metadata that the model is based on
     * @param model
     *          the model to extract a probe ID from
     * @return
     *          the probe ID
     */
    public static String extractProbeIdFromModel(
            QualifiedColumnMetadata[] columnMetadata,
            Model model)
    {
        // By convention the first column is the ID column
        return model.get(columnMetadata[0].getQualifiedName().replace('.', '+'));
    }
}

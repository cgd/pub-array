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

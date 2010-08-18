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
import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A helper class for converting between {@link QualifiedColumnMetadata} and
 * {@link ModelData}s
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QualifiedColumnMetadataModelUtil
{
    /**
     * Convert a {@link TableColumnMetadata} POJO into a model object
     * @param minimal
     *          if true then we only copy name and type over ignoring the
     *          other properties
     * @param pojo
     *          the object to convert
     * @return
     *          the result of the conversion
     */
    public static ModelData fromPojoToModel(
            boolean minimal,
            QualifiedColumnMetadata pojo)
    {
        ModelData model = new BaseModel();
        
        TableColumnMetadataModelUtil.fromPojoToModel(minimal, pojo, model);
        model.set(TableMetadata.TABLE_NAME_PROP_STRING, pojo.getTableName());
        
        return model;
    }
    
    /**
     * Convert the given model into a {@link QualifiedColumnMetadata} object
     * @param model the model
     * @return      the object
     */
    public static QualifiedColumnMetadata fromModelToPojo(ModelData model)
    {
        if(model == null)
        {
            return null;
        }
        else
        {
            QualifiedColumnMetadata pojo = new QualifiedColumnMetadata();
            
            TableColumnMetadataModelUtil.fromModelToPojo(model, pojo);
            pojo.setTableName(
                    (String)model.get(TableMetadata.TABLE_NAME_PROP_STRING));
            
            return pojo;
        }
    }
    
    /**
     * A convenience function to convert a list of models using
     * {@link #fromModelToPojo(ModelData)}
     * @param models
     *          the input to convert
     * @return
     *          the result
     */
    public static List<QualifiedColumnMetadata> fromModelsToPojos(List<ModelData> models)
    {
        List<QualifiedColumnMetadata> pojos =
            new ArrayList<QualifiedColumnMetadata>(models.size());
        for(ModelData model: models)
        {
            pojos.add(fromModelToPojo(model));
        }
        return pojos;
    }
}

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

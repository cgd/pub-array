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

import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.DataType;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.TypeEvidence;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A class for converting {@link TableColumnMetadata} to and from GXT
 * model types
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class TableColumnMetadataModelUtil
{
    /**
     * private constructor. use static function
     */
    private TableColumnMetadataModelUtil()
    {
    }
    
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
    public static ModelData fromPojoToModel(boolean minimal, TableColumnMetadata pojo)
    {
        if(pojo == null)
        {
            return null;
        }
        else
        {
            ModelData model = new BaseModel();
            
            fromPojoToModel(minimal, pojo, model);
            
            return model;
        }
    }
    
    /**
     * Convert a {@link TableColumnMetadata} POJO into a model object
     * @param minimal
     *          if true then we only copy name and type over ignoring the
     *          other properties
     * @param pojo
     *          the object to convert
     * @param model
     *          the model to update
     */
    public static void fromPojoToModel(boolean minimal, TableColumnMetadata pojo, ModelData model)
    {
        model.set(
                TableColumnMetadata.NAME_PROP_STRING,
                pojo.getName());
        model.set(
                TableColumnMetadata.DATA_TYPE_PROP_STRING,
                pojo.getDataType());
        
        if(!minimal)
        {
            model.set(
                    TableColumnMetadata.DESCRIPTION_PROP_STRING,
                    pojo.getDescription());
            model.set(
                    TableColumnMetadata.TYPE_EVIDENCE_PROP_STRING,
                    pojo.getTypeInferenceEvidence());
            model.set(
                    TableColumnMetadata.LONGEST_STRING_LEN_PROP_STRING,
                    pojo.getLongestStringLength());
        }
    }
    
    /**
     * Convert the given model into a POJO
     * @param model
     *          the model to convert
     * @return
     *          the plain java object
     */
    public static TableColumnMetadata fromModelToPojo(ModelData model)
    {
        if(model == null)
        {
            return null;
        }
        else
        {
            TableColumnMetadata pojo = new TableColumnMetadata();
            
            fromModelToPojo(model, pojo);
            
            return pojo;
        }
    }
    
    /**
     * Convert the given model into a POJO
     * @param model
     *          the model to convert
     * @param pojo
     *          the plain java object to update
     */
    public static void fromModelToPojo(ModelData model, TableColumnMetadata pojo)
    {
        pojo.setName(
                (String)model.get(TableColumnMetadata.NAME_PROP_STRING));
        pojo.setDataType(
                (DataType)model.get(TableColumnMetadata.DATA_TYPE_PROP_STRING));
        pojo.setDescription(
                (String)model.get(TableColumnMetadata.DESCRIPTION_PROP_STRING));
        pojo.setTypeInferenceEvidence(
                (TypeEvidence)model.get(TableColumnMetadata.TYPE_EVIDENCE_PROP_STRING));
        Integer longestStringLength =
            model.get(TableColumnMetadata.LONGEST_STRING_LEN_PROP_STRING);
        if(longestStringLength != null)
        {
            pojo.setLongestStringLength(longestStringLength.intValue());
        }
    }
    
    /**
     * A convenience function to convert a list of {@link TableColumnMetadata}
     * POJOs using {@link #fromPojoToModel(boolean, TableColumnMetadata)}
     * @param minimal
     *          if true then we only copy name and type over ignoring the
     *          other properties
     * @param pojos
     *          the input to convert
     * @return
     *          the models
     */
    public static List<ModelData> fromPojosToModels(boolean minimal, List<TableColumnMetadata> pojos)
    {
        List<ModelData> models = new ArrayList<ModelData>(pojos.size());
        for(TableColumnMetadata pojo: pojos)
        {
            models.add(fromPojoToModel(minimal, pojo));
        }
        return models;
    }
    
    /**
     * A convenience function to convert a list of models using
     * {@link #fromModelToPojo(ModelData)}
     * @param models
     *          the input to convert
     * @return
     *          the result
     */
    public static List<TableColumnMetadata> fromModelsToPojos(List<ModelData> models)
    {
        List<TableColumnMetadata> pojos = new ArrayList<TableColumnMetadata>(
                models.size());
        for(ModelData model: models)
        {
            pojos.add(fromModelToPojo(model));
        }
        return pojos;
    }
}

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
import java.util.Arrays;
import java.util.List;

import org.jax.pubarray.gwtcommon.client.Filter.FilterCondition;
import org.jax.pubarray.gwtcommon.client.TableColumnMetadata.DataType;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A helper class for bridging between the filter type and model class
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FilterModelUtil
{
    /**
     * The model key used for the filter condition
     */
    public static final String FILTER_CONDITION_MODEL_KEY = "FILTER_CONDITION";
    
    /**
     * Get a list of valid filter conditions that can be used for the given
     * data type
     * @param type
     *          the type
     * @return
     *          the valid filter conditions for the given type
     */
    public static List<FilterCondition> getValidFilterConditionsForType(DataType type)
    {
        if(type == DataType.INTEGER || type == DataType.REAL)
        {
            // for numeric data only some of the conditions are valid
            final List<FilterCondition> numericFilterConditions =
                new ArrayList<FilterCondition>();
            
            numericFilterConditions.add(FilterCondition.EQUAL_TO);
            numericFilterConditions.add(FilterCondition.LESS_THAN);
            numericFilterConditions.add(FilterCondition.GREATER_THAN);
            numericFilterConditions.add(FilterCondition.EXACTLY_MATCHES_ANY);
            
            return numericFilterConditions;
        }
        else if(type == DataType.TEXT || type == null)
        {
            // For text all of the conditions are valid
            return Arrays.asList(FilterCondition.values());
        }
        else
        {
            throw new IllegalArgumentException(
                    "Don't know anything about type: " + type);
        }
    }
    
    /**
     * Just like {@link #getValidFilterConditionsForType(DataType)} except that
     * this function wraps the conditions in a model
     * @param type
     *          the type to get valid condition models for
     * @return
     *          the models
     */
    public static List<ModelData> getValidFilterConditionModelsForType(DataType type)
    {
        List<FilterCondition> conditions = getValidFilterConditionsForType(type);
        List<ModelData> models = new ArrayList<ModelData>(conditions.size());
        
        for(FilterCondition currCondition: conditions)
        {
            ModelData currModel = new BaseModel();
            currModel.set(FILTER_CONDITION_MODEL_KEY, currCondition);
            models.add(currModel);
        }
        
        return models;
    }
}

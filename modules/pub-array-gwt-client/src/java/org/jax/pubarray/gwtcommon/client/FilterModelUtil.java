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

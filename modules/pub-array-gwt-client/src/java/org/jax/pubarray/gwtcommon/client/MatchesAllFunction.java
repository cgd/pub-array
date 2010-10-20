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
import java.util.Collection;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A filter for matching against all terms
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <M> the type of model that we can filter
 */
public class MatchesAllFunction<M extends ModelData> implements ModelBooleanFunction<M>
{
    private String[] tokensToMatch = new String[0];
    
    /**
     * Constructor
     */
    public MatchesAllFunction()
    {
    }
    
    /**
     * Update the space delimited text which is used for filtering
     * @param text
     *          the text
     */
    public void updateDelimitedText(String text)
    {
        if(text == null)
        {
            this.tokensToMatch = new String[0];
        }
        else
        {
            this.tokensToMatch = text.split("\\s");
            for(int i = 0; i < this.tokensToMatch.length; i++)
            {
                // upper case because we're case insensitive
                this.tokensToMatch[i] = this.tokensToMatch[i].toUpperCase();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean evaluateModel(M modelToTest)
    {
        if(this.tokensToMatch.length == 0)
        {
            return true;
        }
        
        Collection<Object> values = modelToTest.getProperties().values();
        List<String> valueStrings = new ArrayList<String>(values.size());
        for(Object value: values)
        {
            if(value != null)
            {
                String valueStr = value.toString();
                if(valueStr != null)
                {
                    // upper case because we're case insensitive
                    valueStr = valueStr.toUpperCase();
                    valueStrings.add(valueStr);
                }
            }
        }
        
        for(int i = 0; i < this.tokensToMatch.length; i++)
        {
            boolean foundTokenMatch = false;
            
            VAL_STR_LOOP:
            for(String valueStr: valueStrings)
            {
                String[] valueTokens = valueStr.split("\\s");
                for(String valueToken: valueTokens)
                {
                    if(valueToken.startsWith(this.tokensToMatch[i]))
                    {
                        foundTokenMatch = true;
                        break VAL_STR_LOOP;
                    }
                }
            }
            
            if(!foundTokenMatch)
            {
                return false;
            }
        }
        
        return true;
    }
}

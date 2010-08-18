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

import java.util.Collection;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * A function for matching against any term in the given space delimited text
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <M> the type of model that we can filter
 */
public class MatchesAnyFunction<M extends ModelData> implements ModelBooleanFunction<M>
{
    private String[] tokensToMatch = new String[0];
    
    /**
     * Constructor
     */
    public MatchesAnyFunction()
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
        for(Object value: values)
        {
            if(value != null)
            {
                String valueStr = value.toString();
                if(valueStr != null)
                {
                    // upper case because we're case insensitive
                    String[] valueTokens = valueStr.toUpperCase().split("\\s");
                    for(int i = 0; i < this.tokensToMatch.length; i++)
                    {
                        for(String valueToken: valueTokens)
                        {
                            if(valueToken.startsWith(this.tokensToMatch[i]))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
}

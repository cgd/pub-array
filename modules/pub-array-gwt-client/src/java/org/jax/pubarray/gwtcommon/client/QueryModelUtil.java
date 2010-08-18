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

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * General utility functions for using Model objects
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QueryModelUtil
{
    /**
     * The ID to use for order by direction (eg. ascending/descending)
     */
    public static final String ORDER_BY_DIRECTION_COLUMN_ID = "ORDER_BY_DIRECTION_ID";
    
    /**
     * the ID used for the select column
     */
    public static final String SELECT_COLUMN_ID = "SELECT_COLUMN_ID";
    
    /**
     * Static helper function to determine if the given model is selected
     * @see #SELECT_COLUMN_ID
     * @param model
     *          the model to check
     * @return
     *          true if it's selected
     */
    public static boolean isSelected(ModelData model)
    {
        Boolean selected = model.get(SELECT_COLUMN_ID);
        return Boolean.TRUE.equals(selected);
    }
}

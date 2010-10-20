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

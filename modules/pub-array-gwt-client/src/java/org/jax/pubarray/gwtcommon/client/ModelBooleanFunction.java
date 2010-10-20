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
 * Evaluate a boolean function against a given model
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <M>
 *          the type of Model data that this class works on
 */
public interface ModelBooleanFunction<M extends ModelData>
{
    /**
     * Test the given model against this expression
     * @param modelToTest
     *          the model to test
     * @return
     *          the result of the test
     */
    public boolean evaluateModel(M modelToTest);
}

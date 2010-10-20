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

package org.jax.pubarray.gwtqueryapp.client;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.DualListField;

/**
 * This class overrides {@link DualListField} to grow the list heights to match
 * the component height when {@link #onResize(int, int)} is called
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 * @param <D>
 *          the type of model data data that this dual list contains
 */
public class MyDualListField<D extends ModelData> extends DualListField<D>
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResize(int width, int height)
    {
        super.onResize(width, height);
        
        this.fromField.setHeight(height);
        this.toField.setHeight(height);
    }
}

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

import com.extjs.gxt.ui.client.widget.Dialog;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GxtUtil
{
    /**
     * Show a message dialog with the given heading and text
     * @param heading
     *          the dialog heading
     * @param text
     *          the dialog text
     */
    public static void showMessageDialog(
            String heading,
            String text)
    {
        Dialog messageDialog = new Dialog();
        messageDialog.setButtons(Dialog.OK);
        messageDialog.setHideOnButtonClick(true);
        messageDialog.setHeading(heading);
        messageDialog.addText(text);
        messageDialog.show();
    }
}

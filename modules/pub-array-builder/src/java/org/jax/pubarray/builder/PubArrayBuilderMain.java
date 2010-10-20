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

package org.jax.pubarray.builder;

import javax.swing.SwingUtilities;

import org.jax.util.gui.WizardFlipPanel;
import org.jax.util.gui.WizardFrame;

/**
 * This class is the main entry point for the pub array builder application
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PubArrayBuilderMain
{
    /**
     * The constructor
     */
    public PubArrayBuilderMain()
    {
    }

    /**
     * build a PubArray instance
     */
    private void buildPubArray()
    {
        final PubArrayWizardController wizardController =
            new PubArrayWizardController();
        final WizardFlipPanel wizardFlipPanel = new WizardFlipPanel(
                wizardController.getWizardPanels(),
                wizardController);
        final WizardFrame wizardFrame = new WizardFrame(
                wizardController,
                wizardFlipPanel);
        wizardController.setParentComponent(wizardFlipPanel);
        wizardFrame.setSize(800, 600);
        
        wizardFrame.setVisible(true);
    }
    
    /**
     * The main entry point for the PubArray builder
     * @param args
     *          command line arguments (don't care about these)
     */
    public static void main(String[] args)
    {
        final PubArrayBuilderMain builderMain = new PubArrayBuilderMain();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                builderMain.buildPubArray();
            }
        });
    }
}

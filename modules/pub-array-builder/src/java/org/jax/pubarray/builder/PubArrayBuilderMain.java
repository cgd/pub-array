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

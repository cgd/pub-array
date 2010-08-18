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

package org.jax.pubarray.installer;

import javax.swing.SwingUtilities;

import org.jax.util.gui.WizardFlipPanel;
import org.jax.util.gui.WizardFrame;

/**
 * This class is the main entry point for the pub array installer application
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PubArrayInstallerMain
{
    /**
     * The constructor
     */
    public PubArrayInstallerMain()
    {
    }

    /**
     * install PubArray
     */
    private void installPubArray()
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
     * The main entry point for the PubArray installer
     * @param args
     *          command line arguments (don't care about these)
     */
    public static void main(String[] args)
    {
        final PubArrayInstallerMain installerMain = new PubArrayInstallerMain();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                installerMain.installPubArray();
            }
        });
    }
}

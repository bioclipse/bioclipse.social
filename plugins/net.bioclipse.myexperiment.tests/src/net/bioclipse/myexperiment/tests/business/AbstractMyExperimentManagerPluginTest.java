/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.myexperiment.tests.business;

import java.util.List;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.myexperiment.business.IMyExperimentManager;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractMyExperimentManagerPluginTest {

    protected static IMyExperimentManager myexperiment;
    
    @Test public void testDownloadWorkflow() throws BioclipseException {
        String file = myexperiment.downloadWorkflow(927);
        Assert.assertNotNull(file);
        Assert.assertNotSame(0, file.length());
    }

    @Test public void testDownloadWorkflowAndSafe() throws BioclipseException {
        String file = myexperiment.downloadWorkflow(927, "/Virtual/workflow.test");
        Assert.assertNotNull(file);
        Assert.assertNotSame(0, file.length());
    }

    @Test public void testSearch() throws BioclipseException {
        List<Integer> bslScripts = myexperiment.search("opentox");
        Assert.assertNotNull(bslScripts);
        Assert.assertNotSame(0, bslScripts.size());
    }

    @Test public void testList() throws BioclipseException {
        List<Integer> bslScripts = myexperiment.list();
        Assert.assertNotNull(bslScripts);
        Assert.assertNotSame(0, bslScripts.size());
    }

}

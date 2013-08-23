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
package net.bioclipse.myexperiment.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.business.BioclipsePlatformManager;
import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.StringMatrix;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.business.RDFManager;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class MyExperimentManager implements IBioclipseManager {

    public final static String MYEXPERIMENT_PROJECT = "MyExperiment";
    public final static String SPARQL_ENDPOINT =
        "http://rdf.myexperiment.org/sparql";

    // workflow information
    enum WorkflowProperty {
        FILENAME,
        URL
    }

    private static final Logger logger =
        Logger.getLogger(MyExperimentManager.class);
    
    private BioclipsePlatformManager bioclipse =
        new BioclipsePlatformManager();
    private RDFManager rdf =
        new RDFManager();

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "myexperiment";
    }

    public IFile downloadWorkflow(Integer workflowNumber,
                                  IProgressMonitor monitor)
        throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask(
            "Downloading workflow from MyExperiment" + workflowNumber,
            100
        );

        if (workflowNumber == null)
            throw new BioclipseException("Input must not be null");

        IProject project;
        try {
            project = getProjectDirectory(monitor);
        } catch ( CoreException e ) {
            logger.error(
                "Error while opening MyExperiment target folder",
                e
            );
            throw new BioclipseException(
                "Could not find a project to save the BSL script in."
            );
        }
        Map<WorkflowProperty,String> info = getWorkflowInfo(
            workflowNumber, monitor
        );
        String path = project.getFullPath()
            .append(info.get(WorkflowProperty.FILENAME)).toString();
        IFile target = ResourcePathTransformer.getInstance().transform(path);

        monitor.subTask("Download workflow...");
        IFile result = bioclipse.downloadAsFile(
            info.get(WorkflowProperty.URL), target, monitor
        );
        monitor.worked(50);

        return result;
    }

    public IFile downloadWorkflow(Integer workflowNumber,
                                  IFile target,
                                  IProgressMonitor monitor)
    throws BioclipseException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask(
            "Downloading workflow from MyExperiment" + workflowNumber,
            100
        );

        if (workflowNumber == null)
            throw new BioclipseException("Input must not be null");

        Map<WorkflowProperty,String> info = getWorkflowInfo(
            workflowNumber, monitor
        );

        IFile result = bioclipse.downloadAsFile(
            info.get(WorkflowProperty.URL), target, monitor
        );

        return result;
    }

    // private helper methods

    private Map<WorkflowProperty,String>
        getWorkflowInfo(Integer workflowNumber, IProgressMonitor monitor)
    throws BioclipseException {
        monitor.subTask("Querying the RDF database...");
        
        Map<WorkflowProperty,String> info =
            new HashMap<WorkflowProperty, String>();

        String sparql =
            "PREFIX mebase: <http://rdf.myexperiment.org/ontologies/base/> " +
            "SELECT ?filename ?url WHERE {" +
            "  <http://www.myexperiment.org/workflows/" +
            workflowNumber + ">" +
            "    mebase:filename ?filename;" +
            "    mebase:content-url ?url." +
            "}";
        StringMatrix results = 
            rdf.sparqlRemote(SPARQL_ENDPOINT, sparql, monitor);
        if (results.getRowCount() == 0)
            throw new BioclipseException(
                "Workflow not found."
            );
        String filename = results.get(1, "filename");
        filename = removeType(filename);
        info.put(WorkflowProperty.FILENAME, filename);
        String url = results.get(1, "url");
        if (url.indexOf("^^") != -1)
            url = url.substring(0, url.indexOf("^^"));
        info.put(WorkflowProperty.URL, url);

        monitor.worked(50);
        return info;
    }
    
    private String removeType(String filename) {
        StringBuffer result = new StringBuffer();
        boolean typeSeparatorFound = false;
        for (int i=filename.length()-1; i>=0; i--) {
            char curChar = filename.charAt(i);
            if (typeSeparatorFound) result.append(curChar);
            if (curChar == '^' && filename.charAt(i-1) == '^') {
                i--; // skip second ^ too
                typeSeparatorFound = true;
            }
        }
        
        if (!typeSeparatorFound)
            return filename;
        else
            return result.reverse().toString();
    }

    private IProject getProjectDirectory(IProgressMonitor monitor)
    throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(MYEXPERIMENT_PROJECT);
        if (!project.exists()) project.create(monitor);
        if (!project.isOpen()) project.open(monitor);

        return project;
    }

    public List<Integer> search(String query, IProgressMonitor monitor)
        throws BioclipseException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        List<Integer> workflows = new ArrayList<Integer>();
        monitor.beginTask("Searching MyExperiment for BSL scripts with '" +
                          query + "'...", 1);

        String sparql =
            "PREFIX mecontrib: <http://rdf.myexperiment.org/ontologies/" +
                               "contributions/>" +
            "PREFIX mebase: <http://rdf.myexperiment.org/ontologies/" +
                               "base/>" +
            "PREFIX dcterms: <http://purl.org/dc/terms/>" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "SELECT ?workflow WHERE {" +
            "  ?workflow mebase:has-content-type ?type ." +
            "  ?workflow rdf:type                mecontrib:Workflow ." +
            "  ?workflow dcterms:title           ?title ." +
            "  ?type     rdf:type                mebase:ContentType ." +
            "  ?type     dcterms:title           ?typetitle ." +
            "  FILTER regex(?title, \"" + query + "\", \"i\") ." +
            "  FILTER regex(?typetitle, \"Bioclipse\") ." +
            "}";
        StringMatrix results =
            rdf.sparqlRemote(SPARQL_ENDPOINT, sparql, monitor);
        if (results.getRowCount() > 0) {
        	for (String workflow : results.getColumn("workflow")) {
        		int number = Integer.valueOf(workflow.substring(
        				workflow.lastIndexOf('/')+1
        		));
        		workflows.add(number);
        	}
        }

        return workflows;
    }

    public List<Integer> list(IProgressMonitor monitor)
    throws BioclipseException {
    if (monitor == null) {
        monitor = new NullProgressMonitor();
    }

    List<Integer> workflows = new ArrayList<Integer>();
    monitor.beginTask("Retrieving all BSL workflows...", 1);

    String sparql =
        "PREFIX mecontrib: <http://rdf.myexperiment.org/ontologies/" +
                           "contributions/>" +
        "PREFIX mebase: <http://rdf.myexperiment.org/ontologies/" +
                           "base/>" +
        "PREFIX dcterms: <http://purl.org/dc/terms/>" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        "SELECT ?workflow WHERE {" +
        "  ?workflow mebase:has-content-type ?type ." +
        "  ?workflow rdf:type                mecontrib:Workflow ." +
        "  ?type     rdf:type                mebase:ContentType ." +
        "  ?type     dcterms:title           ?typetitle ." +
        "  FILTER regex(?typetitle, \"Bioclipse\") ." +
        "}";
    StringMatrix results =
        rdf.sparqlRemote(SPARQL_ENDPOINT, sparql, monitor);
    if (results.getRowCount() > 0) {
    	for (String workflow : results.getColumn("workflow")) {
    		int number = Integer.valueOf(workflow.substring(
    				workflow.lastIndexOf('/')+1
    		));
    		workflows.add(number);
    	}
    }

    return workflows;
}
}

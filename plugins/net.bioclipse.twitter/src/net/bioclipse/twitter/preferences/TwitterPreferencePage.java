/*******************************************************************************
 * Copyright (c) 2009  Egon Willighagen <egonw@user.sf.net>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.twitter.preferences;

import net.bioclipse.twitter.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for ones Twitter account.
 */
public class TwitterPreferencePage extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

    private StringFieldEditor twitterUserName;
    private StringFieldEditor twitterPassword;
    
    public TwitterPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Twitter Settings");
	}
	
	/**
	 * Creates the field editors.
	 */
	public void createFieldEditors() {
	    twitterUserName = new StringFieldEditor(
	        PreferenceConstants.TWITTER_USERNAME,
	        "Twitter User name",
	        getFieldEditorParent()
	    );
		addField(twitterUserName);

		twitterPassword = new StringFieldEditor(
            PreferenceConstants.TWITTER_PASSWORD,
            "Twitter Password",
            getFieldEditorParent()
        );
        addField(twitterPassword);
	}

	public void init(IWorkbench workbench) {}
	
}
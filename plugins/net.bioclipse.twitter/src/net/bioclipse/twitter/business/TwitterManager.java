/*******************************************************************************
 * Copyright (c) 2009  Jonathan Alvarsson <jonalv@users.sf.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.twitter.business;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.twitter.Activator;
import net.bioclipse.twitter.preferences.PreferenceConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;

import winterwell.jtwitter.Twitter;

public class TwitterManager implements IBioclipseManager {

    private static final Logger logger = Logger.getLogger(TwitterManager.class);
    
    private Twitter twitter;

    /**
     * Gives a short one word name of the manager used as variable name when
     * scripting.
     */
    public String getManagerName() {
        return "twitter";
    }

    public void setStatus(String status) throws BioclipseException {
        if (status == null)
            throw new BioclipseException(
                "Status message must not be null."
            );
        
        if (status.length() > 140)
            throw new BioclipseException(
                "Status message may not exceed 140 characters."
            );

        if (twitter == null) {
            Preferences prefs = Activator.getDefault().getPluginPreferences();
            String username = prefs.getString(PreferenceConstants.TWITTER_USERNAME);
            String password = prefs.getString(PreferenceConstants.TWITTER_PASSWORD);
            twitter = new Twitter(username, password);
            logger.info("Logged in as: " + username);
        }
        twitter.setStatus(status);
    }

}

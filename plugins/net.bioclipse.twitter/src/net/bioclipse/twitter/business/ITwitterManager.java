/*******************************************************************************
 * Copyright (c) 2009 Egon Willighagen <egonw@users.sf.net> 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.twitter.business;

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
//The Twitter feature is not included in the social feature for the moment
@PublishedClass(
    value="Manager that allows interaction with Twitter."
)
public interface ITwitterManager extends IBioclipseManager {

    @PublishedMethod(
        methodSummary="Sends a status message to the Twitter network"
    )
    public void setStatus(String status) throws BioclipseException;

}

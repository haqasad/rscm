/*
 * Really Simple Context Middleware (RCSM)
 *
 * Copyright (c) 2012 The RCSM Team
 *
 * This file is part of the RCSM: the Really Simple Context Middleware for ANDROID. More information about the project
 * is available at: http://code.google.com/p/rscm
 *
 * The RCSM is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.aspectsense.rscm.context.plugin;

import android.os.Bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Date: 4/14/12
 * Time: 8:32 PM
 */
public interface IPluginRecord extends Serializable
{
    public String getPackageName();

    public String getCategory();

    public String [] getRequiredPermissions();

    public String [] getProvidedScopes();

    public String [] getRequiredScopes();

    public Map<String,String> getMetadata();
}

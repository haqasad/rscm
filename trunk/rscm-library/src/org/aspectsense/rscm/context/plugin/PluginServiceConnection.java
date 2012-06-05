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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.aspectsense.rscm.IContextListener;
import org.aspectsense.rscm.IContextPlugin;

/**
 * Date: 4/14/12
 * Time: 1:12 PM
 */
public class PluginServiceConnection implements ServiceConnection
{
    public static final String TAG = "org.aspectsense.rscm.context.plugin.PluginServiceConnection";

    private final IContextListener contextListener;

    public PluginServiceConnection(final IContextListener contextListener)
    {
        this.contextListener = contextListener;
    }

    private IContextPlugin contextPlugin;

    public void onServiceConnected(final ComponentName className, final IBinder boundService)
    {
        contextPlugin = IContextPlugin.Stub.asInterface(boundService);
        try
        {
            contextPlugin.setContextListener(contextListener);
        }
        catch (RemoteException re)
        {
            Log.e(TAG, "Error while enabling and connecting with plugin: " + contextPlugin);
        }
    }

    public void onServiceDisconnected(ComponentName className)
    {
        try
        {
            contextPlugin.unsetContextListener();
        }
        catch(RemoteException re)
        {
            Log.e(TAG, "Error while disabling and disconnecting from plugin: " + contextPlugin);
        }
        contextPlugin = null;
    }
}


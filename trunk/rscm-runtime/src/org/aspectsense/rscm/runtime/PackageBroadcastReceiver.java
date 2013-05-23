/*
 * Really Simple Context Middleware (RSCM)
 *
 * Copyright (c) 2012-2013 The RSCM Team
 *
 * This file is part of the RSCM: the Really Simple Context Middleware for ANDROID. More information about the project
 * is available at: http://code.google.com/p/rscm
 *
 * The RSCM is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
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

package org.aspectsense.rscm.runtime;

import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import org.aspectsense.rscm.IContextManagement;
import org.aspectsense.rscm.context.Intents;

import static org.aspectsense.rscm.Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL;
import static org.aspectsense.rscm.Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL;

/**
* Date: 4/14/12
* Time: 3:50 PM
*/
public class PackageBroadcastReceiver extends BroadcastReceiver
{
    public static final String TAG = "org.aspectsense.rscm.runtime.PackageBroadcastReceiver";

    public static final Intent INTENT_SELECT_CONTEXT_PLUGIN = new Intent(Intents.ACTION_SELECT_CONTEXT_PLUGIN);

    public void onReceive(final Context context, final Intent intent)
    {
        final String packageName = intent.getData().getEncodedSchemeSpecificPart();
        final String action = intent.getAction();
        if(Intent.ACTION_PACKAGE_ADDED.equals(action))
        {
            notifyContextService(context, packageName, CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL);
        }
        else if(Intent.ACTION_PACKAGE_REMOVED.equals(action))
        {
            notifyContextService(context, packageName, CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL);
        }
    }

    private void notifyContextService(final Context context, final String packagePath, final int contextManagementEventType)
    {
        final IBinder iBinder = peekService(context, new Intent(Intents.ACTION_CONTEXT_MANAGEMENT));
        // communicate the event only if the context service is already running
        if(iBinder != null)
        {
            final IContextManagement contextManagement = IContextManagement.Stub.asInterface(iBinder);

            try
            {
                switch (contextManagementEventType)
                {
                    case CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL:
                        contextManagement.installPackage(packagePath);
                        break;

                    case CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL:
                        contextManagement.uninstallPackage(packagePath);
                        break;
                }
            }
            catch (RemoteException re)
            {
                Log.e(TAG, re.getMessage());
            }
        }
    }
}
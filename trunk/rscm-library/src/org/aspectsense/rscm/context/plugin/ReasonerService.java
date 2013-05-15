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

package org.aspectsense.rscm.context.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextListener;

/**
 * Date: 4/17/12
 * Time: 11:34 AM
 */
abstract public class ReasonerService extends SensorService
{
    public static final String TAG = "org.aspectsense.rscm.context.plugin.ReasonerService";

    public static final int CONTEXT_VALUE_TYPE = 0x9000;

    @Override public IBinder onBind(Intent intent)
    {
        bindService(new Intent(IContextAccess.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
        return super.onBind(intent);
    }

    @Override public void onDestroy()
    {
        unregisterContextTypes();
        super.onDestroy();
    }

    abstract protected void onContextValueChanged(final ContextValue contextValue);

    protected String [] getPermissions()
    {
        final PackageManager packageManager = getPackageManager();
        final String packageName = getApplicationContext().getPackageName();
        try
        {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            return packageInfo.requestedPermissions;
        }
        catch (PackageManager.NameNotFoundException nnfe)
        {
            Log.e(TAG, "Unknown package name: " + packageName, nnfe);
            return new String[0];
        }
    }

    protected String [] getRequiredScopes()
    {
        final String packageName = getApplicationContext().getPackageName();
        final PackageManager packageManager = getPackageManager();

        try
        {
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES | PackageManager.GET_META_DATA);

            for(final ServiceInfo serviceInfo : packageInfo.services)
            {
                if(getClass().getName().equals(serviceInfo.name))
                {
                    final Bundle bundle = serviceInfo.metaData;
                    for(final String key : bundle.keySet())
                    {
                        if("required_scopes".equals(key))
                        {
                            final String [] split = bundle.get(key).toString().split(",");
                            final String [] requiredScopes = new String[split.length];
                            for(int i = 0; i < requiredScopes.length; i++)
                            {
                                requiredScopes[i] = split[i].trim();
                            }
                            return requiredScopes;
                        }
                    }
                }
            }
        }
        catch (PackageManager.NameNotFoundException nnfe)
        {
            Log.e(TAG, "Unknown package name: " + packageName, nnfe);
        }
        return new String [0];
    }

    private IContextAccess contextAccess = null;

    protected boolean onRequestContextUpdates(final String scope)
    {
        if(contextAccess != null)
        {
            try
            {
                contextAccess.requestContextUpdates(scope, asynchronousContextListener);
                return true;
            }
            catch (RemoteException re)
            {
                Log.e(TAG, "onRequestContextUpdates: " + re);
            }
        }
        return false;
    }

    protected boolean onRemoveContextUpdates(final String scope)
    {
        if(contextAccess != null)
        {
            try
            {
                contextAccess.removeContextUpdates(scope, asynchronousContextListener);
                return true;
            }
            catch (RemoteException re)
            {
                Log.e(TAG, "onRemoveContextUpdates: " + re);
            }
        }
        return false;
    }

    private void registerContextTypes()
    {
        final String [] requestedScopes = getRequiredScopes();

        if(requestedScopes != null)
        {
            for(final String scope : requestedScopes)
            {
                onRequestContextUpdates(scope);
            }
        }
    }

    private void unregisterContextTypes()
    {
        final String [] requestedScopes = getRequiredScopes();

        if(requestedScopes != null)
        {
            for(final String scope : requestedScopes)
            {
                onRemoveContextUpdates(scope);
            }
        }
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            contextAccess = IContextAccess.Stub.asInterface(iBinder);
            registerContextTypes();
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            contextAccess = null;
        }
    };

    private final IContextListener asynchronousContextListener = new IContextListener.Stub()
    {
        @Override public void onContextValueChanged(ContextValue contextValue) throws RemoteException
        {
            handler.sendMessage(handler.obtainMessage(CONTEXT_VALUE_TYPE, contextValue));
        }
    };

    private Handler handler = new Handler()
    {
        @Override public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case CONTEXT_VALUE_TYPE:
                    final ContextValue contextValue = (ContextValue) msg.obj;
                    ReasonerService.this.onContextValueChanged(contextValue);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
}
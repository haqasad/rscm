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

package org.aspectsense.rscm.context.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextListener;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/8/12
 *         Time: 4:34 PM
 *         todo check if there is someone to handle this kind of intent, otherwise point to market
 */
abstract public class ContextListenerActivity extends Activity
{
    public static final String TAG = "org.aspectsense.rscm.context.client.ContextListenerActivity";

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "$*** Binding to context service");
        bindService(new Intent(IContextAccess.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onStop()
    {
        Log.d(TAG, "$*** Unbinding from context service");
        unregisterContextTypes();
        unbindService(mConnection);
        super.onStop();
    }

//    protected String [] getPermissions()
//    {
//        final PackageManager packageManager = getPackageManager();
//        final String packageName = getApplicationContext().getPackageName();
//        try
//        {
//            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
//            return packageInfo.requestedPermissions;
//        }
//        catch (PackageManager.NameNotFoundException nnfe)
//        {
//            Log.e(TAG, "Unknown package name: " + packageName, nnfe);
//            return new String[0];
//        }
//    }

    private void registerContextTypes()
    {
        final String [] requestedScopes = getRequestedScopes();

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
        final String [] requestedScopes = getRequestedScopes();

        if(requestedScopes != null)
        {
            for(final String scope : requestedScopes)
            {
                onRemoveContextUpdates(scope);
            }
        }
    }

    abstract public String [] getRequestedScopes();

    abstract public void onContextValueChanged(ContextValue contextValue);

    protected void onContextServiceConnected() {}

    protected void onContextServiceDisconnected() {}

    private IContextAccess contextAccess = null;

    protected boolean onRequestContextUpdates(final String scope)
    {
        if(contextAccess != null)
        {
            try
            {
Log.d(TAG, "$*** requestContextUpdates(" + scope + ", " + asynchronousContextListener + ")");
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
Log.d(TAG, "$*** removeContextUpdates(" + scope + ", " + asynchronousContextListener + ")");
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

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            Log.d(TAG, "$*** onServiceConnected(" + componentName + ")");
            onContextServiceConnected();
            contextAccess = IContextAccess.Stub.asInterface(iBinder);
            registerContextTypes();
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            Log.d(TAG, "$*** onServiceDisconnected(" + componentName + ")");
            onContextServiceDisconnected();
            contextAccess = null;
        }
    };

    public static final int CONTEXT_VALUE_TYPE = 0x9000;

    private final IContextListener asynchronousContextListener = new IContextListener.Stub()
    {
        @Override public void onContextValueChanged(ContextValue contextValue) throws RemoteException
        {
            Log.d(TAG, "IContextListener.onContextValueChanged: " + contextValue);
            handler.sendMessage(handler.obtainMessage(CONTEXT_VALUE_TYPE, contextValue));
        }
    };

    private Handler handler = new Handler()
    {
        @Override public void handleMessage(Message msg)
        {
            Log.d(TAG, "handler.handleMessage: " + msg);
            switch (msg.what)
            {
                case CONTEXT_VALUE_TYPE:
                    final ContextValue contextValue = (ContextValue) msg.obj;
                    ContextListenerActivity.this.onContextValueChanged(contextValue);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };
}
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

import android.app.Service;
import android.content.*;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextListener;

/**
 * The implementing plugin must provide:
 *     <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
 * and:
 *     <receiver android:name="org.aspectsense.rscm.context.plugin.BackgroundContextListenerService$BootCompletedReceiver">
 *         <intent-filter>
 *             <action android:name="android.intent.action.BOOT_COMPLETED" />
 *             <action android:name="org.aspectsense.rscm.BOOT_COMPLETED" />\
 *         </intent-filter>
 *     </receiver>
 *
 * Date: 8/6/12
 * Time: 8:06 AM
 */
abstract public class BackgroundContextListenerService extends Service
{
    public static final String ACTION_TYPE_BOOT_COMPLETED = "org.aspectsense.rscm.BOOT_COMPLETED";

    public static final String TAG = "org.aspectsense.rscm.context.plugin.BackgroundContextListenerService";

//    @Override public void onCreate()
//    {
//        super.onCreate();
//    }
//
//    @Override public int onStartCommand(Intent intent, int flags, int startId)
//    {
//        final int result = super.onStartCommand(intent, flags, startId);
//        Log.d(TAG, "$*** Binding to context service");
//        bindService(new Intent(IContextAccess.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
//        return result;
//    }
//
//    @Override public void onDestroy()
//    {
//        Log.d(TAG, "$*** Unbinding from context service");
//        unregisterContextTypes();
//        unbindService(mConnection);
//        super.onDestroy();
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
                    BackgroundContextListenerService.this.onContextValueChanged(contextValue);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };

    public class BootCompletedReceiver extends BroadcastReceiver
    {
        @Override public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "Boot completed event received");
            Toast.makeText(context, "Boot completed event received", Toast.LENGTH_SHORT).show();
            bindService(new Intent(IContextAccess.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
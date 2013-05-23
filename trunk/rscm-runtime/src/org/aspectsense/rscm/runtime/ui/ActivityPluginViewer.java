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

package org.aspectsense.rscm.runtime.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.aspectsense.rscm.*;
import org.aspectsense.rscm.runtime.R;

import java.util.List;
import java.util.Vector;

public class ActivityPluginViewer extends Activity
{
    public static final String TAG = "org.aspectsense.rscm.viewer.ActivityPluginViewer";

    private ListView listView;

    private List<PluginRecord> plugins = new Vector<PluginRecord>();

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugins_viewer);

        this.listView = (ListView) findViewById(R.id.main_list_view);
        listView.setAdapter(new ArrayAdapter<PluginRecord>(this, R.layout.services_row, plugins));
    }

    @Override protected void onStart()
    {
        super.onResume();
        bindService(new Intent(IContextManagement.class.getName()), mContextManagementConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(IContextAccess.class.getName()), mContextAccessConnection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onStop()
    {
        super.onPause();
        unbindService(mContextManagementConnection);
        unbindService(mContextAccessConnection);
    }

    private IContextManagement contextManagement;
    private IContextAccess contextAccess;

    private IContextManagementListener contextManagementListener = new IContextManagementListener.Stub()
    {
        @Override public void onPluginInstalled(PluginRecord pluginRecord) throws RemoteException
        {
            Log.d(TAG, "ActivityPluginViewer#onPluginInstalled: " + pluginRecord);
            handler.sendMessage(handler.obtainMessage(Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL, pluginRecord));
        }

        @Override public void onPluginUninstalled(PluginRecord pluginRecord) throws RemoteException
        {
            Log.d(TAG, "ActivityPluginViewer#onPluginUninstalled: " + pluginRecord);
            handler.sendMessage(handler.obtainMessage(Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL, pluginRecord));
        }

        @Override public String toString()
        {
            return "ActivityPluginViewer#IContextManagementListener";
        }
    };

    private Handler handler = new Handler()
    {
        @Override public void handleMessage(Message msg)
        {
            Log.d(TAG, "ActivityPluginViewer#handler.handleMessage: " + msg);
            switch (msg.what) {
                case Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL:
                {
                    final PluginRecord pluginRecord = (PluginRecord) msg.obj;
                    ActivityPluginViewer.this.onPluginInstalled(pluginRecord);
                    break;
                }
                case Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL:
                {
                    final PluginRecord pluginRecord = (PluginRecord) msg.obj;
                    ActivityPluginViewer.this.onPluginUninstalled(pluginRecord);
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void onConnection(final List<PluginRecord> pluginRecords)
    {
        plugins.clear();
        plugins.addAll(pluginRecords);
        Toast.makeText(this, "Connected (" + pluginRecords.size() + " plugins)", Toast.LENGTH_SHORT).show();
        listView.setAdapter(new PluginListAdapter(this, plugins, contextAccess, contextManagement));
    }

    private void onDisconnection()
    {
        plugins.clear();
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        listView.setAdapter(new PluginListAdapter(this, plugins, contextAccess, contextManagement));
    }

    private void onPluginInstalled(final PluginRecord pluginRecord)
    {
        Toast.makeText(this, "Installed: " + pluginRecord.getPackageName(), Toast.LENGTH_SHORT).show();
        plugins.add(pluginRecord);
        listView.setAdapter(new PluginListAdapter(this, plugins, contextAccess, contextManagement));
    }

    private void onPluginUninstalled(final PluginRecord pluginRecord)
    {
        Toast.makeText(this, "Uninstalled: " + pluginRecord.getPackageName(), Toast.LENGTH_SHORT).show();
        plugins.remove(pluginRecord);
        listView.setAdapter(new PluginListAdapter(this, plugins, contextAccess, contextManagement));
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mContextManagementConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            contextManagement = IContextManagement.Stub.asInterface(iBinder);
            try
            {
                // get installed plugins
                final List<PluginRecord> installedPluginRecords = contextManagement.getInstalledPlugins();
                onConnection(installedPluginRecords);

                // register for notifications
                contextManagement.requestContextManagementUpdates(contextManagementListener);
            }
            catch (RemoteException re)
            {
                Log.e(TAG, re.getMessage());
            }
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            try
            {
                // unregister from notifications
                contextManagement.removeContextManagementUpdates(contextManagementListener);

                // clear up local state
                onDisconnection();
            }
            catch (RemoteException re)
            {
                Log.e(TAG, re.getMessage());
            }
            contextManagement = null;
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mContextAccessConnection = new ServiceConnection()
    {
        @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            contextAccess = IContextAccess.Stub.asInterface(iBinder);
        }

        @Override public void onServiceDisconnected(ComponentName componentName)
        {
            contextAccess = null;
        }
    };
}
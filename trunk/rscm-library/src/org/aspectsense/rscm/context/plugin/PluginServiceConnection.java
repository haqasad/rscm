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


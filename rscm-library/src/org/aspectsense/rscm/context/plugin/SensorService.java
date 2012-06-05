package org.aspectsense.rscm.context.plugin;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.aspectsense.rscm.*;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 * Date: 4/13/12
 * Time: 1:01 PM
 */
abstract public class SensorService extends Service
{
    public static final String TAG = "org.aspectsense.rscm.context.plugin.SensorService";

    public static String PACKAGE_NAME;

    @Override public IBinder onBind(Intent intent)
    {
        PACKAGE_NAME = getApplicationContext().getPackageName(); // initialize app package name

        return mBinder;
    }

    protected String [] getProvidedScopes()
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
                        if("provided_scopes".equals(key))
                        {
                            final String [] split = bundle.get(key).toString().split(",");
                            final String [] providedScopes = new String[split.length];
                            for(int i = 0; i < providedScopes.length; i++)
                            {
                                providedScopes[i] = split[i].trim();
                            }
                            return providedScopes;
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

    private IContextListener listener;

    private final IContextPlugin.Stub mBinder = new IContextPlugin.Stub()
    {
        @Override public void setContextListener(IContextListener contextListener) throws RemoteException
        {
            listener = contextListener;
        }

        @Override public void unsetContextListener() throws RemoteException
        {
            listener = null;
        }

//        @Override public ContextValue getLastContextValue(String scope) throws RemoteException
//        {
//            return SensorService.this.getLastContextValue(scope);
//        }
    };

//    abstract public ContextValue getLastContextValue(String scope);

    protected void notifyListener(final ContextValue contextValue)
    {
        try
        {
            if(listener != null)
            {
                listener.onContextValueChanged(contextValue);
            }
        }
        catch (RemoteException re)
        {
            Log.e(TAG, "Remote exception while contacting: " + listener);
        }
    }
}
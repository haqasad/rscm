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

package org.aspectsense.rscm.runtime;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.util.Log;
import org.aspectsense.rscm.*;
import org.aspectsense.rscm.context.Intents;
import org.aspectsense.rscm.context.plugin.PluginServiceConnection;

import java.util.*;

/**
 * Also see: http://developer.android.com/guide/topics/fundamentals/bound-services.html#Messenger
 *
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/7/12
 *         Time: 7:03 PM
 */
public class ContextService extends Service
{
    public static final String TAG = "org.aspectsense.rscm.runtime.ContextService";

    /**
     * The system calls this method when another component wants to bind with the service (such as to perform RPC), by
     * calling bindService(). In your implementation of this method, you must provide an interface that clients use to
     * communicate with the service, by returning an IBinder. You must always implement this method, but if you don't
     * want to allow binding, then you should return null.
     */
    public IBinder onBind(Intent intent)
    {
        if(Intents.ACTION_CONTEXT_ACCESS.equals(intent.getAction()))
        {
            return mBinderContextAccess;
        }
        else if(Intents.ACTION_CONTEXT_MANAGEMENT.equals(intent.getAction()))
        {
            return mBinderContextManagement;
        }

        Log.e(TAG, "Unknown action type: " + intent.getAction());
        return null;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent, flags, startId);
    }

    private final Map<String, Set<IContextListener>> scopesToListeners = new HashMap<String, Set<IContextListener>>();

    private void updateDatabase(final String scope, final ContextValue contextValue)
    {
        // todo insert in DB?
    }

    private PackageManager packageManager;

    /**
     * The system calls this method when the service is first created, to perform one-time setup procedures (before it
     * calls either onStartCommand() or onBind()). If the service is already running, this method is not called.
     */
    @Override public void onCreate()
    {
        super.onCreate();

        packageManager = this.getPackageManager();

        initPlugins();
        resolvePlugins();
    }

    /**
     * The system calls this method when the service is no longer used and is being destroyed. Your service should
     * implement this to clean up any resources such as threads, registered scopesToListeners, receivers, etc. This is the
     * last call the service receives.
     */
    @Override public void onDestroy()
    {
        // stop and clean up any remaining plugins (the scopesToListeners should already be empty)
        activatePlugins();

        super.onDestroy();
    }

    private void addContextListener(String scope, IContextListener contextListener)
    {
        synchronized (scopesToListeners)
        {
            if(!scopesToListeners.containsKey(scope))
            {
                scopesToListeners.put(scope, new HashSet<IContextListener>());
            }
            final Set<IContextListener> contextTypeListeners = scopesToListeners.get(scope);
            contextTypeListeners.add(contextListener);
        }
    }

    private void removeContextListener(final String scope, final IContextListener contextListener)
    {
        synchronized (scopesToListeners)
        {
            if(scopesToListeners.containsKey(scope))
            {
                final Set<IContextListener> contextTypeListeners = scopesToListeners.get(scope);
                contextTypeListeners.remove(contextListener);
            }
        }
    }

    private Set<String> getPermissionsForScope(final String scope)
    {
        final Set<String> permissions = new HashSet<String>();

        for(final PluginRecord pluginRecord : installedPlugins)
        {
            if(pluginRecord.hasProvidedScope(scope))
            {
                final String [] requiredPermissions = pluginRecord.getRequiredPermissions();
                Collections.addAll(permissions, requiredPermissions);
            }
        }

        return permissions;
    }

    private final IContextAccess.Stub mBinderContextAccess = new IContextAccess.Stub()
    {
        @Override public void requestContextUpdates(final String scope, final IContextListener contextListener) throws RemoteException
        {
Log.d(TAG, "$%$ scope: " + scope + ", contextListener: " + contextListener);
            if(scope == null) throw new RemoteException("Invalid null value for scope");
            if(contextListener == null) throw new RemoteException("Invalid null value for contextListener");

            final Set<String> requiredPermissions = getPermissionsForScope(scope);
Log.d(TAG, "$%$ requiredPermissions: " + requiredPermissions);
            boolean hasPermissions = true;
            if(requiredPermissions != null)
            {
                for(final String requiredPermission : requiredPermissions)
                {
Log.d(TAG, "$%$ checkCallingPermission for " + requiredPermission + " --> " + (checkCallingPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED));
                    if(checkCallingPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED)
                    {
                        hasPermissions = false;
                        break;
                    }
                }
            }

            if(hasPermissions)
            {
                addContextListener(scope, contextListener);
                activatePlugins();
            }
            else
            {
                Log.w(TAG, "$%$ Context listener " + contextListener + " has no required permissions: " + requiredPermissions);
                // todo add a message in the status bar of android
            }
        }

        @Override public void removeContextUpdates(String scope, IContextListener contextListener) throws RemoteException
        {
            removeContextListener(scope, contextListener);

            activatePlugins();
        }
    };

    private final Map<String,Set<PluginRecord>> pluginRecordMap = new HashMap<String, Set<PluginRecord>>();

    private Set<PluginRecord> getPluginRecords(final String packagePath)
    {
        if(pluginRecordMap.containsKey(packagePath))
        {
            return pluginRecordMap.get(packagePath);
        }
        else
        {
            final Intent selectContextPluginIntent = new Intent(Intents.ACTION_SELECT_CONTEXT_PLUGIN);
            selectContextPluginIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);// remove?
            final List<ResolveInfo> allPluginsResolveInfo = packageManager.queryIntentServices(selectContextPluginIntent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);

            final Set<PluginRecord> pluginRecords = new HashSet<PluginRecord>();

            for(final ResolveInfo pluginResolveInfo : allPluginsResolveInfo)
            {
                final ServiceInfo serviceInfo = pluginResolveInfo.serviceInfo;
                final String packageName = serviceInfo.packageName;
                final String category = serviceInfo.name;

                if(packagePath.equals(packageName))
                {
                    try
                    {
                        final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS | PackageManager.GET_SERVICES);
                        final Bundle serviceInfoMetadata = serviceInfo.metaData;

                        if(serviceInfoMetadata != null)
                        {
                            // access categories
                            final List<String> providedContextTypes = new ArrayList<String>();
                            final List<String> requiredContextTypes = new ArrayList<String>();
                            final Set<String> keys = serviceInfoMetadata.keySet();
                            final Map<String,String> metadata = new HashMap<String, String>();
                            for(final String key : keys)
                            {
                                final String value = serviceInfoMetadata.get(key).toString();
                                if(key.equals("provided_scopes"))
                                {
                                    final String [] providedScopes = value.split(",");
                                    for(final String providedScope : providedScopes)
                                    {
                                        providedContextTypes.add(providedScope.trim());
                                    }
                                }
                                else if(key.startsWith("required_scopes"))
                                {
                                    final String [] requiredScopes = value.split(",");
                                    for(final String requiredScope : requiredScopes)
                                    {
                                        requiredContextTypes.add(requiredScope.trim());
                                    }
                                }
                                else
                                {
                                    metadata.put(key, value);
                                }
                            }

                            final String [] requestedPermissions = packageInfo.requestedPermissions == null ? new String[0] : packageInfo.requestedPermissions;

                            final PluginRecord pluginRecord = new PluginRecord(
                                    packageName,
                                    category,
                                    requestedPermissions,
                                    providedContextTypes.toArray(new String[providedContextTypes.size()]),
                                    requiredContextTypes.toArray(new String[requiredContextTypes.size()]),
                                    metadata);

                            pluginRecords.add(pluginRecord);
                        }
                    }
                    catch (PackageManager.NameNotFoundException nnfe)
                    {
                        Log.e(TAG, nnfe.getMessage());
                    }
                }
            }

            pluginRecordMap.put(packagePath, pluginRecords);
            return pluginRecords;
        }
    }

    // initializes the plugins when the service is first started
    private void initPlugins()
    {
        installedPlugins.clear();

        final Intent selectContextPluginIntent = new Intent(Intents.ACTION_SELECT_CONTEXT_PLUGIN);
        selectContextPluginIntent.setFlags( Intent.FLAG_DEBUG_LOG_RESOLUTION );
        final List<ResolveInfo> allPluginsResolveInfo = packageManager.queryIntentServices(selectContextPluginIntent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);

        for(final ResolveInfo pluginResolveInfo : allPluginsResolveInfo)
        {
            final ServiceInfo serviceInfo = pluginResolveInfo.serviceInfo;
            final String packageName = serviceInfo.packageName;

            final Set<PluginRecord> pluginRecords = getPluginRecords(packageName);
            for(final PluginRecord pluginRecord : pluginRecords)
            {
                installPlugin(pluginRecord);
            }
        }
    }

    private void installPlugin(final PluginRecord pluginRecord)
    {
        if(pluginRecord != null)
        {
            installedPlugins.add(pluginRecord);
            for(final String scope : pluginRecord.getProvidedScopes())
            {
                Set<PluginRecord> pluginRecords = providedScopesToPlugins.get(scope);
                if(pluginRecords == null)
                {
                    pluginRecords = new HashSet<PluginRecord>();
                    providedScopesToPlugins.put(scope, pluginRecords);
                }
                // assert that no matching plugin record is registered already
                pluginRecords.add(pluginRecord);
            }

            resolvePlugins();
            activatePlugins();

            notifyContextManagementListeners(pluginRecord, Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL);
        }
    }

    private void uninstallPlugin(final PluginRecord pluginRecord)
    {
        if(pluginRecord != null)
        {
            for(final String scope : pluginRecord.getProvidedScopes())
            {
                Set<PluginRecord> pluginRecords = providedScopesToPlugins.get(scope);
                if(pluginRecords != null)
                {
                    pluginRecords.remove(pluginRecord);
                    if(pluginRecords.isEmpty()) providedScopesToPlugins.remove(scope);
                }
            }

            resolvePlugins();
            activatePlugins();

            notifyContextManagementListeners(pluginRecord, Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL);
        }
    }

    private final Set<PluginRecord> installedPlugins = new HashSet<PluginRecord>();
    private final Map<String, Set<PluginRecord>> providedScopesToPlugins = new HashMap<String, Set<PluginRecord>>();
    private final Set<PluginRecord> resolvedPlugins = new HashSet<PluginRecord>();
    private final Map<String, Set<PluginRecord>> resolvedScopesToPlugins = new HashMap<String, Set<PluginRecord>>();
    private final Set<PluginRecord> activePlugins = new HashSet<PluginRecord>();
    private final Map<String, Set<PluginRecord>> activeScopesToPlugins = new HashMap<String, Set<PluginRecord>>();

    /**
     * For details, refer to page 128 of N. Paspallis PhD thesis (http://nearchos.aspectsense.com/phd)
     */
    synchronized private void resolvePlugins()
    {
        final Set<String> resolvedScopes = resolvedScopesToPlugins.keySet();

        boolean changesDetected = true;
        while(changesDetected)
        {
            changesDetected = false;
            for(final PluginRecord pluginRecord : installedPlugins)
            {
                if(!resolvedPlugins.contains(pluginRecord)) // only examine non-resolved plugins
                {
                    if(resolvedScopes.containsAll(Arrays.asList(pluginRecord.getRequiredScopes())))
                    {
                        // mark current plugin as resolved ...
                        resolvedPlugins.add(pluginRecord);

                        // ... and add all its provided scopes to resolvedScopes
                        final String [] pluginProvidedScopes = pluginRecord.getProvidedScopes();
                        for(final String pluginProvidedScope : pluginProvidedScopes)
                        {
                            if(!resolvedScopesToPlugins.containsKey(pluginProvidedScope))
                            {
                                resolvedScopesToPlugins.put(pluginProvidedScope, new HashSet<PluginRecord>());
                            }
                            final Set<PluginRecord> pluginRecords = resolvedScopesToPlugins.get(pluginProvidedScope);
                            pluginRecords.add(pluginRecord);
                        }

                        // clear flag to force more loops
                        changesDetected = true;
                    }
                }
            }
        }

        changesDetected = true;
        while(changesDetected)
        {
            changesDetected = false;
            for(final PluginRecord pluginRecord : installedPlugins)
            {
                if(resolvedPlugins.contains(pluginRecord)) // only examine resolved plugins
                {
                    if(!resolvedScopes.containsAll(Arrays.asList(pluginRecord.getRequiredScopes())))
                    {
                        // mark current plugin as non-resolved ...
                        resolvedPlugins.remove(pluginRecord);

                        // ... and remove all its provided scopes from resolvedScopes
                        final String [] pluginProvidedScopes = pluginRecord.getProvidedScopes();
                        for(final String pluginProvidedScope : pluginProvidedScopes)
                        {
                            if(!resolvedScopesToPlugins.containsKey(pluginProvidedScope))
                            {
                                resolvedScopesToPlugins.put(pluginProvidedScope, new HashSet<PluginRecord>());
                            }
                            final Set<PluginRecord> pluginRecords = resolvedScopesToPlugins.get(pluginProvidedScope);
                            pluginRecords.remove(pluginRecord);
                            if(pluginRecords.isEmpty()) resolvedScopesToPlugins.remove(pluginProvidedScope);
                        }

                        // clear flag to force more loops
                        changesDetected = true;
                    }
                }
            }
        }
    }

    /**
     * For details, refer to page 131 of N. Paspallis PhD thesis (http://nearchos.aspectsense.com/phd)
     */
    synchronized private void activatePlugins()
    {
        final Set<PluginRecord> previouslyActivePlugins = new HashSet<PluginRecord>(activePlugins);

        // first ensure that unresolved plug-ins are removed from the activePlugins set
        for(final PluginRecord pluginRecord : previouslyActivePlugins)
        {
            if(!resolvedPlugins.contains(pluginRecord))
            {
                activePlugins.remove(pluginRecord);
            }
        }

        final Set<String> neededScopes = scopesToListeners.keySet();

        // next make sure that the needed plug-ins are in the [active] plug-ins set
        boolean changesDetected = true;
        while(changesDetected)
        {
            changesDetected = false;
            for(final String neededScope : neededScopes)
            {
                // select plugin
                final Set<PluginRecord> selectedPluginRecords = selectPlugin(neededScope);
                for(final PluginRecord pluginRecord : selectedPluginRecords)
                {
                    if(!activePlugins.contains(pluginRecord))
                    {
                        // add selected plugin to active plugins
                        activePlugins.add(pluginRecord);

                        // register the new plugin as listener as needed
                        for(final String requiredScope : pluginRecord.getRequiredScopes())
                        {
                            if(!scopesToListeners.containsKey(requiredScope))
                            {
                                scopesToListeners.put(requiredScope, new HashSet<IContextListener>());
                            }
                            final Set<IContextListener> registeredListeners = scopesToListeners.get(requiredScope);
                            registeredListeners.add(new ContextListenerPluginWrapper(pluginRecord));
                        }

                        // clear flag to force more loops
                        changesDetected = true;
                    }
                }
            }
        }

        // last make sure that no unneeded plug-ins are in the [active] plug-ins set
        changesDetected = true;
        while(changesDetected)
        {
            changesDetected = false;
            for(final PluginRecord pluginRecord : installedPlugins)
            {
                if(activePlugins.contains(pluginRecord)) // only consider active plugins
                {
                    final String [] providedScopes = pluginRecord.getProvidedScopes();
                    boolean atLeastOneProvidedScopeNeeded = false;
                    for(final String providedScope : providedScopes)
                    {
                        if(scopesToListeners.containsKey(providedScope))
                        {
                            atLeastOneProvidedScopeNeeded = true;
                        }
                    }

                    if(!atLeastOneProvidedScopeNeeded) // if no provided scopes are needed, deactivate the plugin
                    {
                        // deactivate the plugin
                        activePlugins.remove(pluginRecord);

                        // remove any scope dependencies from the scopesToListeners structure
                        final String [] pluginProvidedScopes = pluginRecord.getProvidedScopes();
                        for(final String pluginProvidedScope : pluginProvidedScopes)
                        {
                            final Set<IContextListener> registeredPluginRecords = scopesToListeners.get(pluginProvidedScope);
                            assert registeredPluginRecords != null;
                            registeredPluginRecords.remove(new ContextListenerPluginWrapper(pluginRecord));
                            if(registeredPluginRecords.isEmpty()) scopesToListeners.remove(pluginProvidedScope);
                        }

                        // clear flag to force more loops
                        changesDetected = true;
                    }
                }
            }
        }

        // the actual plugin activation/deactivation takes place below

        // first deactivate plugins as needed...
        for(final PluginRecord previouslyActivePluginRecord : previouslyActivePlugins)
        {
            if(!activePlugins.contains(previouslyActivePluginRecord))
            {
                // deactivate plugin
                disconnectContextPluginService(previouslyActivePluginRecord);

                // update activeScopesToPlugins data structure
                final String [] pluginProvidedScopes = previouslyActivePluginRecord.getProvidedScopes();
                for(final String pluginProvidedScope : pluginProvidedScopes)
                {
                    final Set<PluginRecord> scopeActivePlugins = activeScopesToPlugins.get(pluginProvidedScope);
                    assert scopeActivePlugins != null;
                    scopeActivePlugins.remove(previouslyActivePluginRecord);
                    if(scopeActivePlugins.isEmpty())
                    {
                        activeScopesToPlugins.remove(pluginProvidedScope);
                    }
                }
            }
        }

        // ...then activate new plugins as needed
        for(final PluginRecord activePluginRecord : activePlugins)
        {
            if(!previouslyActivePlugins.contains(activePluginRecord))
            {
                // activate plugin
                connectContextPluginService(activePluginRecord);

                // update activeScopesToPlugins data structure
                final String [] pluginProvidedScopes = activePluginRecord.getProvidedScopes();
                for(final String pluginProvidedScope : pluginProvidedScopes)
                {
                    if(!activeScopesToPlugins.containsKey(pluginProvidedScope))
                    {
                        activeScopesToPlugins.put(pluginProvidedScope, new HashSet<PluginRecord>());
                    }
                    final Set<PluginRecord> scopeActivePlugins = activeScopesToPlugins.get(pluginProvidedScope);
                    scopeActivePlugins.add(activePluginRecord);
                }
            }
        }
    }

    private Set<PluginRecord> selectPlugin(final String scope)
    {
        final Set<PluginRecord> selectedPluginRecords = resolvedScopesToPlugins.get(scope); //todo more focused selection

        return selectedPluginRecords == null ? new HashSet<PluginRecord>() : selectedPluginRecords;
    }

    private final IContextManagement.Stub mBinderContextManagement = new IContextManagement.Stub()
    {
        @Override public boolean isActive(String scope) throws RemoteException
        {
            return activeScopesToPlugins.containsKey(scope);
        }

        @Override public boolean isResolved(String scope) throws RemoteException
        {
            return resolvedScopesToPlugins.containsKey(scope);
        }

        @Override public boolean isInstalled(String scope) throws RemoteException
        {
            return providedScopesToPlugins.containsKey(scope);
        }

        @Override public List<PluginRecord> getInstalledPlugins() throws RemoteException
        {
            return new ArrayList<PluginRecord>(installedPlugins);
        }

        @Override public void requestContextManagementUpdates(IContextManagementListener contextManagementListener) throws RemoteException
        {
            contextManagementListeners.add(contextManagementListener);
        }

        @Override public void removeContextManagementUpdates(IContextManagementListener contextManagementListener) throws RemoteException
        {
            contextManagementListeners.remove(contextManagementListener);
        }

        @Override public void installPackage(String packagePath) throws RemoteException
        {
            final Set<PluginRecord> pluginRecords = getPluginRecords(packagePath);
            for(final PluginRecord pluginRecord : pluginRecords)
            {
                installPlugin(pluginRecord);
            }
        }

        @Override public void uninstallPackage(String packagePath) throws RemoteException
        {
            final Set<PluginRecord> pluginRecords = getPluginRecords(packagePath);
            for(final PluginRecord pluginRecord : pluginRecords)
            {
                uninstallPlugin(pluginRecord);
            }
        }
    };

    private Set<IContextManagementListener> contextManagementListeners = new HashSet<IContextManagementListener>();

    private void notifyContextManagementListeners(final PluginRecord pluginRecord, final int type)
    {
        // run from background thread
        new Thread(new Runnable()
        {
            @Override public void run()
            {
                for(final IContextManagementListener contextManagementListener : contextManagementListeners)
                {
                    try
                    {
                        switch (type)
                        {
                            case Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_INSTALL:
                                contextManagementListener.onPluginInstalled(pluginRecord);
                                break;

                            case Constants.CONTEXT_MANAGEMENT_EVENT_TYPE_UNINSTALL:
                                contextManagementListener.onPluginUninstalled(pluginRecord);
                                break;
                        }
                    }
                    catch (RemoteException re)
                    {
                        Log.e(TAG, re.getMessage());
                    }
                }
            }
        }).start();
    }

    private Map<PluginRecord,PluginServiceConnection> pluginServiceConnections = new HashMap<PluginRecord, PluginServiceConnection>();

    /**
     * Connects to the specified plugin sensor service (and starts it)
     */
    private void connectContextPluginService(final PluginRecord pluginRecord)
    {
        final Intent contextPluginIntent = new Intent(Intents.ACTION_SELECT_CONTEXT_PLUGIN);
        contextPluginIntent.addCategory(pluginRecord.getCategory());
        final PluginServiceConnection pluginServiceConnection = new PluginServiceConnection(asynchronousContextListener);
        bindService(contextPluginIntent, pluginServiceConnection, Context.BIND_AUTO_CREATE);
        pluginServiceConnections.put(pluginRecord, pluginServiceConnection);
    }

    /**
     * Disconnects from the specified plugin sensor service (and stops it)
     */
    private void disconnectContextPluginService(final PluginRecord pluginRecord)
    {
        final PluginServiceConnection pluginServiceConnection = pluginServiceConnections.get(pluginRecord);
        assert pluginServiceConnection != null;
        unbindService(pluginServiceConnection);
        pluginServiceConnections.remove(pluginRecord);
    }

    private void onContextValueChanged(final ContextValue contextValue)
    {
        if(contextValue != null)
        {
            // distribute context events (asynchronously from another thread)
            new Thread(new Runnable() {
                @Override public void run() {
                    final String scope = contextValue.getScope();

                    // update the database
                    updateDatabase(scope, contextValue);

                    // notify registered scopesToListeners
                    final Set<IContextListener> scopeListeners = scopesToListeners.get(scope);
                    if(scopeListeners != null)
                    {
                        for(final IContextListener scopeListener : scopeListeners)
                        {
                            try
                            {
                                scopeListener.onContextValueChanged(contextValue);
                            }
                            catch (RemoteException re)
                            {
                                Log.e(TAG, re.getMessage());
                            }
                        }
                    }
                }
            }).start();
        }
    }

    public static final int CONTEXT_VALUE_TYPE = 0x9000;

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
            switch (msg.what)
            {
                case CONTEXT_VALUE_TYPE:
                    final ContextValue contextValue = (ContextValue) msg.obj;
                    ContextService.this.onContextValueChanged(contextValue);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };

    //todo
    private class ContextListenerPluginWrapper implements IContextListener
    {
        private final PluginRecord pluginRecord;

        ContextListenerPluginWrapper(final PluginRecord pluginRecord)
        {
            this.pluginRecord = pluginRecord;
        }

        @Override public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ContextListenerPluginWrapper that = (ContextListenerPluginWrapper) o;

            if (pluginRecord != null ? !pluginRecord.equals(that.pluginRecord) : that.pluginRecord != null)
            {
                return false;
            }

            return true;
        }

        @Override public int hashCode()
        {
            return pluginRecord != null ? pluginRecord.hashCode() : 0;
        }

        @Override public void onContextValueChanged(ContextValue contextValue) throws RemoteException
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override public IBinder asBinder()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
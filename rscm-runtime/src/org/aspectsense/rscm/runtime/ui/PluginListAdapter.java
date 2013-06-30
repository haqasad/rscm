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

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextManagement;
import org.aspectsense.rscm.PluginRecord;
import org.aspectsense.rscm.runtime.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date: 4/16/12
 * Time: 9:05 PM
 */
public class PluginListAdapter extends BaseAdapter
{
    public static final String TAG = "";

    private final PackageManager packageManager;

    private final Context context;
    private final List<PluginRecord> pluginRecords;

    private final ContextAccessAndManagementPortal contextAccessAndManagementPortal;

    public PluginListAdapter(final Context context, final List<PluginRecord> pluginRecords, final ContextAccessAndManagementPortal contextAccessAndManagementPortal)
    {
        this.context = context;
        this.pluginRecords = pluginRecords;

        this.contextAccessAndManagementPortal = contextAccessAndManagementPortal;

        this.packageManager = context.getPackageManager();
    }

    private boolean isActivePlugin(final String packageName)
    {
        final IContextManagement contextManagement = contextAccessAndManagementPortal.getContextManagement();
        if(contextManagement != null)
        {
            try
            {
                return contextManagement.isActivePlugin(packageName);
            }
            catch (RemoteException re)
            {
                Log.e(TAG, re.getMessage(), re);
            }
        }

        return false;
    }

    private ContextValue getLastContextValue(final String scope)
    {
        final IContextAccess contextAccess = contextAccessAndManagementPortal.getContextAccess();
        if(contextAccess != null)
        {
            try
            {
                return contextAccess.getLastContextValue(scope);
            }
            catch (RemoteException re)
            {
                Log.e(TAG, re.getMessage(), re);
            }
        }

        return null;
    }

    @Override public int getCount()
    {
        return pluginRecords.size();
    }

    @Override public Object getItem(int position)
    {
        return pluginRecords.get(position);
    }

    @Override public long getItemId(int position)
    {
        return position;
    }

    @Override public View getView(final int position, final View convertView, final ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
        {
            final LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.services_row, null);
        }

        final ImageView icon = (ImageView) view.findViewById(R.id.icon);
        final TextView packageName = (TextView) view.findViewById(R.id.package_name);
        final TextView category = (TextView) view.findViewById(R.id.category);
        final TextView permissions = (TextView) view.findViewById(R.id.permissions);
        final TextView providedScopes = (TextView) view.findViewById(R.id.provided_scopes);
        final TextView requiredScopes = (TextView) view.findViewById(R.id.required_scopes);
        final TextView metadata = (TextView) view.findViewById(R.id.metadata);
        final TextView contextValue = (TextView) view.findViewById(R.id.context_value);
        final Button activatePlugin = (Button) view.findViewById(R.id.button_activate_plugin);
        final Button refreshContextValue = (Button) view.findViewById(R.id.button_refresh_context_value);

        final PluginRecord pluginRecord = pluginRecords.get(position);

        try
        {
            final Drawable iconDrawable = packageManager.getApplicationIcon(pluginRecord.getPackageName());
            icon.setImageDrawable(iconDrawable);
            icon.setVisibility(View.VISIBLE);
        }
        catch (PackageManager.NameNotFoundException nnfe)
        {
            icon.setVisibility(View.GONE);
        }

        packageName.setText(pluginRecord.getPackageName());
        category.setText(pluginRecord.getCategory());

        {
            final StringBuilder requestedPermissionsStringBuilder = new StringBuilder("<b>Requested permissions</b><br/>");
            final String [] requestedPermissions = pluginRecord.getRequiredPermissions();
            for(final String requestedPermission : requestedPermissions)
            {
                requestedPermissionsStringBuilder.append(requestedPermission).append("<br/>");
            }
            permissions.setText(Html.fromHtml(requestedPermissionsStringBuilder.toString()));
        }

        {
            final StringBuilder providedScopesStringBuilder = new StringBuilder("<b>Provided scopes</b><br/>");
            for(final String providedScope : pluginRecord.getProvidedScopes())
            {
                providedScopesStringBuilder.append(providedScope).append("<br/>");
            }
            providedScopes.setText(Html.fromHtml(providedScopesStringBuilder.toString()));
        }

        {
            final StringBuilder requiredScopesStringBuilder = new StringBuilder("<b>Required scopes</b><br/>");
            for(final String requiredScope : pluginRecord.getRequiredScopes())
            {
                requiredScopesStringBuilder.append(requiredScope).append("<br/>");
            }
            requiredScopes.setText(Html.fromHtml(requiredScopesStringBuilder.toString()));
        }

        {
            final StringBuilder metadataStringBuilder = new StringBuilder("<b>Metadata</b><br/>");
            final Map<String,String> metadataBundle = pluginRecord.getMetadata();
            for(final String metadataKey : metadataBundle.keySet())
            {
                final String metadataValue = metadataBundle.get(metadataKey);
                metadataStringBuilder.append(metadataKey).append(": ").append(metadataValue).append("<br/>");
            }
            metadata.setText(Html.fromHtml(metadataStringBuilder.toString()));
        }

        {
            final boolean isActive = isActivePlugin(pluginRecord.getPackageName());
            activatePlugin.setText(isActive ? R.string.Deactivate : R.string.Activate);
            activatePlugin.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    activatePlugin(pluginRecord);
                }
            });
        }

        {
            updateContextValue(contextValue, pluginRecord);
            refreshContextValue.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    updateContextValue(contextValue, pluginRecord);
                }
            });
        }

        return view;
    }

    private void activatePlugin(final PluginRecord pluginRecord)
    {
        final boolean isActive = isActivePlugin(pluginRecord.getPackageName());
        if(isActive)
        {
            //todo
        }
        else
        {
            //todo
        }
    }

    private void updateContextValue(final TextView contextValueTextView, final PluginRecord pluginRecord)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        final String [] scopes = pluginRecord.getProvidedScopes();
Log.d(TAG, "showing scopes: " + Arrays.toString(scopes));//todo delete
        for(final String scope : scopes)
        {
            stringBuilder.append(scope).append(" --> ");
            final ContextValue contextValue = getLastContextValue(scope);
            stringBuilder.append(contextValue);
            stringBuilder.append("\n");
        }
        contextValueTextView.setText(stringBuilder.toString());
    }
}
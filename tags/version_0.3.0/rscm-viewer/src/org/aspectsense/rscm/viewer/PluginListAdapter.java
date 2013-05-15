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

package org.aspectsense.rscm.viewer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.aspectsense.rscm.PluginRecord;

import java.util.List;
import java.util.Map;

/**
 * Date: 4/16/12
 * Time: 9:05 PM
 */
public class PluginListAdapter extends BaseAdapter
{
    private final PackageManager packageManager;

    private final Context context;
    private final List<PluginRecord> pluginRecords;

    public PluginListAdapter(final Context context, final List<PluginRecord> pluginRecords)
    {
        this.context = context;
        this.pluginRecords = pluginRecords;

        this.packageManager = context.getPackageManager();
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

        return view;
    }
}
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.PluginRecord;
import org.aspectsense.rscm.runtime.R;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date: 5/1/13
 * Time: 7:44 PM
 */
public class ContextValueListAdapter extends BaseAdapter
{
    public static final String TAG = "";

    private final Context context;
    private final ContextValue [] contextValues;

    private final long now = System.currentTimeMillis();

    public ContextValueListAdapter(final Context context, final ContextValue [] contextValues)
    {
        this.context = context;
        this.contextValues = contextValues;
    }

    @Override public int getCount()
    {
        return contextValues.length;
    }

    @Override public Object getItem(int position)
    {
        return contextValues[position];
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
            view = vi.inflate(R.layout.context_values_row, null);
        }

        final TextView timestampTextView = (TextView) view.findViewById(R.id.context_value_timestamp);
        final TextView jsonValueTextView = (TextView) view.findViewById(R.id.context_value_json_value);

        final ContextValue contextValue = contextValues[position];

        final long timestamp = contextValue.getCreationTimestamp();
        timestampTextView.setText(getTimeDifferenceInWords(now - timestamp));

        jsonValueTextView.setText(contextValue.getValueAsJSONString());

        return view;
    }

    private String getTimeDifferenceInWords(final long duration)
    {
        if(duration < 1000)
        {
            return String.format("%1$d ms ago", duration);
        }
        else if(duration < 60000)
        {
            return String.format("%1$.2f secs ago", duration/1000.0);
        }
        else if(duration < 3600000)
        {
            return String.format("%1$d mins %2$.2f secs ago", duration/60000, (duration%60000)/1000.0);
        }
        else if(duration < 86400000)
        {
            return String.format("%1$d hrs %2$d mins %3$.2f secs ago", duration/3600000, (duration%3600000)/60000, (duration%60000)/1000.0);
        }
        else
        {
            return String.format("%1$d days %2$d hrs %3$d mins %4$.2f secs ago", duration/86400000, (duration%86400000)/3600000, (duration%3600000)/60000, (duration%60000)/1000.0);
        }
    }
}
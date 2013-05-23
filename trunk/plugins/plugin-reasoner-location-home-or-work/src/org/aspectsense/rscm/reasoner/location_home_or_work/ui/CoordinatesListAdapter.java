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

package org.aspectsense.rscm.reasoner.location_home_or_work.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.aspectsense.rscm.reasoner.location_home_or_work.R;
import org.aspectsense.rscm.reasoner.location_home_or_work.data.Coordinates;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date: 4/16/12
 * Time: 9:05 PM
 */
public class CoordinatesListAdapter extends BaseAdapter
{
    public static final String TAG = "";

    private final Context context;
    private final Coordinates [] coordinates;

    private final long now = System.currentTimeMillis();

    public CoordinatesListAdapter(final Context context, final Coordinates [] coordinates)
    {
        this.context = context;
        this.coordinates = coordinates;
    }

    @Override public int getCount()
    {
        return coordinates.length;
    }

    @Override public Object getItem(int position)
    {
        return coordinates[position];
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
            view = vi.inflate(R.layout.coordinates_row, null);
        }

        final TextView timestampTextView = (TextView) view.findViewById(R.id.timestamp);
        final TextView workHomeElsewhereTextView = (TextView) view.findViewById(R.id.work_home_elsewhere);
        final TextView coordinatesTextView = (TextView) view.findViewById(R.id.coordinates);

        final Coordinates coordinates = this.coordinates[position];

        final long timestamp = coordinates.getTimestamp();
        timestampTextView.setText(formatTimestampAsDate(timestamp) + "\n" + getTimeDifferenceInWords(now - timestamp));
        workHomeElsewhereTextView.setText(coordinates.getWhere() == Coordinates.TIME_AT_HOME ? "H" : coordinates.getWhere() == Coordinates.TIME_AT_WORK ? "W" : "E");

        coordinatesTextView.setText("(" + coordinates.getLat() + ", " + coordinates.getLng() + ")");

        return view;
    }

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    private String formatTimestampAsDate(final long timestamp)
    {
        return simpleDateFormat.format(new Date(timestamp));
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
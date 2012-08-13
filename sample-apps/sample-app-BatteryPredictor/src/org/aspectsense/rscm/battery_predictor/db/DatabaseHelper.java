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

package org.aspectsense.rscm.battery_predictor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYSeries;

import java.util.Date;

import static org.aspectsense.rscm.battery_predictor.db.DatabaseMetadata.*;

/**
 * User: Nearchos Paspallis
 * Date: 10/8/11
 * Time: 12:11 PM
 *
 */
public class DatabaseHelper
{
    public static final String TAG = "org.aspectsense.rscm.battery_predictor.db.DatabaseHelper";

    private static DatabaseHelper instance = null;

    static public DatabaseHelper getDatabaseHelper(final Context context)
    {
        // make sure all threads access one and only one database helper
        // (and consequently one database open helper)
        synchronized (DatabaseHelper.class)
        {
            if (instance == null)
            {
                instance = new DatabaseHelper(context);
            }

            return instance;
        }
    }

    private DatabaseOpenHelper databaseOpenHelper;

    private DatabaseHelper(final Context context)
    {
        this.databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, DATABASE_VERSION);
    }

    // ------ handling insertions in the database ---------- //

    public long insert(final long timestamp, final int battery_level, final int power_connected, final double lat, final double lng)
    {
        final SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();

        database.beginTransaction();

        final ContentValues contentValues = new ContentValues();
        contentValues.put(ContextValuesTableMetadata.TIMESTAMP, timestamp);
        contentValues.put(ContextValuesTableMetadata.BATTERY_LEVEL, battery_level);
        contentValues.put(ContextValuesTableMetadata.POWER_CONNECTED, power_connected);
        contentValues.put(ContextValuesTableMetadata.LATITUDE, lat);
        contentValues.put(ContextValuesTableMetadata.LONGITUDE, lng);

        final long rowId = database.insert(ContextValuesTableMetadata.TABLE_NAME, null, contentValues);
        database.setTransactionSuccessful();

        database.endTransaction();

        return rowId;
    }

    public XYSeries getBatterySeries()
    {
        final Cursor cursor = getContextValuesCursor(null, null, ContextValuesTableMetadata.TIMESTAMP + " ASC");

        final int INDEX_BATTERY_LEVEL   = cursor.getColumnIndex(ContextValuesTableMetadata.BATTERY_LEVEL);
        final int INDEX_TIMESTAMP       = cursor.getColumnIndex(ContextValuesTableMetadata.TIMESTAMP);

        int lastBatteryLevel = -1;

        final XYSeries xySeries = new TimeSeries("Battery level");

        final int numOfEntries = cursor.getCount();
        cursor.moveToFirst();
Log.d(TAG, "timestamp: " + cursor.getLong(INDEX_TIMESTAMP) + " (" + new Date(cursor.getLong(INDEX_TIMESTAMP)) + ")");
        for(int i = 0; i < numOfEntries; i++)
        {
            final int batteryLevel = cursor.getInt(INDEX_BATTERY_LEVEL);
            if(batteryLevel != lastBatteryLevel)
            {
                lastBatteryLevel = batteryLevel;
                final long timestamp = cursor.getLong(INDEX_TIMESTAMP);

                xySeries.add(timestamp, batteryLevel);
            }

            cursor.moveToNext();
        }

        cursor.close();

        return xySeries;
    }

    public XYSeries getConnectivitySeries()
    {
        final Cursor cursor = getContextValuesCursor(null, null, ContextValuesTableMetadata.TIMESTAMP + " ASC");

        final int INDEX_POWER_CONNECTED = cursor.getColumnIndex(ContextValuesTableMetadata.POWER_CONNECTED);
        final int INDEX_TIMESTAMP       = cursor.getColumnIndex(ContextValuesTableMetadata.TIMESTAMP);

        int lastPowerConnected = -1;

        final XYSeries xySeries = new TimeSeries("Power connected");

        final int numOfEntries = cursor.getCount();
        cursor.moveToFirst();
        for(int i = 0; i < numOfEntries; i++)
        {
            final int powerConnected = cursor.getInt(INDEX_POWER_CONNECTED);
            final long timestamp = cursor.getLong(INDEX_TIMESTAMP);

            if(lastPowerConnected == -1) // first entry
            {
Log.d(TAG, "timestamp: " + timestamp + " (" + new Date(timestamp) + "), lastPowerConnected: " + lastPowerConnected + ", powerConnected: " + powerConnected);
                xySeries.add(timestamp, powerConnected == ContextValuesTableMetadata.POWER_CONNECTED_TRUE ? 100 : 1);
            }
            else if(powerConnected != lastPowerConnected)
            {
                xySeries.add(timestamp - 1, lastPowerConnected == ContextValuesTableMetadata.POWER_CONNECTED_TRUE ? 100 : 1);
                xySeries.add(timestamp, powerConnected == ContextValuesTableMetadata.POWER_CONNECTED_TRUE ? 100 : 1);
            }
            else if(i == numOfEntries -1) // last entry
            {
                xySeries.add(timestamp, powerConnected == ContextValuesTableMetadata.POWER_CONNECTED_TRUE ? 100 : 1);
            }

            lastPowerConnected = powerConnected;
            cursor.moveToNext();
        }

        cursor.close();

        return xySeries;
    }

    public Cursor getContextValuesCursor()
    {
        return getContextValuesCursor(null, null, ContextValuesTableMetadata.TIMESTAMP + " DESC");
    }

    private Cursor getContextValuesCursor(final String selection, final String [] selectionArguments, final String orderBy)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();

        return database.query(ContextValuesTableMetadata.TABLE_NAME, ContextValuesTableMetadata.ALL_COLUMNS, selection, selectionArguments, null, null, orderBy);
    }
}
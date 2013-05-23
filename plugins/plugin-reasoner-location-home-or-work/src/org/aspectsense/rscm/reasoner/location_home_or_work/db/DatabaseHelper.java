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

package org.aspectsense.rscm.reasoner.location_home_or_work.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.reasoner.location_home_or_work.data.Coordinates;

import static org.aspectsense.rscm.reasoner.location_home_or_work.db.DatabaseMetadata.*;

/**
 * User: Nearchos Paspallis
 * Date: 10/8/11
 * Time: 12:11 PM
 *
 */
public class DatabaseHelper
{
    public static final String TAG = "org.aspectsense.rscm.reasoner.location_home_or_work.db.DatabaseHelper";

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

    public long insert(final long timestamp, final int where, final double lat, final double lng)
    {
        // update database
        final SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();

        database.beginTransaction();

        final ContentValues contentValues = new ContentValues();
        contentValues.put(CoordinatesTableMetadata.TIMESTAMP, timestamp);
        contentValues.put(CoordinatesTableMetadata.WHERE, where);
        contentValues.put(CoordinatesTableMetadata.LATITUDE, lat);
        contentValues.put(CoordinatesTableMetadata.LONGITUDE, lng);

        final long rowId = database.insert(CoordinatesTableMetadata.TABLE_NAME, null, contentValues);
        database.setTransactionSuccessful();

        database.endTransaction();

        return rowId;
    }

    public Coordinates [] getCoordinates(final long fromTimestamp)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        final Cursor cursor = database.query(
                CoordinatesTableMetadata.TABLE_NAME,
                CoordinatesTableMetadata.ALL_COLUMNS,
                CoordinatesTableMetadata.TIMESTAMP + ">=?",
                new String[]{Long.toString(fromTimestamp)},
                null,
                null,
                CoordinatesTableMetadata.TIMESTAMP + " DESC");

        return getCoordinates(cursor);
    }

    public Coordinates [] getCoordinates(final long fromTimestamp, final long toTimestamp)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        final Cursor cursor = database.query(
                CoordinatesTableMetadata.TABLE_NAME,
                CoordinatesTableMetadata.ALL_COLUMNS,
                CoordinatesTableMetadata.TIMESTAMP + ">=? AND " + CoordinatesTableMetadata.TIMESTAMP + "<=?",
                new String[]{Long.toString(fromTimestamp), Long.toString(toTimestamp)},
                null,
                null,
                CoordinatesTableMetadata.TIMESTAMP + " DESC");

        return getCoordinates(cursor);
    }

    private Coordinates [] getCoordinates(final Cursor cursor)
    {
        final int INDEX_TIMESTAMP = cursor.getColumnIndex(CoordinatesTableMetadata.TIMESTAMP);
        final int INDEX_WHERE     = cursor.getColumnIndex(CoordinatesTableMetadata.WHERE);
        final int INDEX_LATITUDE  = cursor.getColumnIndex(CoordinatesTableMetadata.LATITUDE);
        final int INDEX_LONGITUDE = cursor.getColumnIndex(CoordinatesTableMetadata.LONGITUDE);

        final int numOfEntries = cursor.getCount();
        final Coordinates [] coordinates = new Coordinates[numOfEntries];
        cursor.moveToFirst();
        for(int i = 0; i < numOfEntries; i++)
        {
            final long timestamp = cursor.getLong(INDEX_TIMESTAMP);
            final int where      = cursor.getInt(INDEX_WHERE);
            final float lat      = cursor.getFloat(INDEX_LATITUDE);
            final float lng      = cursor.getFloat(INDEX_LONGITUDE);
            coordinates[i] = new Coordinates(timestamp, where, lat, lng);

            cursor.moveToNext();
        }

        cursor.close();

        return coordinates;
    }

    // todo cleanup the database (validity limit set in preferences?)
    public void cleanup(final long cutoffTimestamp)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        database.delete(
                CoordinatesTableMetadata.TABLE_NAME,
                CoordinatesTableMetadata.TIMESTAMP + "<?",
                new String[] {Long.toString(cutoffTimestamp)});
    }
}
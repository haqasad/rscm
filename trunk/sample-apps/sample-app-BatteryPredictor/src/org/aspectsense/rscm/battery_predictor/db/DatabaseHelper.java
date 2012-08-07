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

    public Cursor getContextValuesCursor()
    {
        return getContextValuesCursor(null, null, ContextValuesTableMetadata.TIMESTAMP + " DESC");
    }

    private Cursor getContextValuesCursor(final String selection, final String [] selectionArguments, final String orderBy)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();

        return database.query(ContextValuesTableMetadata.TABLE_NAME, ContextValuesTableMetadata.ALL_COLUMNS, selection, selectionArguments, null, null, orderBy);
    }

//    public Vector<SyndicationFeedOwner> getFeedOwners(final String selection, final String orderBy)
//    {
//        final Vector<SyndicationFeedOwner> selectedFeedOwners = new Vector<SyndicationFeedOwner>();
//
//        final SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();
//        database.beginTransaction();
//
//        final Cursor cursor = database.query(FeedOwnersTableMetadata.TABLE_NAME, // table name
//                FeedOwnersTableMetadata.ALL_COLUMNS, // select columns
//                selection, // selection
//                null, // selection arguments
//                null, // group by
//                null, // having
//                orderBy); // order by
//
//        final int FEED_OWNER_ID_INDEX = cursor.getColumnIndex(FeedOwnersTableMetadata._ID);
//        final int FEED_OWNER_TITLE_INDEX = cursor.getColumnIndex(FeedOwnersTableMetadata.TITLE);
//        final int FEED_OWNER_LINK_INDEX = cursor.getColumnIndex(FeedOwnersTableMetadata.LINK);
//        final int FEED_OWNER_ICON_INDEX = cursor.getColumnIndex(FeedOwnersTableMetadata.ICON);
//        final int FEED_OWNER_ICON_URL_INDEX = cursor.getColumnIndex(FeedOwnersTableMetadata.ICON_URL);
//
//        cursor.moveToFirst();
//
//        final int numOfRecords = cursor.getCount();
//        for (int i = 0; i < numOfRecords; i++)
//        {
//            final long id           = cursor.getLong(FEED_OWNER_ID_INDEX);
//            final String title      = cursor.getString(FEED_OWNER_TITLE_INDEX);
//            final String link       = cursor.getString(FEED_OWNER_LINK_INDEX);
//            final String icon       = cursor.getString(FEED_OWNER_ICON_INDEX);
//            final String icon_url   = cursor.getString(FEED_OWNER_ICON_URL_INDEX);
//
//            selectedFeedOwners.add(new SyndicationFeedOwner(id, title, icon, icon_url, link));
//
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//
//        database.setTransactionSuccessful();
//        database.endTransaction();
//
//        return selectedFeedOwners;
//    }
}
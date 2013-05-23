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

package org.aspectsense.rscm.runtime.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;
import org.aspectsense.rscm.ContextValue;

import static org.aspectsense.rscm.runtime.db.DatabaseMetadata.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Nearchos Paspallis
 * Date: 10/8/11
 * Time: 12:11 PM
 *
 */
public class DatabaseHelper
{
    public static final String TAG = "org.aspectsense.rscm.runtime.db.DatabaseHelper";

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

    private final Map<String, ContextValue> cache = new HashMap<String, ContextValue>();

    public long insert(final ContextValue contextValue)
    {
        final long timestamp = contextValue.getCreationTimestamp();
        final String scope = contextValue.getScope();
        final String valueAsJSON = contextValue.getValueAsJSONString();

        // update cache
        cache.put(scope, contextValue);

        // update database
        final SQLiteDatabase database = databaseOpenHelper.getWritableDatabase();

        database.beginTransaction();

        final ContentValues contentValues = new ContentValues();
        contentValues.put(ContextValuesTableMetadata.TIMESTAMP, timestamp);
        contentValues.put(ContextValuesTableMetadata.SCOPE, scope);
        contentValues.put(ContextValuesTableMetadata.VALUE_AS_JSON, valueAsJSON);

        final long rowId = database.insert(ContextValuesTableMetadata.TABLE_NAME, null, contentValues);
        database.setTransactionSuccessful();

        database.endTransaction();

        return rowId;
    }

    /**
     * Returns the latest {@link ContextValue} (based on the timestamp) for the given scope.
     * This method first checks the cache, and if a value is not available in it, it fetches it from the database (and
     * updates the cache as well).
     *
     * @param scope
     * @return an instance of {@link ContextValue} (NULL if no entry is in the database for the given scope)
     */
    public ContextValue getLatestValue(final String scope)
    {
        final ContextValue cachedContextValue = cache.get(scope);

        if(cachedContextValue != null)
        {
            return cachedContextValue;
        }
        else
        {
            final ContextValue databaseContextValue = getLatestValueFromDatabase(scope);
            if(databaseContextValue != null)
            {
                // update cache
                cache.put(scope, databaseContextValue);
            }

            // this might be null, which signifies that no such value exists in the cache and in the database
            return databaseContextValue;
        }
    }

    private final String SELECT_LAST_SCOPE = "SELECT * FROM " + ContextValuesTableMetadata.TABLE_NAME +
            " WHERE " + ContextValuesTableMetadata.SCOPE + "=? AND " + ContextValuesTableMetadata.TIMESTAMP + " = " +
            "(SELECT MAX(" + ContextValuesTableMetadata.TIMESTAMP + ") FROM " + ContextValuesTableMetadata.TABLE_NAME + " WHERE " + ContextValuesTableMetadata.SCOPE + "=?)";

    /**
     * Returns the latest {@link ContextValue} (based on the timestamp) for the given scope from the database.
     *
     * @param scope
     * @return an instance of {@link ContextValue} (NULL if no entry is in the database for the given scope)
     */
    public ContextValue getLatestValueFromDatabase(final String scope)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        final Cursor cursor = database.rawQuery(SELECT_LAST_SCOPE, new String [] {scope, scope} );

        final int INDEX_TIMESTAMP       = cursor.getColumnIndex(ContextValuesTableMetadata.TIMESTAMP);
        final int INDEX_VALUE_AS_JSON   = cursor.getColumnIndex(ContextValuesTableMetadata.VALUE_AS_JSON);

        final ContextValue contextValue;

        final int numOfEntries = cursor.getCount();
        if(numOfEntries > 0)
        {
            cursor.moveToFirst();
            final long timestamp        = cursor.getLong(INDEX_TIMESTAMP);
            final String valueAsJSON    = cursor.getString(INDEX_VALUE_AS_JSON);

            contextValue = new ContextValue(timestamp, scope, ContextValue.SOURCE_PACKAGE_NAME_UNKNOWN, valueAsJSON);
        }
        else
        {
            contextValue = null;
        }

        cursor.close();

        return contextValue;
    }

    public ContextValue [] getAllValues(final String scope)
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        final Cursor cursor = database.query(
                ContextValuesTableMetadata.TABLE_NAME,
                new String[]{ContextValuesTableMetadata.TIMESTAMP, ContextValuesTableMetadata.VALUE_AS_JSON},
                ContextValuesTableMetadata.SCOPE + "=?",
                new String[]{scope},
                null,
                null,
                ContextValuesTableMetadata.TIMESTAMP + " DESC");

        final int INDEX_TIMESTAMP       = cursor.getColumnIndex(ContextValuesTableMetadata.TIMESTAMP);
        final int INDEX_VALUE_AS_JSON   = cursor.getColumnIndex(ContextValuesTableMetadata.VALUE_AS_JSON);

        final int numOfEntries = cursor.getCount();
        final ContextValue [] contextValues = new ContextValue[numOfEntries];
        cursor.moveToFirst();
        for(int i = 0; i < numOfEntries; i++)
        {
            final long timestamp     = cursor.getLong(INDEX_TIMESTAMP);
            final String valueAsJSON = cursor.getString(INDEX_VALUE_AS_JSON);
            contextValues[i] = new ContextValue(timestamp, scope, ContextValue.SOURCE_PACKAGE_NAME_UNKNOWN, valueAsJSON);

            cursor.moveToNext();
        }

        cursor.close();

        return contextValues;
    }

    public String [] getDistinctScopes()
    {
        final SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        final Cursor cursor = database.query(
                true,
                ContextValuesTableMetadata.TABLE_NAME,
                new String [] {ContextValuesTableMetadata.SCOPE},
                null,
                null,
                null,
                null,
                null,
                null);

        final int INDEX_SCOPE = cursor.getColumnIndex(ContextValuesTableMetadata.SCOPE);

        final int numOfEntries = cursor.getCount();
        final String [] scopes = new String[numOfEntries];

        cursor.moveToFirst();
        for(int i = 0; i < numOfEntries; i++)
        {
            scopes[i] = cursor.getString(INDEX_SCOPE);
            cursor.moveToNext();
        }

        cursor.close();

        return scopes;
    }

    public void cleanup()
    {
        // todo cleanup the database (validity limit set in preferences?)
    }
}
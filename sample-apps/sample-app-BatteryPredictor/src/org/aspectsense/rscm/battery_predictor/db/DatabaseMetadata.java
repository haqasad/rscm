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

import android.provider.BaseColumns;

/**
 * User: Nearchos Paspallis
 * Date: 10/7/11
 * Time: 6:47 PM
 */
public class DatabaseMetadata
{
    public static final String DATABASE_NAME = "data.db";

    public static final int DATABASE_VERSION = 12080702;

    public static final class ContextValuesTableMetadata implements BaseColumns
    {
        private ContextValuesTableMetadata() { /* empty */ }

        public static final int POWER_CONNECTED_FALSE   = 0;
        public static final int POWER_CONNECTED_TRUE    = 1;

        public static final String TABLE_NAME = "context_values";

        // column names
        public static final String TIMESTAMP        = "timestamp";
        public static final String BATTERY_LEVEL    = "battery_level";
        public static final String POWER_CONNECTED  = "power_connected";
        public static final String LATITUDE         = "lat";
        public static final String LONGITUDE        = "lng";

        public static final String [] ALL_COLUMNS = {
                _ID,                // primary key
                TIMESTAMP,          // long
                BATTERY_LEVEL,      // int [0-100]
                POWER_CONNECTED,    // int [0-1]
                LATITUDE,           // real
                LONGITUDE           // real
        };
    }
}
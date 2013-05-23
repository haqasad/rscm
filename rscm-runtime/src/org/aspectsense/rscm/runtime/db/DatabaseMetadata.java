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

import android.provider.BaseColumns;

/**
 * User: Nearchos Paspallis
 * Date: 10/7/11
 * Time: 6:47 PM
 */
public class DatabaseMetadata
{
    public static final String DATABASE_NAME = "context.db";

    public static final int DATABASE_VERSION = 13042901;

    public static final class ContextValuesTableMetadata implements BaseColumns
    {
        private ContextValuesTableMetadata() { /* empty */ }

        public static final String TABLE_NAME = "context_values";

        // column names
        public static final String TIMESTAMP            = "timestamp";
        public static final String SCOPE                = "scope";
        public static final String VALUE_AS_JSON        = "value_as_json";

        public static final String [] ALL_COLUMNS = {
                _ID,                 // primary key
                TIMESTAMP,           // long
                SCOPE,               // string
                VALUE_AS_JSON        // string
        };
    }
}
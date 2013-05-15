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

package org.aspectsense.rscm;

import android.os.Parcel;
import android.os.Parcelable;
import org.aspectsense.rscm.context.model.IContextValue;
import org.aspectsense.rscm.context.plugin.SensorService;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/7/12
 *         Time: 8:05 PM
 */
public class ContextValue implements IContextValue, Parcelable
{
    public static final String SOURCE_PACKAGE_NAME_UNKNOWN = "unknown_package_name";

    private final long creationTimestamp;
    private final String scope;
    private final String sourcePackageName;
    private final String valueAsJSONString;

    public ContextValue(final String scope, final String valueAsJSONString)
    {
        this(new Date().getTime(), scope, SensorService.PACKAGE_NAME, valueAsJSONString);
    }

    public ContextValue(final long creationTimestamp, final String scope, final String sourcePackageName, final String valueAsJSONString)
    {
        this.creationTimestamp = creationTimestamp;
        this.scope = scope;
        this.sourcePackageName = sourcePackageName;
        this.valueAsJSONString = valueAsJSONString;
    }

    static public ContextValue createContextValue(final String scope, final boolean value)
    {
        return new ContextValue(scope, "{ value: " + value + " }");
    }

    static public ContextValue createContextValue(final String scope, final int value)
    {
        return new ContextValue(scope, "{ value: " + Integer.toString(value) + " }");
    }

    static public ContextValue createContextValue(final String scope, final long value)
    {
        return new ContextValue(scope, "{ value: " + Long.toString(value) + " }");
    }

    static public ContextValue createContextValue(final String scope, final double value)
    {
        return new ContextValue(scope, "{ value: " + Double.toString(value) + " }");
    }

    static public ContextValue createContextValue(final String scope, final String value)
    {
        return new ContextValue(scope, "{ value: \"" + value + "\" }");
    }

    @Override public long getCreationTimestamp()
    {
        return creationTimestamp;
    }

    @Override public String getScope()
    {
        return scope;
    }

    public String getSourcePackageName()
    {
        return sourcePackageName;
    }

    @Override public String getValueAsJSONString()
    {
        return valueAsJSONString;
    }

    public boolean getValueAsBoolean() throws JSONException
    {
        return new JSONObject(valueAsJSONString).getBoolean("value");
    }

    public int getValueAsInteger() throws JSONException
    {
        return new JSONObject(valueAsJSONString).getInt("value");
    }

    public long getValueAsLong() throws JSONException
    {
        return new JSONObject(valueAsJSONString).getLong("value");
    }

    public double getValueAsDouble() throws JSONException
    {
        return new JSONObject(valueAsJSONString).getDouble("value");
    }

    public String getValueAsString() throws JSONException
    {
        return new JSONObject(valueAsJSONString).getString("value");
    }

    private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    @Override public String toString()
    {
        return getScope() + "@" + sourcePackageName + "(" + DEFAULT_DATE_FORMAT.format(new Date(creationTimestamp)) + ")::" + valueAsJSONString;
    }

    // ------------------------------------------- Implement Parcelable --------------------------------------------- //

    @Override public int describeContents()
    {
        return 0;
    }

    public static final Creator<ContextValue> CREATOR = new Creator<ContextValue>()
    {
        @Override public ContextValue createFromParcel(Parcel in)
        {
            final long creationTimestamp = in.readLong();
            final String scope = in.readString();
            final String sourcePackageName = in.readString();
            final String valueAsJSONString = in.readString();
            return new ContextValue(creationTimestamp, scope, sourcePackageName, valueAsJSONString);
        }

        @Override public ContextValue[] newArray(int size)
        {
            return new ContextValue[size];
        }
    };

    @Override public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeLong(creationTimestamp);
        parcel.writeString(scope);
        parcel.writeString(sourcePackageName);
        parcel.writeString(valueAsJSONString);
    }

    // ---------------------------------------- End of Implement Parcelable ----------------------------------------- //
}
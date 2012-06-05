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

package org.aspectsense.rscm;

import android.os.Parcel;
import android.os.Parcelable;
import org.aspectsense.rscm.context.plugin.IPluginRecord;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 4/14/12
 * Time: 6:54 PM
 */
public class PluginRecord implements Parcelable, IPluginRecord
{
    private final String packageName;
    private final String category;
    private final String [] requestedPermissions;
    private final String [] providedScopes;
    private final String [] requiredScopes;
    private final Map<String,String> metadata;

    public PluginRecord(final String packageName,
                        final String category,
                        final String [] requestedPermissions,
                        final String [] providedScopes,
                        final String [] requiredScopes,
                        final Map<String,String> metadata)
    {
        this.packageName = packageName;
        this.category = category;
        this.requestedPermissions = requestedPermissions;
        this.providedScopes = providedScopes;
        this.requiredScopes = requiredScopes;
        this.metadata = metadata;
    }

    public String getPackageName()
    {
        return packageName;
    }

    @Override public String getCategory()
    {
        return category;
    }

    public String [] getRequiredPermissions()
    {
        return requestedPermissions;
    }

    public String [] getProvidedScopes()
    {
        return providedScopes;
    }

    public boolean hasProvidedScope(final String scope)
    {
        for(final String providedScope : providedScopes)
        {
            if(providedScope.equals(scope)) return true;
        }

        return false;
    }

    public String [] getRequiredScopes()
    {
        return requiredScopes;
    }

    public Map<String,String> getMetadata()
    {
        return metadata;
    }

    @Override public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PluginRecord that = (PluginRecord) o;

        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
        {
            return false;
        }
        if (category != null ? !category.equals(that.category) : that.category != null)
        {
            return false;
        }
        if (!Arrays.equals(requestedPermissions, that.requestedPermissions))
        {
            return false;
        }
        if (!Arrays.equals(providedScopes, that.providedScopes))
        {
            return false;
        }
        if (!Arrays.equals(requiredScopes, that.requiredScopes))
        {
            return false;
        }
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null)
        {
            return false;
        }

        return true;
    }

    @Override public int hashCode()
    {
        int result = packageName != null ? packageName.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (requestedPermissions != null ? Arrays.hashCode(requestedPermissions) : 0);
        result = 31 * result + (providedScopes != null ? Arrays.hashCode(providedScopes) : 0);
        result = 31 * result + (requiredScopes != null ? Arrays.hashCode(requiredScopes) : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "PluginRecord{" +
                "packageName='" + packageName + '\'' +
                ", category='" + category + '\'' +
                ", requestedPermissions=" + (requestedPermissions == null ? null : Arrays.asList(requestedPermissions)) +
                ", providedScopes=" + (providedScopes == null ? null : Arrays.asList(providedScopes)) +
                ", requiredScopes=" + (requiredScopes == null ? null : Arrays.asList(requiredScopes)) +
                ", metadata=" + metadata +
                '}';
    }

    // ------------------------------------------- Implement Parcelable --------------------------------------------- //

    @Override public int describeContents()
    {
        return 0;
    }

    public static final Creator<PluginRecord> CREATOR = new Creator<PluginRecord>()
    {
        @Override public PluginRecord createFromParcel(Parcel in)
        {
            final String packageName = in.readString();
            final String category = in.readString();

            final int numOfRequestedPermissions = in.readInt();
            final String [] requestedPermissionsArray = new String[numOfRequestedPermissions];
            in.readStringArray(requestedPermissionsArray);

            final int numOfProvidedScopes = in.readInt();
            final String [] providedScopesArray = new String[numOfProvidedScopes];
            in.readStringArray(providedScopesArray);

            final int numOfRequiredScopes = in.readInt();
            final String [] requiredScopesArray = new String[numOfRequiredScopes];
            in.readStringArray(requiredScopesArray);

            final Map<String,String> metadata = new HashMap<String, String>();
            in.readMap(metadata, null);

            return new PluginRecord(packageName, category, requestedPermissionsArray, providedScopesArray, requiredScopesArray, metadata);
        }

        @Override public PluginRecord [] newArray(int size)
        {
            return new PluginRecord[size];
        }
    };

    @Override public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(packageName);
        parcel.writeString(category);
        parcel.writeInt(requestedPermissions.length);
        parcel.writeStringArray(requestedPermissions);
        parcel.writeInt(providedScopes.length);
        parcel.writeStringArray(providedScopes);
        parcel.writeInt(requiredScopes.length);
        parcel.writeStringArray(requiredScopes);
        parcel.writeMap(metadata);
    }

    // ---------------------------------------- End of Implement Parcelable ----------------------------------------- //
}
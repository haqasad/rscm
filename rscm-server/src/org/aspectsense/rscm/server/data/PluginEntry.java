package org.aspectsense.rscm.server.data;

import java.io.Serializable;

/**
 * User: Nearchos Paspallis
 * Date: 5/7/13
 * Time: 11:03 AM
 */
public class PluginEntry implements Serializable
{
    private final String uuid;
    private final String packageId;
    private final String creatorEmail;
    private final String description;
    private final String developerUrl;
    private final String providedScopes;
    private final String requiredScopes;
    private final String permissions;
    private final boolean isDeprecated; // true iff deleted from the user
    private final long uploaded;
    private final long lastUpdated;

    PluginEntry(final String uuid,
                final String packageId,
                final String creatorEmail,
                final String description,
                final String developerUrl,
                final String providedScopes,
                final String requiredScopes,
                final String permissions,
                final boolean isDeprecated,
                final long uploaded,
                final long lastUpdated)
    {
        this.uuid = uuid;
        this.packageId = packageId;
        this.creatorEmail = creatorEmail;
        this.description = description;
        this.developerUrl = developerUrl;
        this.providedScopes = providedScopes;
        this.requiredScopes = requiredScopes;
        this.permissions = permissions;
        this.isDeprecated = isDeprecated;
        this.uploaded = uploaded;
        this.lastUpdated = lastUpdated;
    }

    public String getUUID()
    {
        return uuid;
    }

    public String getPackageId()
    {
        return packageId;
    }

    public String getCreatorEmail()
    {
        return creatorEmail;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDeveloperUrl()
    {
        return developerUrl;
    }

    public String getProvidedScopes()
    {
        return providedScopes;
    }

    public String getRequiredScopes()
    {
        return requiredScopes;
    }

    public String getPermissions()
    {
        return permissions;
    }

    public boolean isDeprecated()
    {
        return isDeprecated;
    }

    public long getUploaded()
    {
        return uploaded;
    }

    public long getLastUpdated()
    {
        return lastUpdated;
    }
}
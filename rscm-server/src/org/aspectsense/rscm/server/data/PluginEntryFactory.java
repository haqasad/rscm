package org.aspectsense.rscm.server.data;

import com.google.appengine.api.datastore.*;

import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Nearchos Paspallis
 * Date: 5/7/13
 * Time: 11:02 AM
 */
public class PluginEntryFactory
{
    public static final Logger log = Logger.getLogger(PluginEntryFactory.class.getCanonicalName());

    public static final String KEY = "key";
    public static final String KIND = "PluginEntry";

    public static final String PROPERTY_PACKAGE_ID      = "package_id";
    public static final String PROPERTY_CREATOR_EMAIL   = "creator_email";
    public static final String PROPERTY_DESCRIPTION     = "description";
    public static final String PROPERTY_DEVELOPER_URL   = "developer_url";
    public static final String PROPERTY_PROVIDED_SCOPES = "provided_scopes";
    public static final String PROPERTY_REQUIRED_SCOPES = "required_scopes";
    public static final String PROPERTY_PERMISSIONS     = "permissions";
    public static final String PROPERTY_IS_DEPRECATED   = "is_deprecated";
    public static final String PROPERTY_UPLOADED        = "uploaded";
    public static final String PROPERTY_LAST_UPDATED    = "last_updated";

    static public PluginEntry getPluginEntry(final String keyAsString)
    {
        final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        try
        {
            final Entity pluginEntryEntity = datastoreService.get(KeyFactory.stringToKey(keyAsString));

            return getFromEntity(pluginEntryEntity);
        }
        catch (EntityNotFoundException enfe)
        {
            log.severe("Could not find " + KIND + " with key: " + keyAsString);

            return null;
        }
    }

    public static final String ALL_PLUGIN_ENTRIES = "all-plugin-entries";

    static public Vector<PluginEntry> getAllPluginEntries()
    {
        final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        final Query query = new Query(KIND).addSort(PROPERTY_UPLOADED);
        final PreparedQuery preparedQuery = datastoreService.prepare(query);
        final Vector<PluginEntry> pluginEntries = new Vector<PluginEntry>();
        for(final Entity entity : preparedQuery.asIterable())
        {
            pluginEntries.add(getFromEntity(entity));
        }

        return pluginEntries;
    }

    static public Key addPluginEntry(final String packageId,
                                     final String creatorEmail,
                                     final String description,
                                     final String developerUrl,
                                     final String providedScopes,
                                     final String requiredScopes,
                                     final String permissions,
                                     final boolean isDeprecated)
    {
        final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        final Entity pluginEntryEntity = new Entity(KIND);

        pluginEntryEntity.setProperty(PROPERTY_PACKAGE_ID, packageId);
        pluginEntryEntity.setProperty(PROPERTY_CREATOR_EMAIL, creatorEmail);
        pluginEntryEntity.setProperty(PROPERTY_DESCRIPTION, description);
        pluginEntryEntity.setProperty(PROPERTY_DEVELOPER_URL, developerUrl);
        pluginEntryEntity.setProperty(PROPERTY_PROVIDED_SCOPES, providedScopes);
        pluginEntryEntity.setProperty(PROPERTY_REQUIRED_SCOPES, requiredScopes);
        pluginEntryEntity.setProperty(PROPERTY_PERMISSIONS, permissions);
        pluginEntryEntity.setProperty(PROPERTY_IS_DEPRECATED, isDeprecated);
        final long lastUpdated = System.currentTimeMillis();
        pluginEntryEntity.setProperty(PROPERTY_UPLOADED, lastUpdated);
        pluginEntryEntity.setProperty(PROPERTY_LAST_UPDATED, lastUpdated);

        return datastoreService.put(pluginEntryEntity);
    }

    static public Key editPluginEntry(final String uuid,
                                      final String description,
                                      final String developerUrl,
                                      final String providedScopes,
                                      final String requiredScopes,
                                      final String permissions,
                                      final boolean isDeprecated)
            throws EntityNotFoundException
    {
        final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        final Entity pluginEntryEntity = datastoreService.get(KeyFactory.stringToKey(uuid));

        pluginEntryEntity.setProperty(PROPERTY_DESCRIPTION, description);
        pluginEntryEntity.setProperty(PROPERTY_DEVELOPER_URL, developerUrl);
        pluginEntryEntity.setProperty(PROPERTY_PROVIDED_SCOPES, providedScopes);
        pluginEntryEntity.setProperty(PROPERTY_REQUIRED_SCOPES, requiredScopes);
        pluginEntryEntity.setProperty(PROPERTY_PERMISSIONS, permissions);
        pluginEntryEntity.setProperty(PROPERTY_IS_DEPRECATED, isDeprecated);
        final long lastUpdated = System.currentTimeMillis();
        pluginEntryEntity.setProperty(PROPERTY_LAST_UPDATED, lastUpdated);

        return datastoreService.put(pluginEntryEntity);
    }

    static public PluginEntry getFromEntity(final Entity entity)
    {
        return new PluginEntry(
                KeyFactory.keyToString(entity.getKey()),
                (String) entity.getProperty(PROPERTY_PACKAGE_ID),
                (String) entity.getProperty(PROPERTY_CREATOR_EMAIL),
                (String) entity.getProperty(PROPERTY_DESCRIPTION),
                (String) entity.getProperty(PROPERTY_DEVELOPER_URL),
                (String) entity.getProperty(PROPERTY_PROVIDED_SCOPES),
                (String) entity.getProperty(PROPERTY_REQUIRED_SCOPES),
                (String) entity.getProperty(PROPERTY_PERMISSIONS),
                (Boolean) entity.getProperty(PROPERTY_IS_DEPRECATED),
                (Long) entity.getProperty(PROPERTY_UPLOADED),
                (Long) entity.getProperty(PROPERTY_LAST_UPDATED));
    }
}
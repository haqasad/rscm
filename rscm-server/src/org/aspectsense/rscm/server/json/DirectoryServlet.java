package org.aspectsense.rscm.server.json;

import com.google.appengine.api.datastore.*;
import org.aspectsense.rscm.server.data.PluginEntry;
import org.aspectsense.rscm.server.data.PluginEntryFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * User: Nearchos Paspallis
 * Date: 5/6/13
 * Time: 8:27 PM
 */
public class DirectoryServlet extends javax.servlet.http.HttpServlet
{
    public static final String MAGIC = "";

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException
    {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        final StringBuilder stringBuilder = new StringBuilder();
        final String result;

        final String fromS = request.getParameter("from");
        long from = 0L;
        try
        {
            from = fromS == null ? 0L : Long.parseLong(fromS);
        }
        catch (NumberFormatException nfe)
        {
            log("Error parsing 'from' argument: " + fromS, nfe);
        }

        final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        stringBuilder
                .append("{\n")
                .append("  \"status\": \"ok\",\n");

        long lastUpdated = 0;

        lastUpdated = Math.max(lastUpdated, appendAvailablePlugins(datastore, stringBuilder, from));

        stringBuilder
                .append("  \"lastUpdated\": " + lastUpdated + "\n")
                .append("}");

        result = stringBuilder.toString();

        final PrintWriter printWriter = response.getWriter();

        printWriter.println(result);
    }

    private long appendAvailablePlugins(final DatastoreService datastore, final StringBuilder stringBuilder, final long from)
    {
        final Query.Filter filterFrom = new Query.FilterPredicate(
                PluginEntryFactory.PROPERTY_LAST_UPDATED,
                Query.FilterOperator.GREATER_THAN,
                from);

        final Query query = new Query(PluginEntryFactory.KIND);
        // filter cities last updated after "from"
        query.setFilter(filterFrom);
        final PreparedQuery preparedQuery = datastore.prepare(query);
        final Iterable<Entity> iterable = preparedQuery.asIterable();
        final Iterator<Entity> iterator = iterable.iterator();

        stringBuilder.append("  \"pluginEntries\": [\n");

        long lastUpdated = 0L;

        while(iterator.hasNext())
        {
            final Entity entity = iterator.next();
            final PluginEntry pluginEntry = PluginEntryFactory.getFromEntity(entity);

            stringBuilder.append(
                    "    {\n" +
                            "      \"UUID\": \"" + pluginEntry.getUUID() + "\",\n" +
                            "      \"packageId\": \"" + pluginEntry.getPackageId() + "\",\n" +
                            "      \"description\": \"" + pluginEntry.getDescription() + "\",\n" +
                            "      \"developerUrl\": \"" + pluginEntry.getDeveloperUrl() + "\",\n" +
                            "      \"downloadUrl\": \"" + pluginEntry.getDownloadUrl() + "\",\n" +
                            "      \"providedScopes\": " + asJsonArray(pluginEntry.getProvidedScopes()) + ",\n" +
                            "      \"requiredScopes\": " + asJsonArray(pluginEntry.getRequiredScopes()) + ",\n" +
                            "      \"permissions\": " + asJsonArray(pluginEntry.getPermissions()) + ",\n" +
                            "      \"isDeprecated\": " + pluginEntry.isDeprecated() + ",\n" +
                            "      \"lastUpdated\": " + pluginEntry.getLastUpdated() + ",\n" +
                            "      \"uploaded\": " + pluginEntry.getUploaded() + "\n" +
                            "    }" + (iterator.hasNext() ? ",\n\n" : "\n")
            );

            lastUpdated = Math.max(lastUpdated, pluginEntry.getLastUpdated());
        }

        stringBuilder.append("  ],\n\n");

        return lastUpdated;
    }

    private String asJsonArray(final String commaSeparatedList)
    {
        if(commaSeparatedList == null || commaSeparatedList.isEmpty())
        {
            return "[]"; // empty JSON array
        }
        else
        {
            final StringTokenizer stringTokenizer = new StringTokenizer(commaSeparatedList, ",");
            final StringBuilder stringBuilder = new StringBuilder("[");
            final int count = stringTokenizer.countTokens();
            for(int i = 0; i < count; i++)
            {
                stringBuilder.append("\"").append(stringTokenizer.nextToken()).append("\"");
                if(i < count-1) stringBuilder.append(",");
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }
}
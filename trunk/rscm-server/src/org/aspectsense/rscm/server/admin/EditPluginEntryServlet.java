package org.aspectsense.rscm.server.admin;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.aspectsense.rscm.server.data.PluginEntry;
import org.aspectsense.rscm.server.data.PluginEntryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: Nearchos Paspallis
 * Date: 11/21/12
 * Time: 1:04 PM
 */
public class EditPluginEntryServlet extends HttpServlet
{
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();

        response.setContentType("text/html");

        if(user == null)
        {
            response.getWriter().print("<p>You need to <a href=\"" + userService.createLoginURL(request.getRequestURI()) + "\">sign in</a> to use this service.</p>");
        }
        else
        {
            final String userEmail = user.getEmail();

            final String uuid = request.getParameter(PluginEntryFactory.KEY);
log(getClass() + ": uuid: " + uuid);//todo delete

            final String packageID = request.getParameter(PluginEntryFactory.PROPERTY_PACKAGE_ID);
            final String description = request.getParameter(PluginEntryFactory.PROPERTY_DESCRIPTION);
            final String developerURL = request.getParameter(PluginEntryFactory.PROPERTY_DEVELOPER_URL);
            final String providedScopes = request.getParameter(PluginEntryFactory.PROPERTY_PROVIDED_SCOPES);
            final String requiredScopes = request.getParameter(PluginEntryFactory.PROPERTY_REQUIRED_SCOPES);
            final String permissionsS = request.getParameter(PluginEntryFactory.PROPERTY_PERMISSIONS);
            final boolean isDeprecated = Boolean.parseBoolean(request.getParameter(PluginEntryFactory.PROPERTY_IS_DEPRECATED));

            // if uuid is not empty, then EDIT, else ADD
            if(uuid != null && !uuid.isEmpty())
            {
                // edit rather than add

                final PluginEntry pluginEntry = PluginEntryFactory.getPluginEntry(uuid);
                final String originalCreatorEmail = pluginEntry == null ? null : pluginEntry.getCreatorEmail();
                if(!userEmail.equalsIgnoreCase(originalCreatorEmail))
                {
                    response.getWriter().print("You cannot edit a plugin created by someone else");
                }
                else
                {
                    try
                    {
                        PluginEntryFactory.editPluginEntry(
                                uuid,
                                description,
                                developerURL,
                                providedScopes,
                                requiredScopes,
                                permissionsS,
                                isDeprecated);

                        log("Edited " + PluginEntryFactory.KIND + " with key: " + uuid);

                        response.sendRedirect("/directory#" + uuid);
                    }
                    catch (EntityNotFoundException enfe)
                    {
                        response.getWriter().println("<h1>Error while editing pharmacy with UUID: " + uuid + "</h1><p>" + Arrays.toString(enfe.getStackTrace()) + "</p>");
                    }
                }
            }
            else
            {
                // add
                final Key key = PluginEntryFactory.addPluginEntry(
                        packageID,
                        userEmail,
                        description,
                        developerURL,
                        providedScopes,
                        requiredScopes,
                        permissionsS,
                        isDeprecated);

                log("Added " + PluginEntryFactory.KIND + " with key: " + key);

                response.sendRedirect("/directory#" + KeyFactory.keyToString(key));
            }
        }
    }
}
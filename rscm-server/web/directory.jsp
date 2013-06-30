<%@ page import="java.util.Vector" %>
<%@ page import="org.aspectsense.rscm.server.data.PluginEntry" %>
<%@ page import="org.aspectsense.rscm.server.data.PluginEntryFactory" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%--
  Date: 2013-05-07
  Time: 5:53 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

    <head>
        <title>Really Simple Context Middleware (RSCM) - Directory of plugins</title>
    </head>

  <body>

    <img src="rscm_240x100.png" border="0" alt="RSCM logo"/>
    <h1>Directory of plugins</h1>

<%
    final UserService userService = UserServiceFactory.getUserService();
    final User user = userService.getCurrentUser();
    if (user == null)
    {
%>
    <p>You need to <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">sign in</a> to use this service.</p>
<%
    }
    else
    {
%>
    <p>Logged in as: <%= user.getEmail() %> [<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>]</p>
<%
    }

    final Vector<PluginEntry> pluginEntries = PluginEntryFactory.getAllPluginEntries();
%>

    <p>Number of plugins: <%= pluginEntries.size() %></p>

<%
    for(final PluginEntry pluginEntry : pluginEntries)
    {
        final String uuid = pluginEntry.getUUID();
        final String packageId = pluginEntry.getPackageId();
        final String developerUrl = pluginEntry.getDeveloperUrl();
%>

    <div id="<%=uuid%>">
        <h2>Package ID: <%= packageId %></h2>
        <p>Description: <%= pluginEntry.getDescription() %></p>
        <p>Developer site: <a target="_blank" href="<%= developerUrl %>"><%= developerUrl %></a></p>
        <p>Provided scopes:</p>
        <ul>
<%
        {
            final String providedScopesS = pluginEntry.getProvidedScopes();
            final StringTokenizer stringTokenizer = new StringTokenizer(providedScopesS, ",");
            while(stringTokenizer.hasMoreTokens())
            {
%>
            <li><%=stringTokenizer.nextToken()%></li>
<%
            }
        }
%>
        </ul>
        <p>Required scopes:</p>
        <ul>
<%
        {
            final String requiredScopesS = pluginEntry.getRequiredScopes();
            final StringTokenizer stringTokenizer = new StringTokenizer(requiredScopesS, ",");
            while(stringTokenizer.hasMoreTokens())
            {
%>
            <li><%=stringTokenizer.nextToken()%></li>
<%
            }
        }
%>
        </ul>
        <p>Permissions:</p>
        <ul>
<%
        {
            final String permissionsS = pluginEntry.getPermissions();
            final StringTokenizer stringTokenizer = new StringTokenizer(permissionsS, ",");
            while(stringTokenizer.hasMoreTokens())
            {
%>
            <li><%=stringTokenizer.nextToken()%></li>
<%
            }
        }
%>
        </ul>
        <p><%= pluginEntry.isDeprecated() %></p>
        <p>Uploaded: <%= new Date(pluginEntry.getUploaded()) %></p>
        <p>Last updated: <%= new Date(pluginEntry.getLastUpdated()) %></p>

<%
        if(user != null && user.getEmail().equalsIgnoreCase(pluginEntry.getCreatorEmail()))
        {
%>
        <p><a href="edit-plugin?key=<%=pluginEntry.getUUID()%>">Edit plugin entry</a></p>
<%
        }
%>
    </div>
<%
    }
%>

  </body>

</html>
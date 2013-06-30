<%@ page import="org.aspectsense.rscm.server.data.PluginEntryFactory" %>
<%@ page import="org.aspectsense.rscm.server.data.PluginEntry" %>
<%--
  User: Nearchos Paspallis
  Date: 5/7/13
  Time: 7:01 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

  <head>
    <title>Really Simple Context Middleware (RSCM) - Edit plugin</title>
  </head>

  <body>

<%
    final String keyAsString = request.getParameter(PluginEntryFactory.KEY);
    final PluginEntry pluginEntry = PluginEntryFactory.getPluginEntry(keyAsString);
%>
    <img src="rscm_240x100.png" border="0" alt="RSCM logo"/>
    <h1>Edit plugin</h1>

    <form action="admin/modify-plugin" method="post">
        <input type="hidden" name="<%=PluginEntryFactory.KEY%>" value="<%=keyAsString%>" />
        <table>
            <tr>
                <td><b>Package ID</b></td>
                <td><input type="text" name="<%= PluginEntryFactory.PROPERTY_PACKAGE_ID %>" value="<%=pluginEntry.getPackageId()%>" /></td>
            </tr>
            <tr>
                <td><b>Description</b></td>
                <td><textarea style="width: 400px; height: 100px;" name="<%= PluginEntryFactory.PROPERTY_DESCRIPTION %>" ><%=pluginEntry.getDescription()%></textarea></td>
            </tr>
            <tr>
                <td><b>Developer URL</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_DEVELOPER_URL%>" value="<%=pluginEntry.getDeveloperUrl()%>"/></td>
            </tr>
            <tr>
                <td><b>Provided Scopes (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_PROVIDED_SCOPES %>" value="<%=pluginEntry.getProvidedScopes()%>"/></td>
            </tr>
            <tr>
                <td><b>Required Scopes (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_REQUIRED_SCOPES %>" value="<%=pluginEntry.getRequiredScopes()%>"/></td>
            </tr>
            <tr>
                <td><b>Permissions (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_PERMISSIONS %>" value="<%=pluginEntry.getPermissions()%>"/></td>
            </tr>
        </table>
        <div><input type="submit" value="Edit Plugin entry" /></div>
    </form>

  </body>

</html>
<%@ page import="org.aspectsense.rscm.server.data.PluginEntryFactory" %>
<%--
  User: Nearchos Paspallis
  Date: 5/7/13
  Time: 7:01 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

  <head>
    <title>Really Simple Context Middleware (RSCM) - Add plugin</title>
  </head>

  <body>

    <img src="rscm_240x100.png" border="0" alt="RSCM logo"/>
    <h1>Add plugin</h1>

    <form action="admin/modify-plugin" method="post">
        <table>
            <tr>
                <td><b>Package ID</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_PACKAGE_ID %>" /></td>
            </tr>
            <tr>
                <td><b>Description</b></td>
                <td><textarea style="width: 400px; height: 100px;" name="<%= PluginEntryFactory.PROPERTY_DESCRIPTION %>"></textarea></td>
            </tr>
            <tr>
                <td><b>Developer URL</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_DEVELOPER_URL%>" /></td>
            </tr>
            <tr>
                <td><b>Download URL</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_DOWNLOAD_URL%>" /></td>
            </tr>
            <tr>
                <td><b>Provided Scopes (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_PROVIDED_SCOPES %>" /></td>
            </tr>
            <tr>
                <td><b>Required Scopes (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_REQUIRED_SCOPES %>" /></td>
            </tr>
            <tr>
                <td><b>Permissions (comma separated list)</b></td>
                <td><input style="width: 400px;" type="text" name="<%= PluginEntryFactory.PROPERTY_PERMISSIONS %>" /></td>
            </tr>
        </table>
        <div><input type="submit" value="Add Plugin entry" /></div>
    </form>

  </body>

</html>
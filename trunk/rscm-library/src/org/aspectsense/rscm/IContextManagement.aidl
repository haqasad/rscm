package org.aspectsense.rscm;

import org.aspectsense.rscm.IContextManagementListener;
import org.aspectsense.rscm.PluginRecord;

interface IContextManagement
{
    boolean isActive(in String scope);

    boolean isResolved(in String scope);

    boolean isInstalled(in String scope);

    List<PluginRecord> getInstalledPlugins();

    void requestContextManagementUpdates(in IContextManagementListener contextManagementListener);

    void removeContextManagementUpdates(in IContextManagementListener contextManagementListener);

    void installPackage(in String packagePath);

    void uninstallPackage(in String packagePath);
}
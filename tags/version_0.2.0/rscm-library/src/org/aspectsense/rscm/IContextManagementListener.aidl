package org.aspectsense.rscm;

import org.aspectsense.rscm.PluginRecord;

oneway interface IContextManagementListener
{
    void onPluginInstalled(in PluginRecord pluginRecord);

    void onPluginUninstalled(in PluginRecord pluginRecord);
}
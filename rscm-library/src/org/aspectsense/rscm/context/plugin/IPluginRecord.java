package org.aspectsense.rscm.context.plugin;

import android.os.Bundle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Date: 4/14/12
 * Time: 8:32 PM
 */
public interface IPluginRecord extends Serializable
{
    public String getPackageName();

    public String getCategory();

    public String [] getRequiredPermissions();

    public String [] getProvidedScopes();

    public String [] getRequiredScopes();

    public Map<String,String> getMetadata();
}

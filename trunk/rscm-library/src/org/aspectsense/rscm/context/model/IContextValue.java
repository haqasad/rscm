package org.aspectsense.rscm.context.model;

import java.io.Serializable;

/**
 * @author Nearchos Paspallis (nearchos@aspectsense.com)
 *         Date: 3/7/12
 *         Time: 7:25 PM
 */
public interface IContextValue extends Serializable
{
    public long getCreationTimestamp();

    public String getScope();

    public String getSourcePackageName();

    public String getValueAsJSONString();
}
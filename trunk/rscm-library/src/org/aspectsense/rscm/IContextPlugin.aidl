package org.aspectsense.rscm;

import org.aspectsense.rscm.IContextListener;
import org.aspectsense.rscm.ContextValue;

interface IContextPlugin
{
    void setContextListener(in IContextListener contextListener);

    void unsetContextListener();
}
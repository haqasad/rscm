package org.aspectsense.rscm;

import org.aspectsense.rscm.IContextListener;
import org.aspectsense.rscm.ContextValue;

interface IContextAccess
{
    void requestContextUpdates(in String scope, in IContextListener contextListener);

    void removeContextUpdates(in String scope, in IContextListener contextListener);

    ContextValue getLastContextValue(in String scope);
}
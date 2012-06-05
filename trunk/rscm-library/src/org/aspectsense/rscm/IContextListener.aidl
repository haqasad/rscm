package org.aspectsense.rscm;

import org.aspectsense.rscm.ContextValue;

oneway interface IContextListener
{
    void onContextValueChanged(in ContextValue contextValue);
}
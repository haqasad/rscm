package org.aspectsense.rscm.runtime.ui;

import org.aspectsense.rscm.IContextAccess;
import org.aspectsense.rscm.IContextManagement;

/**
 * User: Nearchos Paspallis
 * Date: 5/24/13
 * Time: 11:53 AM
 */
public interface ContextAccessAndManagementPortal
{
    public IContextAccess getContextAccess();

    public IContextManagement getContextManagement();
}
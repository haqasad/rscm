/*
 * Really Simple Context Middleware (RCSM)
 *
 * Copyright (c) 2012 The RCSM Team
 *
 * This file is part of the RCSM: the Really Simple Context Middleware for ANDROID. More information about the project
 * is available at: http://code.google.com/p/rscm
 *
 * The RCSM is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.aspectsense.rscm.reasoner.user_activity;

import android.util.Log;
import org.aspectsense.rscm.ContextValue;
import org.aspectsense.rscm.context.plugin.ReasonerService;
import org.json.JSONException;

/**
 * User: Nearchos Paspallis
 * Date: 6/6/12
 * Time: 8:42 PM
 */
public class UserActivityReasoner extends ReasonerService
{
    public static final String TAG = "org.aspectsense.rscm.reasoner.user_activity.UserActivityReasoner";

    public static final String SCOPE_USER_ACTIVITY = "user.activity";

    @Override protected void onContextValueChanged(ContextValue contextValue)
    {
        if("battery.level".equals(contextValue.getScope()))
        {
            try
            {
                final int battery_level = contextValue.getValueAsInteger();
                notifyListener(ContextValue.createContextValue(SCOPE_USER_ACTIVITY, "The user holds a phone with " + battery_level + "% remaining battery level"));
            }
            catch (JSONException jsone)
            {
                Log.e(TAG, "JSON exception while parsing context value: " + contextValue, jsone);
            }
        }
    }
}
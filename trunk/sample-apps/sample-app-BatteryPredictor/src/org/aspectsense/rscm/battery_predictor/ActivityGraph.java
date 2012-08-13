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

package org.aspectsense.rscm.battery_predictor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.aspectsense.rscm.battery_predictor.db.DatabaseHelper;

/**
 * Date: 8/12/12
 * Time: 12:46 PM
 */
public class ActivityGraph extends Activity
{
    public static final int [] COLORS = new int[] { Color.GREEN, Color.RED };

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final DatabaseHelper databaseHelper = DatabaseHelper.getDatabaseHelper(this);

        // dataset
        final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        final XYSeries batterySeries = databaseHelper.getBatterySeries();
        dataset.addSeries(batterySeries);
        final XYSeries connectivitySeries = databaseHelper.getConnectivitySeries();
        dataset.addSeries(connectivitySeries);

        // renderer
//                PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
        final XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setShowGrid(true);
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(15);
        renderer.setLegendTextSize(15);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] { 20, 30, 15, 20 });
        final int length = COLORS.length;
        for (int i = 0; i < length; i++) {
            final XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(COLORS[i]);
//            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
        final GraphicalView graphicalView = ChartFactory.getTimeChartView(this, dataset, renderer, "Context graph");

        setContentView(graphicalView);
    }
}
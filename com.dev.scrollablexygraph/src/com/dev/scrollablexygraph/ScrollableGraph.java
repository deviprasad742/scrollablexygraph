package com.dev.scrollablexygraph;

/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;

public class ScrollableGraph extends Figure {

	private XYGraph xyGraph;
	private ScrollableGraphPreferences preferences;
	private ScrollableGraphDataProvider dataProvider;

	public ScrollableGraph(ScrollableGraphPreferences preferences) {
		this.preferences = preferences;
		createGraph();
		xyGraph.setOpaque(false);
		add(xyGraph);

	}
	
	@Override
	protected void layout() {
		Rectangle clientArea = getClientArea().getCopy();
		xyGraph.setBounds(new Rectangle(clientArea));
		super.layout();
	}

	private void createGraph() {
		xyGraph = new XYGraph();
		xyGraph.setTitle("Scrollable XY-Graph");

		xyGraph.primaryXAxis.setDateEnabled(preferences.isDateEnabled());
		xyGraph.primaryXAxis.setAutoScale(true);
		xyGraph.primaryYAxis.setAutoScale(true);
		xyGraph.primaryXAxis.setAutoScaleThreshold(0);
		xyGraph.primaryYAxis.setAutoScaleThreshold(0);


		//create a trace data provider, which will provide the data to the trace.
		dataProvider = new ScrollableGraphDataProvider(preferences);
		//		traceDataProvider.setXAxisDateEnabled(true);

		//create the trace
		final Trace intTrace = new Trace("Randome Values", xyGraph.primaryXAxis, xyGraph.primaryYAxis, dataProvider);
		intTrace.setAntiAliasing(true);
		//set trace property
		intTrace.setPointStyle(PointStyle.NONE);
		//add the trace to xyGraph
		xyGraph.addTrace(intTrace);

	}

	public ScrollableGraphDataProvider getDataProvider() {
		return dataProvider;
	}

}

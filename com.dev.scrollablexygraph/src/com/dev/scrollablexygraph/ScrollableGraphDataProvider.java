package com.dev.scrollablexygraph;

import org.csstudio.swt.xygraph.dataprovider.AbstractDataProvider;
import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.swt.widgets.Display;

public class ScrollableGraphDataProvider extends AbstractDataProvider {

	private static final double MARGIN = 0.3;
	public static final int LIVE_DATA_SELECTION = -1;
	private ScrollableGraphPreferences preferences;
	private ExtCircularBuffer<ISample> buffer;

	private int startIndex;
	private ISample[] pastSamples;
	private int size;
	private Runnable fireUpdate;
	private boolean dataRangedirty = false;

	public ScrollableGraphDataProvider(ScrollableGraphPreferences preferences) {
		super(true);
		this.preferences = preferences;
		buffer = new ExtCircularBuffer<ISample>(preferences.getHistoryLimit());
		fireUpdate = new Runnable() {
			public void run() {
				for (IDataProviderListener listener : listeners) {
					listener.dataChanged(ScrollableGraphDataProvider.this);
				}
			}
		};
	}

	public int getScrollLength() {
		double ratio = (double) buffer.size() / preferences.getVisibleItems();
		return (int) Math.ceil(ratio) * preferences.getThumb();
	}

	public synchronized void setDataSelection(int selection) {
		if (selection == LIVE_DATA_SELECTION) {
			pastSamples = null;
		} else {
			int graphSpan = preferences.getVisibleItems();
			// each increment covers half of graph span hence selection also covers half of it
			int startIndex = (graphSpan / preferences.getThumb()) * selection;
			int endIndex = startIndex + graphSpan;
			if (endIndex >= buffer.size()) {
				endIndex = buffer.size();
				startIndex = endIndex - graphSpan;
			}
			pastSamples = buffer.getSubArray(startIndex, endIndex, new Sample[0]);
		}
		fireDataChange();
	}

	@Override
	public int getSize() {
		return size;
	}

	public int getTotalSize() {
		return buffer.size();
	}

	@Override
	public ISample getSample(int index) {
		if (isPaused()) {
			return pastSamples[index];
		} else {
			return buffer.getElement(startIndex + index);
		}
	}

	@Override
	protected void innerUpdate() {
		dataRangedirty = true;
		// update data range immediately to save the computation from 
		updateDataRange();
	}

	/**
	 * Code copied from {@link
	 * org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider.
	 * updateDataRange()} and modified to support autoscaling
	 * 
	 */
	@Override
	protected synchronized void updateDataRange() {
		if (!dataRangedirty) {
			return;
		}
		dataRangedirty = false;

		if (getSize() > 0) {
			ISample start = getSample(0);
			//ISample end = getSample(getSize() - 1);

			double xMin = start.getXValue();
			double xMax = xMin + (preferences.getVisibleItems() - 1) * (preferences.isDateEnabled() ? preferences.getRefreshDelay() : 1);
			xDataMinMax = new Range(xMin, xMax);

			double yMin = start.getYValue();
			double yMax = yMin;
			for (int i = 0; i < getSize(); i++) {
				ISample sample = getSample(i);
				if (yMin > sample.getYValue() - sample.getYMinusError())
					yMin = sample.getYValue() - sample.getYMinusError();
				if (yMax < sample.getYValue() + sample.getYPlusError())
					yMax = sample.getYValue() + sample.getYPlusError();
			}

			// add little margins on top and bottom while autoscaling
			yMin = yMin - yMin * MARGIN;
			yMin = Math.max(yMin, 0);
			yMax = yMax + yMax * MARGIN;

			yMin = Math.round(yMin);
			yMax = Math.round(yMax);

			yDataMinMax = new Range(yMin, yMax);
		} else {
			xDataMinMax = null;
			yDataMinMax = null;
		}
	}

	public synchronized void addSample(ISample sample) {
		buffer.add(sample);
		size = Math.min(buffer.size(), preferences.getVisibleItems());

		// compute the start index
		int endIndex = buffer.size();
		startIndex = endIndex - preferences.getVisibleItems();
		if (startIndex < 0) {
			startIndex = 0;
		}

		if (!isPaused()) {// donot update graph
			fireDataChange();
		}
	}

	public boolean isPaused() {
		return pastSamples != null;
	}

	@Override
	protected void fireDataChange() {
		innerUpdate();
		Display.getDefault().asyncExec(fireUpdate);
	}

}

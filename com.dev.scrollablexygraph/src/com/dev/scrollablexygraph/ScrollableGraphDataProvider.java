package com.dev.scrollablexygraph;

import java.util.Arrays;

import org.csstudio.swt.xygraph.dataprovider.AbstractDataProvider;
import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * A data provider which displays the most recent samples when the scrollbar is
 * to extreme right or the past samples based on the scrollbar position.
 * 
 */

public class ScrollableGraphDataProvider extends AbstractDataProvider {

	private static final double MARGIN = 0.3;
	public static final int LIVE_DATA_SELECTION = -1;
	private ScrollableGraphPreferences preferences;
	private ExtCircularBuffer<ISample> buffer;

	private int startIndex;
	// the part of the history which is currently visible
	private ISample[] visibleHistory;
	// a snapshot at the time when the scrollbar is moved back first time
	private ISample[] historySnapShot;
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
		double itemsPerScroll = (double) preferences.getVisibleItems() / preferences.getThumb();
		double scrollLength = buffer.size() / itemsPerScroll;
		return (int) Math.ceil(scrollLength);
	}

	public synchronized void setDataSelection(int selection) {
		if (selection == LIVE_DATA_SELECTION) {
			historySnapShot = null;
		} else {
			if (historySnapShot == null) {
				// capture the history at the point the scrollbar is moved back from extreme right
				historySnapShot = buffer.getSubArray(0, buffer.size(), new Sample[0]);
			}
			int graphSpan = preferences.getVisibleItems();
			int startIndex = (graphSpan / preferences.getThumb()) * selection;
			int endIndex = startIndex + graphSpan;
			int snapSize = historySnapShot.length;
			if (endIndex >= snapSize) {
				endIndex = snapSize;
				startIndex = endIndex - graphSpan;
			}
			//portin of the history which is dispayed currently
			visibleHistory = Arrays.copyOfRange(historySnapShot, startIndex, endIndex);
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
			return visibleHistory[index];
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
		return historySnapShot != null;
	}

	@Override
	protected void fireDataChange() {
		innerUpdate();
		Display.getDefault().asyncExec(fireUpdate);
	}

}

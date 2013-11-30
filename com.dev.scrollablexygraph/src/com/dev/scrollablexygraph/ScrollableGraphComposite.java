package com.dev.scrollablexygraph;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

public class ScrollableGraphComposite extends Composite {

	private ScrollableGraph xyGraph;
	private ScrollableGraphPreferences preferences;
	private Slider scrollBar;

	public ScrollableGraphComposite(Composite parent, int style) {
		this(parent, style, new ScrollableGraphPreferences());
	}

	public ScrollableGraphComposite(Composite parent, int style, ScrollableGraphPreferences preferences) {
		super(parent, style);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		GridLayoutFactory.fillDefaults().applyTo(this);
		this.preferences = preferences;
		createUI(this);
	}

	private void createUI(Composite composite) {
		Canvas canvas = new Canvas(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
		GridLayoutFactory.fillDefaults().applyTo(canvas);

		LightweightSystem lws = new LightweightSystem(canvas);
		xyGraph = new ScrollableGraph(preferences);
		lws.setContents(xyGraph);

		scrollBar = new Slider(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scrollBar);
		setScrollBarVisible(false);

		addListeners();
		addDataGenerator();
	}

	private void addListeners() {
		addScrollListener();
		addDataListener();
	}

	private void addDataGenerator() {
		TimerTask task = new TimerTask() {
			double i = 0;
			long j = 1;

			@Override
			public void run() {
				Random random = new Random();
				i = preferences.isDateEnabled() ? System.currentTimeMillis() : ++i;
				j = 20 + random.nextInt(15);
				Sample sample = new Sample(i, j);
				xyGraph.getDataProvider().addSample(sample);
			}
		};
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, preferences.getRefreshDelay());
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				timer.cancel();
			}
		});
	}

	private void addDataListener() {
		xyGraph.getDataProvider().addDataProviderListener(new IDataProviderListener() {
			@Override
			public void dataChanged(IDataProvider dataProvider) {
				updateScrollBar(xyGraph.getDataProvider());
			}
		});
	}

	private void addScrollListener() {
		scrollBar.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String string = "SWT.NONE";
				switch (event.detail) {
				case SWT.DRAG:
					selectionChanged();
					string = "SWT.DRAG";
					break;
				case SWT.HOME:
					string = "SWT.HOME";
					break;
				case SWT.END:
					string = "SWT.END";
					break;
				case SWT.ARROW_DOWN:
					selectionChanged();
					string = "SWT.ARROW_DOWN";
					break;
				case SWT.ARROW_UP:
					selectionChanged();
					string = "SWT.ARROW_UP";
					break;
				case SWT.PAGE_DOWN:
					selectionChanged();
					string = "SWT.PAGE_DOWN";
					break;
				case SWT.PAGE_UP:
					selectionChanged();
					string = "SWT.PAGE_UP";
					break;
				}
				System.out.println("Scroll detail -> " + string);
			}
		});
	}

	private void selectionChanged() {
		if (scrollBar.getSelection() == scrollBar.getMaximum() - scrollBar.getThumb()) {
			if (xyGraph.getDataProvider().isPaused()) {
				xyGraph.getDataProvider().setDataSelection(ScrollableGraphDataProvider.LIVE_DATA_SELECTION);
			}
		} else {
			xyGraph.getDataProvider().setDataSelection(scrollBar.getSelection());
		}
	}

	private void updateScrollBar(ScrollableGraphDataProvider dataProvider) {
		boolean isScrollRequired = dataProvider.getTotalSize() > preferences.getVisibleItems();
		if (isScrollRequired && !dataProvider.isPaused()) {
			int scrollLength = dataProvider.getScrollLength();
			int minimum = 0;
			int increment = preferences.getScrollIncrement();
			int thumb = preferences.getThumb();
			int selection = scrollLength;
			scrollBar.setValues(selection, minimum, scrollLength, thumb, increment, increment);
			if (!scrollBar.isVisible()) {
				setScrollBarVisible(true);
			}
		}
	}

	private void setScrollBarVisible(boolean visible) {
		scrollBar.setVisible(visible);
		((GridData) scrollBar.getLayoutData()).exclude = !visible;
		layout(true);
	}

}

package com.dev.scrollablexygraph;

public class ScrollableGraphPreferences {

	public static final int EVENT_REFRESH = -1;

	private int visibleItems = 100;
	private int historyLimit = 10000;
	private int refreshDelay = 50;

	private int captureDelay = 500;
	private boolean captureEvents = false;
	private boolean eventClues = true;
	private int captureLimit = 10;
	private boolean isDateEnabled = false;

	// thumb represents the items covered by scrollbar  visibleitems/thumb is the no of items moved per scroll
	private int thumb = 20;
	private int increment = 1;

	public int getVisibleItems() {
		return visibleItems;
	}

	public void setVisibleItems(int visibleItems) {
		this.visibleItems = visibleItems;
	}

	public int getHistoryLimit() {
		return historyLimit;
	}

	public void setHistoryLimit(int historyLimit) {
		this.historyLimit = historyLimit;
	}

	public int getRefreshDelay() {
		return refreshDelay;
	}

	public void setRefreshDelay(int refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

	public boolean isCaptureEvents() {
		return captureEvents;
	}

	public void setCaptureEvents(boolean captureEvents) {
		this.captureEvents = captureEvents;
	}

	public boolean isEventClues() {
		return eventClues;
	}

	public void setEventClues(boolean eventClues) {
		this.eventClues = eventClues;
	}

	public int getCaptureDelay() {
		return captureDelay;
	}

	public void setCaptureDelay(int captureDelay) {
		this.captureDelay = captureDelay;
	}

	public int getCaptureLimit() {
		return captureLimit;
	}

	public void setCaptureLimit(int captureLimit) {
		this.captureLimit = captureLimit;
	}

	public int getThumb() {
		return thumb;
	}
	
	public void setThumb(int thumb) {
		this.thumb = thumb;
	}

	public int getScrollIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}
	
	public boolean isDateEnabled() {
		return isDateEnabled;
	}
	
	public void setDateEnabled(boolean isDateEnabled) {
		this.isDateEnabled = isDateEnabled;
	}

}

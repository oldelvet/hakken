package uk.co.vurt.hakken.domain.task.pageitem;

import java.util.ArrayList;

public class PageMultiItem extends PageItem {
	private ArrayList<PageItem> items;

	public ArrayList<PageItem> getItems() {
		return items;
	}

	public void setItems(ArrayList<PageItem> items) {
		this.items = items;
	}
}

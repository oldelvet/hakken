package uk.co.vurt.hakken.domain.task;

import java.util.ArrayList;
import java.util.List;

import uk.co.vurt.hakken.domain.task.pageitem.PageItem;

public class Page {
	private String name;
	private String title;
	private List<PageItem> items;
	
	public Page(){
		items = new ArrayList<PageItem>();
	}
	
	public Page(String name, String title, List<PageItem> items) {
		super();
		this.name = name;
		this.title = title;
		this.items = items;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<PageItem> getItems() {
		return items;
	}

	public void setItems(List<PageItem> items) {
		this.items = items;
	}

	public void addItem(PageItem item){
		this.items.add(item);
	}

	@Override
	public String toString() {
		return "Page [name=" + name + ", title=" + title + ", items=" + items
				+ "]";
	}
	
	
}
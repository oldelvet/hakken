package uk.co.vurt.hakken.activities;

import uk.co.vurt.hakken.domain.job.DataItem;
import uk.co.vurt.hakken.domain.task.pageitem.PageItem;

/**
 * Widget data accessors.
 */
public interface DataWidgetTools {
    /**
     * Retrieve data item for the supplied
     * @param pageName page name
     * @param name data item name
     * @param type data item type
     * @return the data item
     */
    DataItem retrieveDataItem(String pageName, String name, String type);

    /**
     * Create widget page key for identifying widget on page.
     * @param pageName page name
     * @param item page item
     * @return the key for the specified item
     */
    String createWidgetKey(String pageName, PageItem item);
}

package uk.co.vurt.hakken.activities;

import android.net.Uri;

import net.wmfs.coalesce.csql.ExpressionException;

import uk.co.vurt.hakken.domain.job.DataItem;
import uk.co.vurt.hakken.domain.task.pageitem.PageItem;

/**
 * Widget data accessors.
 */
public interface DataWidgetTools {
    /**
     * Retrieve data item for the supplied name combination.
     * @param pageName page name
     * @param name data item name
     * @param type data item type
     * @return the data item
     */
    DataItem retrieveDataItem(String pageName, String name, String type);

    /**
     * Create data item for the supplied value.
     * @param pageName page name
     * @param name data item name
     * @param type data item type
     * @return the data item
     */
    DataItem createDataItem(String pageName, String name, String type, String value);

    /**
     * Store the supplied data item.
     * @param dataItem the data item to save
     * @return the URI of the data item
     */
    Uri storeDataItem(DataItem dataItem);

    /**
     * Store the supplied data item.
     * @param dataItem the data item to save
     * @param keyword keyword
     * @param description description
     * @return the URI of the data item
     */
    Uri storeDataItem(DataItem dataItem, String keyword, String description);

    /**
     * Remove the supplied data item.
     * @param dataItem the data item to delete
     */
    void removeDataItem(DataItem dataItem);

    /**
     * Create widget page key for identifying widget on page.
     * @param pageName page name
     * @param item page item
     * @return the key for the specified item
     */
    String createWidgetKey(String pageName, PageItem item);

    /**
     * Evaluate a condition.
     * @param condition the condition to evaluate
     * @return true if the condition is satisfied
     * @throws ExpressionException
     */
    boolean evaluateCondition(String condition) throws ExpressionException;

    /**
     * Evaluate an expression value.
     * @param expression the expression to evaluate
     * @return the expression value
     */
    String evaluateExpression(String expression);

    /**
     * Record a validation message.
     * @param msg the message
     */
    void recordValidationMessage(String msg);
}

package uk.co.vurt.hakken.ui.widget;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.wmfs.coalesce.csql.ExpressionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.vurt.hakken.R;
import uk.co.vurt.hakken.activities.DataWidgetTools;
import uk.co.vurt.hakken.activities.DatePickerDialogTools;
import uk.co.vurt.hakken.activities.RunJob;
import uk.co.vurt.hakken.domain.job.DataItem;
import uk.co.vurt.hakken.domain.task.pageitem.PageItem;

/**
 * Multiple widget group parent.
 * This holds the child widgets and manages the mappings to the underlying data item storage.
 */
public class MultiGroupParent extends AbstractLabelledWidget
implements Serializable, View.OnClickListener, MultiChildListener {
    protected Button mButtonExtend;

    /** Page name. */
    private String mPageName;
    /** Child group items specification. */
    private List<PageItem> mItems;
    /** Variable base name. */
    private String mBaseName;
    /** Parent data widget tools. */
    private DataWidgetTools mDwt;
    /** Parent date widget tools. */
    private DatePickerDialogTools mDpd;
    /** Parent widget wrapper mapping. */
    private HashMap<String, WidgetWrapper> mWidgetWrapperMap;
    /**
     * The next child ID to be allocated. Zero is allocated to the child included in the
     * parent XML layout.
     */
    private int nextChildId = 0;
    /**
     * The children currently attached to the group. The index in the array is permanently
     * allocated to that child as an internal ID field. It is not associated with the data
     * storage that uses a separate number allocation method.
     */
    private final SparseArray<MultiGroupChild> mChildren = new SparseArray<>();
    /**
     * The arrangement order of attached children. This is used for data load/save operations
     * and is used to identify the data index.
     */
    private final List<Integer> mChildOrder = new ArrayList<>();
    /** Any retired data items that should be removed/flushed on the next save. */
    private final Set<Integer> mRetiredDataIndices = new HashSet<Integer>();

    /**
     * Constructor.
     * @param context
     */
    public MultiGroupParent(Context context) {
        super(context);
        inflate(context, R.layout.multi_group, this);
        label = (TextView)findViewById(R.id.multi_parent_label);
        mButtonExtend = (Button)findViewById(R.id.buttonExtend);
        mButtonExtend.setOnClickListener(this);
    }

    /**
     *
     * @param pageName
     * @param widgetWrapperMap
     * @param isAdHoc
     * @param missingValues
     * @return
     */
    public boolean saveItems(String pageName,
                             HashMap<String, WidgetWrapper> widgetWrapperMap,
                             boolean isAdHoc,
                             List<String> missingValues) {
        boolean valid = true;
        for (Integer childId : mChildOrder) {
            MultiGroupChild child = mChildren.get(childId);
            ChildDwt childDwt = (ChildDwt) child.getmDwt();
            valid &= RunJob.savePageItems(mItems,
                    pageName,
                    widgetWrapperMap,
                    isAdHoc,
                    childDwt,
                    missingValues);
        }
        return valid;
    }

    /**
     * Set the page items to be used in each child
     * @param pageName page name
     * @param basename variable base name
     * @param items sub-page items
     * @param dwt parent data widget tools
     * @param dpd date picker dialog tools
     */
    public void setItems(String pageName, String basename, List<PageItem> items,
                         HashMap<String, WidgetWrapper> widgetWrapperMap,
                         DataWidgetTools dwt, DatePickerDialogTools dpd) {
        this.mPageName = pageName;
        this.mBaseName = basename;
        this.mItems = items;
        this.mWidgetWrapperMap = widgetWrapperMap;
        this.mDwt = dwt;
        this.mDpd = dpd;
        MultiGroupChild child = (MultiGroupChild) findViewById(R.id.multi_child);
        recordNewChild(nextChildId++, child);
    }

    /**
     * Process add child.
     *
     * In the fullness of time this may reject adding due to rules requested by the page
     * description.
     */
    private void maybeAddChild() {
        MultiGroupChild child = new MultiGroupChild(getContext());
        recordNewChild(nextChildId++, child);
        // Add child at the end of the groups. Just before the add button.
        this.addView(child, this.getChildCount() - 1);
    }

    /**
     * Hook a child into the data structures and setup the child layout.
     * @param childId the identification code to use for this child
     * @param child the child
     */
    private void recordNewChild(Integer childId, MultiGroupChild child) {

        child.setChildListener(this);
        child.setChildId(childId);
        ChildDwt childDwt = new ChildDwt(childId, mDwt, mBaseName);
        child.setItems(mPageName, mItems, mWidgetWrapperMap, childDwt, mDpd);
        mChildren.put(childId, child);
        MultiGroupChild otherChild = mChildren.get(childId);
        // Added location is the next child index
        int dataIndex = mChildOrder.size();
        child.setDataIndex(dataIndex);
        childDwt.setDataIndex(dataIndex);
        // Retired data index item no longer needs removing
        mRetiredDataIndices.remove((Integer)dataIndex);
        mChildOrder.add(childId);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonExtend:
                maybeAddChild();
                break;
            default:
                // Not interesting
                break;
        }
    }

    @Override
    public void removeChildRequest(MultiGroupChild child) {
        Integer childId = child.getChildId();
        mChildren.delete(childId);
        boolean wasFound = mChildOrder.remove(childId);
        if (wasFound) {
            // Renumber existing data index
            // This may not be necessary but it simplifies things if always done
            int dataIndex = 0;
            for (Integer oneChildId : mChildOrder) {
                MultiGroupChild oneChild = mChildren.get(oneChildId);
                ChildDwt dwt = (ChildDwt)oneChild.getmDwt();
                oneChild.setDataIndex(dataIndex);
                dwt.setDataIndex(dataIndex);
                dataIndex++;
            }
            // Record that the last item is now a retired data index value
            this.mRetiredDataIndices.add(mChildOrder.size());
        } else {
            throw new RuntimeException("Child mismatch - child did not exist");
        }
        this.removeView(child);
    }

    /**
     * Child group data widget tools. This acts as the interface between the group and the overall
     * widget child naming system.
     */
    public class ChildDwt implements DataWidgetTools {

        /** Child group internal widget ID. The ID is unique for the baseName in the currently running
         * instance of this page. It will likely change when moving to a new page and/or when
         * revisiting the screen. The ID does stay constant over screen orientation changes.
         */
        private final int childId;
        /** Base widget tools. */
        private final DataWidgetTools baseDwt;
        /** Item base name. */
        private final String baseName;
        /**
         * Data index. The data index to load/save from for this item. This can change as
         * children are added/removed. But we expect the parent to keep track of any that need
         * cleaning up on save.
         */
        private Integer dataIndex;

        /**
         * Constructor.
         * @param childId child group internal widget ID
         * @param baseDwt parent base widget tools
         * @param baseName item base name
         */
        public ChildDwt(int childId, DataWidgetTools baseDwt, String baseName) {
            this.childId = childId;
            this.baseDwt = baseDwt;
            this.baseName = baseName;
        }

        /**
         * Set the data index to use for the children of this item.
         * @param dataIndex data index
         */
        public void setDataIndex(Integer dataIndex) {
            this.dataIndex = dataIndex;
        }

        @Override
        public DataItem retrieveDataItem(String pageName, String name, String type) {
            String dataname = baseName + "[" + dataIndex + "]" + name;
            return baseDwt.retrieveDataItem(pageName, dataname, type);
        }

        @Override
        public DataItem createDataItem(String pageName, String name, String type, String value) {
            String dataname = baseName + "[" + dataIndex + "]" + name;
            return baseDwt.createDataItem(pageName, dataname, type, value);
        }

        @Override
        public String createWidgetKey(String pageName, PageItem item) {
            // Note this is only used for internal display widget caching and is not used for external naming.
            return pageName + "_" + baseName + "{" + childId + "}" + item.getName() + "_" + item.getType();
        }

        @Override
        public Uri storeDataItem(DataItem dataItem) {
            return baseDwt.storeDataItem(dataItem);
        }

        @Override
        public Uri storeDataItem(DataItem dataItem, String keyword, String description) {
            return baseDwt.storeDataItem(dataItem, keyword, description);
        }

        @Override
        public void removeDataItem(DataItem dataItem) {
            baseDwt.removeDataItem(dataItem);
        }

        @Override
        public boolean evaluateCondition(String condition) throws ExpressionException {
            return baseDwt.evaluateCondition(condition);
        }

        @Override
        public String evaluateExpression(String expression) {
            return baseDwt.evaluateExpression(expression);
        }

        @Override
        public void recordValidationMessage(String msg) {
            baseDwt.recordValidationMessage(msg);
        }
    }
}

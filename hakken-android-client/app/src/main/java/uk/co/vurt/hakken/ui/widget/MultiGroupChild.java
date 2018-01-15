package uk.co.vurt.hakken.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import uk.co.vurt.hakken.R;
import uk.co.vurt.hakken.activities.DataWidgetTools;
import uk.co.vurt.hakken.activities.DatePickerDialogTools;
import uk.co.vurt.hakken.activities.RunJob;
import uk.co.vurt.hakken.domain.task.pageitem.PageItem;

/**
 * Multi-widget group child item.
 *
 * This forms a repeatable widget group that can take any normal widgets internally. In theory
 * a multi-widget could be contained within here but it is not tested/supported.
 */
public class MultiGroupChild extends AbstractLabelledWidget
implements Serializable, View.OnClickListener {
    /** Child items specification. */
    private List<PageItem> mItems;
    /** Remove child button. */
    protected Button mButtonRemove;
    /** Child removal listener. */
    protected MultiChildListener mChildListener;
    /** Child ID field. This is the key used for save/restore operations. */
    private Integer childId;
    /**
     * Data index. The data index to load/save from for this item. This can change as
     * children are added/removed. But we expect the parent to keep track of any that need
     * cleaning up on save.
     */
    private Integer dataIndex;
    /** Data widget tools that are in use. */
    private DataWidgetTools mDwt;

    /**
     * Constructor.
     * @param context
     */
    public MultiGroupChild(Context context) {
        super(context);
        doCommonSetup(context);
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     */
    public MultiGroupChild(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        doCommonSetup(context);
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public MultiGroupChild(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doCommonSetup(context);
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    public MultiGroupChild(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        doCommonSetup(context);
    }

    /**
     * Common setup for each constructor.
     * @param context
     */
    private void doCommonSetup(Context context) {
        inflate(context, R.layout.multi_child, this);
        mButtonRemove = findViewById(R.id.buttonRemove);
        mButtonRemove.setOnClickListener(this);
    }

    /**
     * Set the child ID field value.
     * @param childId child ID
     */
    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    /**
     * @return the childId for this node
     */
    public Integer getChildId() {
        return childId;
    }

    /**
     * Set the data index field value.
     * @param dataIndex data index
     */
    public void setDataIndex(Integer dataIndex) {
        this.dataIndex = dataIndex;
    }

    /**
     * @return the data index for this node
     */
    public Integer getDataIndex() {
        return dataIndex;
    }

    /**
     * @return the data widget tools for this item
     */
    public DataWidgetTools getmDwt() {
        return mDwt;
    }

    /**
     * Set the page items to be used in each child
     * @param pageName page name
     * @param items page items
     * @param dwt data widget tools
     * @param dpd date picker dialog tools
     */
    public void setItems(String pageName,
                         List<PageItem> items,
                         HashMap<String, WidgetWrapper> widgetWrapperMap,
                         DataWidgetTools dwt,
                         DatePickerDialogTools dpd) {
        this.mItems = items;
        this.mDwt = dwt;
        RunJob.buildWidgets(getContext(), items, pageName, widgetWrapperMap, this, dpd, dwt);
    }

    /**
     * Set the child listener.
     * @param childListener
     */
    public void setChildListener(MultiChildListener childListener) {
        mChildListener = childListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRemove:
                if (mChildListener != null) {
                    mChildListener.removeChildRequest(this);
                }
                break;
            default:
                // No interest in other items.
                break;
        }
    }
}

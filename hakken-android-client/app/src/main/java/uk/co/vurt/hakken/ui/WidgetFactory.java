package uk.co.vurt.hakken.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uk.co.vurt.hakken.R;
import uk.co.vurt.hakken.activities.DataWidgetTools;
import uk.co.vurt.hakken.activities.DatePickerDialogTools;
import uk.co.vurt.hakken.domain.NameValue;
import uk.co.vurt.hakken.domain.job.DataItem;
import uk.co.vurt.hakken.domain.task.pageitem.LabelledValue;
import uk.co.vurt.hakken.domain.task.pageitem.PageItem;
import uk.co.vurt.hakken.domain.task.pageitem.PageMultiItem;
import uk.co.vurt.hakken.processor.PageItemProcessor;
import uk.co.vurt.hakken.ui.widget.LabelledCheckBox;
import uk.co.vurt.hakken.ui.widget.LabelledDatePicker;
import uk.co.vurt.hakken.ui.widget.LabelledEditBox;
import uk.co.vurt.hakken.ui.widget.LabelledSpinner;
import uk.co.vurt.hakken.ui.widget.MultiGroupParent;
import uk.co.vurt.hakken.ui.widget.WidgetWrapper;
import android.content.Context;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * This class is messy. Needs reimplementing with a nice map of individual widget creators
 * keyed on the item type, or something like that.
 * 
 *
 */
public class WidgetFactory {

	private static final String TAG = "WidgetFactory";
	
	
	public static WidgetWrapper createWidget(Context context,
                                             PageItem item,
                                             DataItem dataItem,
                                             String pageName,
                                             HashMap<String, WidgetWrapper> widgetWrapperMap,
                                             DataWidgetTools dwt,
                                             DatePickerDialogTools dpd) {

		View widget = null;

		boolean readonly = PageItemProcessor.getBooleanAttribute(item, "readonly");
		boolean hidden = PageItemProcessor.getBooleanAttribute(item, "hidden");
		boolean required = PageItemProcessor.getBooleanAttribute(item, "required");
		String condition = PageItemProcessor.getStringAttribute(item, "condition");
		String hint = PageItemProcessor.getStringAttribute(item, "hint");
		String length = PageItemProcessor.getStringAttribute(item, "length");
		String defaultValue = PageItemProcessor.getStringAttribute(item, "default");
		
		String initialValue = item.getValue();
		if(dataItem != null){
			initialValue = dataItem.getValue();
		}
		
		// create new widget and add it to the map
		if ("LABEL".equals(item.getType())) {
			TextView label = new TextView(context);

			label.setText(item.getLabel());
			widget = label;
		} else if ("MULTI".equals(item.getType())) {
            String minStr = PageItemProcessor.getStringAttribute(item, "minitems");
            String maxStr = PageItemProcessor.getStringAttribute(item, "maxitems");
            int minItems = MultiGroupParent.MIN_CHILDREN_DEFAULT;
            if ((minStr != null) && (minStr.trim().length() > 0)) {
                try {
                    minItems = Integer.parseInt(minStr);
                } catch (Exception e) {
                    Log.w(TAG, "Bad minitems value " + minStr);
                }
            }
            if (minItems < 0) {
                minItems = 0;
            }
            if (minItems > MultiGroupParent.MAX_CHILDREN_LIMIT) {
                minItems = MultiGroupParent.MAX_CHILDREN_LIMIT;
            }
            int maxItems = MultiGroupParent.MIN_CHILDREN_DEFAULT;
            if ((maxStr != null) && (maxStr.trim().length() > 0)) {
                try {
                    maxItems = Integer.parseInt(maxStr);
                } catch (Exception e) {
                    Log.w(TAG, "Bad maxitems value " + maxStr);
                }
            }
            if (maxItems < minItems) {
                maxItems = minItems;
            }
            if (maxItems > MultiGroupParent.MAX_CHILDREN_LIMIT) {
                maxItems = MultiGroupParent.MAX_CHILDREN_LIMIT;
            }
			PageMultiItem pmi = (PageMultiItem) item;
			MultiGroupParent gp = new MultiGroupParent(context);
			gp.setLimits(minItems, maxItems);
			gp.setItems(pageName, pmi.getName(), pmi.getItems(), widgetWrapperMap, dwt, dpd);
			widget = gp;
		} else if ("TEXT".equals(item.getType())) {
			if(initialValue == null){
				initialValue = "";
			}
			if(readonly){
				TextView label = new TextView(context);
				label.setText(item.getLabel() + ": " + initialValue);
				widget = label;
			}else{
				LabelledEditBox editBox = new LabelledEditBox(context, 
															  item.getLabel(), 
															  initialValue,
															  defaultValue,
															  hint,
															  length);
				widget = editBox;
				String linesAttribute = PageItemProcessor.getStringAttribute(item, "lines");
				if(linesAttribute != null && linesAttribute.length() > 0){
					editBox.setLines(Integer.parseInt(linesAttribute));
				}
			}
		
			// TO DO
			// Add new type for Address which displays 5 boxes for each address field
			// Will then need to mangle them together in the submit part of the form.
			
		} else if ("DIGITS".equals(item.getType())
				|| "NUMERIC".equals(item.getType())) {
			boolean noDefault = PageItemProcessor.getBooleanAttribute(item, "nodefault");
			if(noDefault && initialValue == null){
				initialValue = "";
			}
//			LabelledEditBox editBox = new LabelledEditBox(context,
//					item.getLabel(), 
//					defaultValue != null ? defaultValue : "0");
			
			LabelledEditBox editBox = new LabelledEditBox(context, 
					  item.getLabel(), 
					  initialValue,
					  defaultValue,
					  hint,
					  length);
			
			editBox.setKeyListener(new DigitsKeyListener());
			widget = editBox;
		} else if ("DATETIME".equals(item.getType())) {
			final LabelledDatePicker datePicker = new LabelledDatePicker(
					context, item.getLabel(),
					initialValue != null ? initialValue : "");
			widget = datePicker;
		} else if ("YESNO".equals(item.getType())) {
			
			
			LabelledCheckBox checkBox = new LabelledCheckBox(
					context,
					item.getLabel(),
					initialValue != null && (initialValue.equals("true") || initialValue.equals("Y")) 
							? true
							: false);
			widget = checkBox;
		} else if ("SELECT".equals(item.getType()) || "SELECT_RADIO".equals(item.getType())) {
			boolean multiSelect = false;
			if(item.getAttributes() != null && item.getAttributes().containsKey("multiselect")){
				Log.d(TAG, "multiselect: " + item.getAttributes().get("multiselect"));				
				multiSelect = Boolean.parseBoolean(item.getAttributes().get("multiselect"));
			}
			LabelledSpinner spinner = new LabelledSpinner(context, item.getLabel(), multiSelect);
			//At this point, the value is just a string containing json.
			//we need to convert that to something usable by the spinner.
			ArrayList<NameValue> spinnerArray = new ArrayList<NameValue>();
			if(!multiSelect){
				spinnerArray.add(new NameValue(context.getResources().getString(R.string.spinner_none_selected),null)); //add a blank entry to avoid the first item being pre-selected.
			}

			ArrayList<String> dataItemValues = new ArrayList<String>();
			if(initialValue != null){
				if(initialValue.contains(",")){
					//multiselect value
					dataItemValues.addAll(Arrays.asList(initialValue.split("[,]")));
				} else {
					dataItemValues.add(initialValue);
				}
			}
			
			List<NameValue> selected = new ArrayList<NameValue>();
			List<LabelledValue> values = item.getValues();
			for(LabelledValue value: values){
				NameValue nameValue = new NameValue(value.getLabel(), value.getValue(), value.getSingleSelect());
				if(dataItemValues.contains(value.getValue())){
					selected.add(nameValue);
				}
				spinnerArray.add(nameValue);
			}
			spinner.setItems(spinnerArray);
			for(NameValue selectedNameValue: selected){
				Log.d(TAG, "Setting selected value: " + selectedNameValue);
				spinner.setSelected(selectedNameValue);
			}
			widget = spinner;
		} else {
			TextView errorLabel = new TextView(context);
			errorLabel.setText("Unknown item: '" + item.getType() + "'");
			widget = errorLabel;
		}

		
		
		return new WidgetWrapper(widget, required, condition, readonly, hidden);
	}
}

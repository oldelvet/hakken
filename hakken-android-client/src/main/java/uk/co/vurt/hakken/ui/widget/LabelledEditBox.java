package uk.co.vurt.hakken.ui.widget;

import java.io.Serializable;

import uk.co.vurt.hakken.R;
import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class LabelledEditBox extends AbstractLabelledWidget implements Serializable{

	private EditText textBox;
	
	public LabelledEditBox(Context context, String labelText, String initialValue) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.labelled_edit_box, this);

		label = (TextView)findViewById(R.id.labelled_edit_box_label);
		textBox = (EditText)findViewById(R.id.labelled_edit_box_value);
		textBox.setSingleLine(true);
		this.setOrientation(VERTICAL);

		setLabel(labelText);
		
		if(initialValue != null){
			textBox.setText(initialValue);						
		}
	}
	
	public LabelledEditBox(Context context, String labelText, 
			String initialValue, String defaultValue, String hint, String length) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.labelled_edit_box, this);

		label = (TextView)findViewById(R.id.labelled_edit_box_label);
		textBox = (EditText)findViewById(R.id.labelled_edit_box_value);
		textBox.setSingleLine(true);
		this.setOrientation(VERTICAL);

		setLabel(labelText);
		
		if(length != null){
			InputFilter[] filterArray = new InputFilter[1];
			filterArray[0] = new InputFilter.LengthFilter(Integer.parseInt(length));
			textBox.setFilters(filterArray);
		}
		Log.d("CK Edit Box: ", "initialValue[" + initialValue + "] hint[" + hint + "]");
		
		
		// Show default values as hints. 
		// If initial value is the same as a default, then display this as a hint, the actual value
		// will be set when the save_and_validate method is called in the RunJob class.
		//
		// This helps to indicate which values have been changed by the user from the default, and avoids
		// having to delete a value before entering one. It also gets around the problem of default values
		// coming through from Activity Assistant, which are treated as actual values.
		if(initialValue != null) {
			if (defaultValue != null && initialValue.equals(defaultValue)) {
				textBox.setHint(initialValue);
			} else {
				textBox.setText(initialValue);
			}
		} else {
			if (defaultValue != null) {
				textBox.setHint(defaultValue);
			}			
		}

		// Set hint, this value will be overwritten when user types in text field
		if (hint != null) {
			textBox.setHint(hint);
		}

	}

	public String getValue(){
		return textBox.getText().toString();
	}
	
	public void setValue(String value){
		textBox.setText(value);
	}
	
	public void setKeyListener(KeyListener input){
		textBox.setKeyListener(input);
	}
	
	public void setLines(int lines){
		textBox.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
		textBox.setMinLines(lines);
		textBox.setGravity(Gravity.TOP);
		textBox.setVerticalScrollBarEnabled(true);
		textBox.setHorizontalScrollBarEnabled(false);
		textBox.setSingleLine(false);
	}
}

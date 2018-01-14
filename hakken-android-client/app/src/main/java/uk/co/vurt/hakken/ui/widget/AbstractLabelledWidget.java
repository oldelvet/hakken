package uk.co.vurt.hakken.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class AbstractLabelledWidget extends LinearLayout {

	protected TextView label;
	
	public AbstractLabelledWidget(Context context) {
		super(context);
		doCommonSetup();
	}

	public AbstractLabelledWidget(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		doCommonSetup();
	}

	public AbstractLabelledWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		doCommonSetup();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public AbstractLabelledWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		doCommonSetup();
	}

	private void doCommonSetup() {
		this.setOrientation(VERTICAL);
	}
	
	public void setLabel(String labelText){
		if(label != null){
			label.setText(labelText);
		}
	}
}

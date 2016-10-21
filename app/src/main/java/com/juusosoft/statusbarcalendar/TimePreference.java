/**
 * 
 */
package com.juusosoft.statusbarcalendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * 
 * @author Juuso Valkeejärvi
 * @version 3.7.2014
 * Select day and hour count
 */
public class TimePreference extends DialogPreference {
	private int days;
	private int hours;
	private int value;
	private NumberPicker pickerHour = null;
	private NumberPicker pickerDay = null;


	/**
	 *
	 * @param ctxt app context
	 */
	public TimePreference(Context ctxt) {
		this(ctxt, null);
	}


	/**
	 *
	 * @param ctxt app context
	 * @param attrs attributes
	 */
	public TimePreference(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}


	/**
	 * 
	 * @param ctxt app context
	 * @param attrs attributes
	 * @param defStyle style
	 */
	public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);
		setPositiveButtonText("OK");
		setNegativeButtonText("Cancel");
	}


	@Override
	protected View onCreateDialogView() {
        Context context = getContext();
		LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		parentParams.weight = 1;
		parentParams.gravity = Gravity.CENTER;
		LinearLayout.LayoutParams layoutText = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutText.gravity = Gravity.CENTER;
		
		LinearLayout masterView = new LinearLayout(context);
		masterView.setOrientation(LinearLayout.VERTICAL);
		masterView.setGravity(Gravity.CENTER);
		LinearLayout pickerView = new LinearLayout(context);
		pickerView.setOrientation(LinearLayout.HORIZONTAL);
		pickerView.setGravity(Gravity.CENTER);
		LinearLayout parentView = new LinearLayout(context);
		parentView.setOrientation(LinearLayout.HORIZONTAL);
		parentView.setGravity(Gravity.CENTER);

		LinearLayout dayView = new LinearLayout(context);
		dayView.setOrientation(LinearLayout.VERTICAL);
		TextView dayText = new TextView(context);
		dayText.setText(context.getString(R.string.days));
		dayText.setTextSize(18);
		pickerDay = new NumberPicker(context);
		pickerDay.setMaxValue(20);
		pickerDay.setMinValue(0);

		
		LinearLayout hourView = new LinearLayout(context);
		hourView.setOrientation(LinearLayout.VERTICAL);
		TextView hourText = new TextView(context);
		hourText.setText(context.getString(R.string.hours));
		hourText.setTextSize(18);
		pickerHour = new NumberPicker(context);
		pickerHour.setMaxValue(23);
		pickerHour.setMinValue(0);
		
		if (android.os.Build.VERSION.SDK_INT >= 21 ) {
			int dividerColor = fetchAccentColor();
			setDividerColor(pickerDay, dividerColor);
			setDividerColor(pickerHour, dividerColor);
		}
		

		TextView infoText = new TextView(context);
		infoText.setText(context.getString(R.string.get_all));
		infoText.setTextSize(15);

		dayView.addView(pickerDay, parentParams);
		dayView.addView(dayText, layoutText);
		hourView.addView(pickerHour, parentParams);
		hourView.addView(hourText, layoutText);
		pickerView.addView(dayView, parentParams);
		pickerView.addView(hourView, parentParams);
		pickerView.setPadding(50, 50, 50, 50);
		masterView.addView(pickerView);
		masterView.addView(infoText, layoutText);
		return (masterView);
	}
	
	private int fetchAccentColor() {
	    TypedValue typedValue = new TypedValue();
	    TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.colorAccent });
	    int color = a.getColor(0, 0);
	    a.recycle();
	    return color;
	}

	private void setDividerColor(NumberPicker picker, int color) {

	    java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
	    for (java.lang.reflect.Field pf : pickerFields) {
	        if (pf.getName().equals("mSelectionDivider")) {
	            pf.setAccessible(true);
	            try {
	                ColorDrawable colorDrawable = new ColorDrawable(color);
	                pf.set(picker, colorDrawable);
	            } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
					e.printStackTrace();
				}
	            break;
	        }
	    }
	}
	

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		pickerDay.setValue(days);
		pickerHour.setValue(hours);
	}


	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			days = pickerDay.getValue();
			hours = pickerHour.getValue();
			value = days * 24 + hours;
			setSummary(getSummary());
			if (callChangeListener(value)) {
				persistInt(value);
				notifyChanged();
			}
		}
	}


	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}


	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		if (restoreValue) {
			if (defaultValue == null) {
				value = getPersistedInt(48);
			} else {
				value = getPersistedInt(Integer.parseInt((String) defaultValue));
			}
		} else {
			if (defaultValue == null) {
				value = 48;
			} else {
				value = Integer.parseInt((String) defaultValue);
			}
		}
		days = value / 24;
		hours = value % 24;
		setSummary(getSummary());
	}


	@Override
	public CharSequence getSummary() {
		if (hours == 0 && days == 0) {
			return "Get all";
		}
		return days + " days, " + hours + " hours.";
	}
}

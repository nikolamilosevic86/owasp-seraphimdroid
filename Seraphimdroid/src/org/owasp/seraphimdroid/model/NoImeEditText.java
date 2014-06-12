package org.owasp.seraphimdroid.model;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class NoImeEditText extends EditText {

	public NoImeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onCheckIsTextEditor() {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}

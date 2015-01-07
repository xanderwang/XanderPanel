package com.xandy.expanddialog;

import com.xandy.expanddialog.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		View v = findViewById(R.id.test);
		v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show();
			}
		});
	}
	
	private void show() {
		ExpandDialog.Builder m = new ExpandDialog.Builder(this);
		m.setTitle("Expand Dialog");
		m.setMultiChoiceItems(new String[]{"1","2","3"}, new boolean[]{false,true,true}, null);
//		m.setSingleChoiceItems(new String[]{"1","2","3"}, 2, null);
		ExpandDialog s = m.create();
		s.show();
	}
}

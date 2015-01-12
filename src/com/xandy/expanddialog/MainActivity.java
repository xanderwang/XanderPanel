package com.xandy.expanddialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private int mButtonsId[] = {
			R.id.mulit_top ,
			R.id.mulit_bottom ,
			R.id.single_top ,
			R.id.single_bottom ,
			R.id.view_top ,
			R.id.view_bottom
	};
	
	private Context mContext ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
		for( int i = 0 ; i < mButtonsId.length ; i++ ) {
			View v = findViewById(mButtonsId[i]);
			v.setOnClickListener(mOnClickListener);
		}
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ExpandDialog.Builder mBuilder = new ExpandDialog.Builder(mContext);
			mBuilder.setTitle("Expand Dialog");
			switch (v.getId()) {
			case R.id.mulit_top : 
				mBuilder.setGravity(Gravity.TOP);
				mBuilder.setMultiChoiceItems(
						new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"}, 
						new boolean[]{false,true,true,false,true,true,false,true,true,false,true,true}, 
						null);
				break;
			case R.id.mulit_bottom : 
				mBuilder.setGravity(Gravity.BOTTOM);
				mBuilder.setMultiChoiceItems(
						new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"}, 
						new boolean[]{false,true,true,false,true,true,false,true,true,false,true,true}, 
						null);
				break;
			case R.id.single_top : 
				mBuilder.setGravity(Gravity.TOP);
				mBuilder.setSingleChoiceItems(
						new String[]{"1","2","3","1","2","3","1","2","3"}, 0, null);
				break;
			case R.id.single_bottom : 
				mBuilder.setGravity(Gravity.BOTTOM);
				mBuilder.setSingleChoiceItems(
						new String[]{"1","2","3","1","2","3","1","2","3"}, 0, null);
				break;
			case R.id.view_top : 
				mBuilder.setGravity(Gravity.TOP);
				mBuilder.setMessage("this is a messsage");
				break;
			case R.id.view_bottom : 
				mBuilder.setGravity(Gravity.BOTTOM);
				mBuilder.setMessage("this is a messsage");
				break;
			}
			
			mBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();
					
				}
			});
			
			ExpandDialog dialog = mBuilder.create();
			dialog.show();
		}
	};
	
}

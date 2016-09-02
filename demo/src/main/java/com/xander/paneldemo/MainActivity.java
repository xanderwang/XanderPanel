package com.xander.paneldemo;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.xander.panel.XanderPanel;

public class MainActivity extends AppCompatActivity {

    private int mButtonsId[] = {
            R.id.top_title_message,
            R.id.bottom_title_message,
            R.id.single_top,
            R.id.single_bottom,
            R.id.view_top,
            R.id.view_bottom,
            R.id.custom_top,
            R.id.custom_bottom
    };

    private Context mContext;
    private LayoutInflater mInflater;

    private static Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mInflater = LayoutInflater.from(mContext);

        for (int i = 0; i < mButtonsId.length; i++) {
            View v = findViewById(mButtonsId[i]);
            v.setOnClickListener(mOnClickListener);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            XanderPanel.Builder mBuilder = new XanderPanel.Builder(MainActivity.this);
            switch (v.getId()) {
                case R.id.top_title_message:
                    mBuilder.setTitle("Title")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("I am Message!!!")
                            .setGravity(Gravity.TOP)
                            .setCanceledOnTouchOutside(false);
                    break;
                case R.id.bottom_title_message:
                    mBuilder.setTitle("Title")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("I am Message!!!")
                            .setGravity(Gravity.BOTTOM)
                            .setCanceledOnTouchOutside(false);
                    break;
                case R.id.single_top:
                    mBuilder.setGravity(Gravity.TOP);
                    break;
                case R.id.single_bottom:
                    mBuilder.setGravity(Gravity.BOTTOM);
                    break;
                case R.id.view_top:
                    mBuilder.setGravity(Gravity.TOP);
                    mBuilder.setCanceledOnTouchOutside(true);
                    mBuilder.setMessage("this is a messsage");
                    break;
                case R.id.view_bottom:
                    mBuilder.setCanceledOnTouchOutside(true);
                    mBuilder.setGravity(Gravity.BOTTOM);
                    mBuilder.setMessage("this is a messsage");
                    break;
                case R.id.custom_top:
                    mBuilder.setGravity(Gravity.TOP);
                    mBuilder.setCanceledOnTouchOutside(true);
                    View mCustomView = mInflater.inflate(R.layout.custom_layout, null);
                    mBuilder.setView(mCustomView);
                    break;
                case R.id.custom_bottom:
                    mBuilder.setCanceledOnTouchOutside(true);
                    mBuilder.setGravity(Gravity.BOTTOM);
                    View mCustomViewBottom = mInflater.inflate(R.layout.custom_layout, null);
                    mBuilder.setView(mCustomViewBottom);
                    break;
            }

            mBuilder.setIcon(R.mipmap.ic_launcher);
            mBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (null == mToast) {
                        Toast.makeText(getApplicationContext(), "dismiss", Toast.LENGTH_SHORT).show();
                    } else {
                        mToast.setText("dismiss");
                    }
                }
            });

            XanderPanel xanderPanel = mBuilder.create();
            xanderPanel.show();
        }
    };

}

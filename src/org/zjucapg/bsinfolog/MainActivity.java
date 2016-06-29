package org.zjucapg.bsinfolog;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;

public class MainActivity extends Activity {

	private static final String BSINFO_SERVICE_CLASSNAME = "org.zjucapg.bsinfolog.BSInfoService";
	private static final String APP_DIR_NAME = "BSInfoLog";
	public static final String UPDATE_UI_TAG = "org.zjucapg.bsinfolog.UPDATE_UI";
	
	private TextView tv_bsinfo;
	private Button btn_record;
	private boolean isBsInfoServiceRunning = false;
	
	private Intent bsinfoServiceIntent = null;
	
	private UpdateUIBroadcastReceiver mReceiver = null;
	private boolean mIsReceiverRegistered = false;
	
	private int dbm = 0;
	private String type = "Unknow";
	private int mcc = -1;
	private int mnc = -1;
	private int lac = -1;
	private int cell_id = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		createSDCardDir();
		
		isBsInfoServiceRunning = isServiceRunning(getApplicationContext(), BSINFO_SERVICE_CLASSNAME);
		bsinfoServiceIntent = new Intent(getApplicationContext(), BSInfoService.class);
		
		initViews();
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if (!mIsReceiverRegistered) {
			if (mReceiver == null)
				mReceiver = new UpdateUIBroadcastReceiver();
			registerReceiver(mReceiver, new IntentFilter(UPDATE_UI_TAG));
			mIsReceiverRegistered = true;
		}
		
		dbm = BSInfoService.signalDbm;
		type = BSInfoService.networkType;
		mcc = BSInfoService.mcc;
		mnc = BSInfoService.mnc;
		lac = BSInfoService.lac;
		cell_id = BSInfoService.cellId;
		
		if (isBsInfoServiceRunning) {
			updateUI();	
		} else {
			tv_bsinfo.setText("空闲");
		}
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// 动态注册 Receiver 
		if (mIsReceiverRegistered) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
			mIsReceiverRegistered = false;
		}
	}


	private void initViews() {
		tv_bsinfo = (TextView) findViewById(R.id.et_bsinfo);
		btn_record = (Button) findViewById(R.id.btn_record);
		
		if (isBsInfoServiceRunning) {
			btn_record.setText("停止记录");
			//tv_bsinfo.setText("正在记录....");
		} else {
			btn_record.setText("开始记录");
			tv_bsinfo.setText("空闲");
		}
		
		btn_record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isBsInfoServiceRunning) {
					getApplicationContext().stopService(bsinfoServiceIntent);
				} else {
					getApplicationContext().startService(bsinfoServiceIntent);
				}
				
				isBsInfoServiceRunning = isServiceRunning(getApplicationContext(), BSINFO_SERVICE_CLASSNAME);
				
				if (isBsInfoServiceRunning) {
					btn_record.setText("停止记录");
					//tv_bsinfo.setText("正在记录....");
				} else {
					btn_record.setText("开始记录");
					tv_bsinfo.setText("空闲");
				}
				
//				TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//				Toast.makeText(getApplicationContext(), tm.getNetworkType() + "", Toast.LENGTH_LONG).show(); 
				
			}
		});
	}

	private static boolean isServiceRunning(Context ctx, String serviceName) {
		ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
			if(serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
	    
		return false;
	}
	
	private static void createSDCardDir() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			
			File path = new File(sdcardDir.getPath() + "/" + APP_DIR_NAME);
			if (!path.exists()) {
				path.mkdirs();
			} else {
				return;
			}
		}
	}
	
	public static String getSDCardDirStr() {
		String res = "";
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.getPath() + "/" + APP_DIR_NAME;
		}
		return res;
	}
	
	private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			type = intent.getStringExtra("type");
			dbm = intent.getIntExtra("dbm", 0);
			mcc = intent.getIntExtra("mcc", -1);
			mnc = intent.getIntExtra("mnc", -1);
			lac = intent.getIntExtra("lac", -1);
			cell_id = intent.getIntExtra("cell_id", -1);
			
			updateUI();
		}
		
	}
	
	private void updateUI() {
		tv_bsinfo.setText(
				"TYPE: " + type + "\ndBm: " + dbm + "\nMCC: " + mcc + 
				"\nMNC: " + mnc + "\nLAC: " + lac + "\nCell_ID: " + cell_id);
		
	}
}

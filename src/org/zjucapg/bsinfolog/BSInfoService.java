package org.zjucapg.bsinfolog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.opencsv.CSVWriter;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class BSInfoService extends Service {

	private final BSInfoStateListener bsinfoListener = new BSInfoStateListener();

	File csvFile = null;

	private static final int NOTIFICATION_ID = 8823;
	
	public static int signalDbm = 0;
	public static String networkType = "Unknown";
	public static int mcc = -1;
	public static int mnc = -1;
	public static int lac = -1;
	public static int cellId = -1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		System.out.println("服务启动");
		createInform();
		
		String Datetime;
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
		Datetime = dateformat.format(c.getTime());

		String path_str = MainActivity.getSDCardDirStr() + "/" + Datetime;
		File path = new File(path_str);
		if (!path.exists()) {
			path.mkdirs();
		}

		csvFile = new File(path_str + "/bsinfo.csv");

		Writer writer;
		try {
			writer = new FileWriter(csvFile, true);
			CSVWriter csvWriter = new CSVWriter(writer, ',');

			String[] strs0 = { "Base_Station_Signal_Strength", Datetime };
			csvWriter.writeNext(strs0);

			String[] strs1 = { "timestamp", "signal_strength[dbm]", "type[lte/cdma/gsm/evdo]", "MCC", "MNC", "LAC", "Cell_ID" };
			csvWriter.writeNext(strs1);

			csvWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		startSignalLevelListener();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		System.out.println("服务关闭");
		stopListener();
		
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nManager.cancel(NOTIFICATION_ID);
	}

	private void startSignalLevelListener() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
		tm.listen(bsinfoListener, events);
	}

	private void stopListener() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		tm.listen(bsinfoListener, PhoneStateListener.LISTEN_NONE);
	}

	public class BSInfoStateListener extends PhoneStateListener {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {

			long timestamp = System.currentTimeMillis();

			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			
			// 获取信号强度
			signalDbm = 0;
			networkType = "Unknown";  // 信号类型
			Method[] methods = android.telephony.SignalStrength.class.getMethods();
			int nwType = tm.getNetworkType();
			for (Method mthd : methods) {
				// Lte
				if (nwType == TelephonyManager.NETWORK_TYPE_LTE && mthd.getName().equals("getLteDbm")) {

					try {
						signalDbm = (Integer) mthd.invoke(signalStrength);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (signalDbm < -1) {
						networkType = "Lte";
						break;
					}
				}

				// Cdma
				if ((nwType == TelephonyManager.NETWORK_TYPE_CDMA) && mthd.getName().equals("getCdmaDbm")) {

					try {
						signalDbm = (Integer) mthd.invoke(signalStrength);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (signalDbm < -1) {
						networkType = "Cdma";
						break;
					}
				}

				// Evdo
				if ((nwType == TelephonyManager.NETWORK_TYPE_EVDO_0 || 
					nwType == TelephonyManager.NETWORK_TYPE_EVDO_A ||
					nwType == TelephonyManager.NETWORK_TYPE_EVDO_B) && 
						mthd.getName().equals("getEvdoDbm")) {

					try {
						signalDbm = (Integer) mthd.invoke(signalStrength);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (signalDbm < -1) {
						networkType = "Evdo";
						break;
					}
				}

				// Gsm
				if ((nwType == TelephonyManager.NETWORK_TYPE_EDGE ||
					nwType == TelephonyManager.NETWORK_TYPE_GPRS ||
					nwType == 17 ||
					nwType == 10) && 
						mthd.getName().equals("getGsmDbm")) {

					try {
						signalDbm = (Integer) mthd.invoke(signalStrength);
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (signalDbm < -1) {
						networkType = "Gsm";
						break;
					}
				}
			}
			
			String[] strs = {timestamp + "", signalDbm + "", networkType, mcc + "", mnc + "", lac + "", cellId + ""};
			
			Writer writer;
			try {
				writer = new FileWriter(csvFile, true);
				CSVWriter csvWriter = new CSVWriter(writer, ',');
				csvWriter.writeNext(strs);
				csvWriter.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String operator = tm.getNetworkOperator();
			if (!operator.isEmpty()) {
				mcc = Integer.parseInt(operator.substring(0, 3));
				mnc = Integer.parseInt(operator.substring(3));

				// 暂时只考虑 Gsm 基站
				GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
				lac = location.getLac();
				cellId = location.getCid();				
			} else {
				mcc = -1;
				mnc = -1;
				lac = -1;
				cellId = -1;
				networkType = "Unknown";
				signalDbm = Integer.MAX_VALUE; 
			}
			
			Intent itntUI = new Intent(MainActivity.UPDATE_UI_TAG);
			itntUI.putExtra("type", networkType);
			itntUI.putExtra("dbm", signalDbm);
			itntUI.putExtra("mcc", mcc);
			itntUI.putExtra("mnc", mnc);
			itntUI.putExtra("lac", lac);
			itntUI.putExtra("cell_id", cellId);
			
			getApplication().sendBroadcast(itntUI);
		}
	}
	
	public void createInform() {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pdi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
		
		Notification notification = new Notification(R.drawable.ic_launcher, "正在记录基站信息", System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), "BSInfoLog", "正在记录基站信息", pdi);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nManager.notify(NOTIFICATION_ID, notification);
	}
}

/* 
 * 2010-2017 (C) Antonio Redondo
 * http://antonioredondo.com
 * http://github.com/AntonioRedondo/AnotherMonitor
 *
 * Code under the terms of the GNU General Public License v3.
 *
 */

package org.anothermonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.os.Debug.MemoryInfo;
import android.os.Process;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.widget.Toast;

// math and model import
import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.special.Gamma;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.RunStats;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


public class ServiceReader extends Service {
	
	private boolean /*threadSuspended, */recording, firstRead = true, topRow = true;
	private int memTotal, pId, intervalRead, intervalUpdate, intervalWidth, maxSamples = 2002;
	private long workT, totalT, workAMT, total, totalBefore, work, workBefore, workAM, workAMBefore;
	private String s;
	private String[] sa;
	private List<Float> cpuTotal, cpuAM, Input_20, dis_auto, dis_RNN;
	private List<Integer> memoryAM;
	private List<Map<String, Object>> mListSelected; // Integer		 C.pId
												  // String		 C.pName
												  // Integer	 C.work
												  // Integer	 C.workBefore
												  // List<Sring> C.finalValue
	private List<String> memUsed, memAvailable, memFree, cached, threshold;
	private ActivityManager am;
	private Debug.MemoryInfo[] amMI;
	private ActivityManager.MemoryInfo mi;
	private NotificationManager mNM;

	private Notification mNotificationRead, mNotificationRecord;
	private BufferedReader reader;
	private BufferedWriter mW;
	private File mFile;
	private SharedPreferences mPrefs;
	private Runnable readRunnable = new Runnable() { // http://docs.oracle.com/javase/8/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
		@Override   
		public void run() {
			// The service makes use of an explicit Thread instead of a Handler because with the Threat the code is executed more synchronously.
			// However the ViewGraphic is drew with a Handler because the drawing code must be executed in the UI thread.
			Thread thisThread = Thread.currentThread();

			String MODEL_FILE = "file:///android_asset/model2.pb";  //Load tensorflow Model
			TensorFlowInferenceInterface tensorflow = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);

			String MODEL_FILE_RNN = "file:///android_asset/model_RNN_1.pb";  //Load tensorflow Model
			TensorFlowInferenceInterface tensorflow_RNN = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE_RNN);

			while (readThread == thisThread) {
				read(tensorflow, tensorflow_RNN);
				try {
					Thread.sleep(intervalRead);
/*					synchronized (this) {
						while (readThread == thisThread && threadSuspended)
							wait();
					}*/
				} catch (InterruptedException e) {
					break;
				}
				
				// The Runnable can be suspended and resumed with the below code:
//				threadSuspended = !threadSuspended;
//				if (!threadSuspended)
//					notify();
			}
		}
		
/*		public synchronized void stop() {
			readThread = null;
			notify();
		}*/
		
		
	};
	private volatile Thread readThread = new Thread(readRunnable, C.readThread);
	private BroadcastReceiver receiverStartRecord = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startRecord();
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	},
	receiverStopRecord = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopRecord();
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
	},
	receiverClose = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
			sendBroadcast(new Intent(C.actionFinishActivity));
			stopSelf();
		}
	};





	class ServiceReaderDataBinder extends Binder {
		ServiceReader getService() {
			return ServiceReader.this;
		}
	}
	
	
	
	
	
	@Override
	public void onCreate() {
		cpuTotal = new ArrayList<Float>(maxSamples);
		cpuAM = new ArrayList<Float>(maxSamples);
		memoryAM = new ArrayList<Integer>(maxSamples);
		memUsed = new ArrayList<String>(maxSamples);
		memAvailable = new ArrayList<String>(maxSamples);
		memFree = new ArrayList<String>(maxSamples);
		cached = new ArrayList<String>(maxSamples);
		threshold = new ArrayList<String>(maxSamples);
		Input_20 = new ArrayList<Float>(maxSamples*6);
		dis_auto = new ArrayList<Float>(maxSamples);
		dis_RNN = new ArrayList<Float>(maxSamples);
		dis_auto.add(0,0f);
		dis_RNN.add(0,0f);

		pId = Process.myPid();
		
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		amMI = am.getProcessMemoryInfo(new int[]{ pId });
		mi = new ActivityManager.MemoryInfo();
		
		mPrefs = getSharedPreferences(getString(R.string.app_name) + C.prefs, MODE_PRIVATE);
		intervalRead = mPrefs.getInt(C.intervalRead, C.defaultIntervalRead);
		intervalUpdate = mPrefs.getInt(C.intervalUpdate, C.defaultIntervalUpdate);
		intervalWidth = mPrefs.getInt(C.intervalWidth, C.defaultIntervalWidth);
		
		readThread.start();
		
//		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Constants.anotherMonitorEvent));
		registerReceiver(receiverStartRecord, new IntentFilter(C.actionStartRecord));
		registerReceiver(receiverStopRecord, new IntentFilter(C.actionStopRecord));
		registerReceiver(receiverClose, new IntentFilter(C.actionClose));

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		PendingIntent contentIntent =  TaskStackBuilder.create(this)
//				.addParentStack(ActivityMain.class)
//				.addNextIntent(new Intent(this, ActivityMain.class))
				.addNextIntentWithParentStack(new Intent(this, ActivityMain.class))
				.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIStartRecord = PendingIntent.getBroadcast(this, 0, new Intent(C.actionStartRecord), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIStopRecord = PendingIntent.getBroadcast(this, 0, new Intent(C.actionStopRecord), PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pIClose = PendingIntent.getBroadcast(this, 0, new Intent(C.actionClose), PendingIntent.FLAG_UPDATE_CURRENT);
		
		mNotificationRead = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notify_read2))
//				.setTicker(getString(R.string.notify_read))
				.setSmallIcon(R.drawable.icon_bw)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon, null))
				.setWhen(0) // Removes the time
				.setOngoing(true)
				.setContentIntent(contentIntent) // PendingIntent.getActivity(this, 0, new Intent(this, ActivityMain.class), 0)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notify_read2)))
				.addAction(R.drawable.icon_circle_sb, getString(R.string.menu_record), pIStartRecord)
				.addAction(R.drawable.icon_times_ai, getString(R.string.menu_close), pIClose)
				.build();
		
		mNotificationRecord = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notify_record2))
				.setTicker(getString(R.string.notify_record))
				.setSmallIcon(R.drawable.icon_recording_bw)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_recording, null))
				.setWhen(0)
				.setOngoing(true)
				.setContentIntent(contentIntent)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.notify_record2)))
				.addAction(R.drawable.icon_stop_sb, getString(R.string.menu_stop_record), pIStopRecord)
				.addAction(R.drawable.icon_times_ai, getString(R.string.menu_close), pIClose)
				.build();

//		mNM.notify(0, mNotificationRead);
		startForeground(10, mNotificationRead); // If not the AM service will be easily killed when a heavy-use memory app (like a browser or Google Maps) goes onto the foreground
	}
	
	
	
	
	
	@Override
	public void onDestroy() {
		if (recording)
			stopRecord();
		mNM.cancelAll();
		
		unregisterReceiver(receiverStartRecord);
		unregisterReceiver(receiverStopRecord);
		unregisterReceiver(receiverClose);
		
		try {
			readThread.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized (this) {
			readThread = null;
			notify();
		}
	}
	
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceReaderDataBinder();
	}
	
	
	
	
	
	@SuppressLint("NewApi")
	@SuppressWarnings("unchecked")
	private void read(TensorFlowInferenceInterface tensorflow, TensorFlowInferenceInterface tensorflow_RNN) {
//		String MODEL_FILE = "file:///android_asset/model2.pb";  //模型存放路径
//		TensorFlowInferenceInterface tensorflow = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);

		try {
			reader = new BufferedReader(new FileReader("/proc/meminfo"));
			s = reader.readLine();
			while (s != null) {
				// Memory is limited as far as we know
				while (memFree.size() >= maxSamples) {
					try{
						cpuTotal.remove(cpuTotal.size() - 1);
						cpuAM.remove(cpuAM.size() - 1);
					}catch (Throwable t){
						System.out.println("cpu_fail");
						System.out.println(cpuTotal.size());
					}

					memoryAM.remove(memoryAM.size() - 1);
					
					memUsed.remove(memUsed.size() - 1);
					memAvailable.remove(memAvailable.size() - 1);
					memFree.remove(memFree.size() - 1);
					cached.remove(cached.size() - 1);
					threshold.remove(threshold.size() - 1);
				}
				if (mListSelected != null && !mListSelected.isEmpty()) {
					List<Integer> l = (List<Integer>) (mListSelected.get(0)).get(C.pFinalValue);
					if (l != null && l.size() >= maxSamples)
						for (Map<String, Object> m : mListSelected) {
							((List<Integer>) m.get(C.pFinalValue)).remove(l.size() - 1);
							((List<Integer>) m.get(C.pTPD)).remove(((List<Integer>) m.get(C.pTPD)).size() - 1);
						}
				}
				if (mListSelected != null && !mListSelected.isEmpty()) {
					for (Map<String, Object> m : mListSelected) {
						List<Integer> l = (List<Integer>) m.get(C.pFinalValue);
						if (l == null)
							break;
						while (l.size() >= maxSamples)
							l.remove(l.size() - 1);
						l = (List<Integer>) m.get(C.pTPD);
						while (l.size() >= maxSamples)
							l.remove(l.size() - 1);
					}
				}
				
				// Memory values. Percentages are calculated in the ActivityMain class.
				if (firstRead && s.startsWith("MemTotal:")) {
					memTotal = Integer.parseInt(s.split("[ ]+", 3)[1]);
					firstRead = false;
				} else if (s.startsWith("MemFree:"))
					memFree.add(0, s.split("[ ]+", 3)[1]);
				else if (s.startsWith("Cached:"))
					cached.add(0, s.split("[ ]+", 3)[1]);
				
				s = reader.readLine();
			}
			reader.close();
			
			// http://stackoverflow.com/questions/3170691/how-to-get-current-memory-usage-in-android
			am.getMemoryInfo(mi);
			if (mi == null) { // Sometimes mi is null
				memUsed.add(0, String.valueOf(0));
				memAvailable.add(0, String.valueOf(0));
				threshold.add(0, String.valueOf(0));
			} else {
				memUsed.add(0, String.valueOf(memTotal - mi.availMem/1024));
				memAvailable.add(0, String.valueOf(mi.availMem/1024));
				threshold.add(0, String.valueOf(mi.threshold/1024));
			}
			
			memoryAM.add(amMI[0].getTotalPrivateDirty());
//			Log.d("TotalPrivateDirty", String.valueOf(amMI[0].getTotalPrivateDirty()));
//			Log.d("TotalPrivateClean", String.valueOf(amMI[0].getTotalPrivateClean()));
//			Log.d("TotalPss", String.valueOf(amMI[0].getTotalPss()));
//			Log.d("TotalSharedDirty", String.valueOf(amMI[0].getTotalSharedDirty()));
			
//			CPU usage percents calculation. It is possible negative values or values higher than 100% may appear.
//			http://stackoverflow.com/questions/1420426
//			http://kernel.org/doc/Documentation/filesystems/proc.txt
			if (Build.VERSION.SDK_INT < 26) {
				reader = new BufferedReader(new FileReader("/proc/stat"));
				sa = reader.readLine().split("[ ]+", 9);
				work = Long.parseLong(sa[1]) + Long.parseLong(sa[2]) + Long.parseLong(sa[3]);
				total = work + Long.parseLong(sa[4]) + Long.parseLong(sa[5]) + Long.parseLong(sa[6]) + Long.parseLong(sa[7]);
				reader.close();
			}
			
			reader = new BufferedReader(new FileReader("/proc/" + pId + "/stat"));
			sa = reader.readLine().split("[ ]+", 18);
			workAM = Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]);
			reader.close();
			
			if (mListSelected != null && !mListSelected.isEmpty()) {
				int[] arrayPIds = new int[mListSelected.size()];
				synchronized (mListSelected) {
					int n=0;
					for (Map<String, Object> p : mListSelected) {
						try {
							if (p.get(C.pDead) == null) {
								reader = new BufferedReader(new FileReader("/proc/" + p.get(C.pId) + "/stat"));
								arrayPIds[n] = Integer.valueOf((String) p.get(C.pId));
								++n;
								sa = reader.readLine().split("[ ]+", 18);
								p.put(C.work, (float) Long.parseLong(sa[13]) + Long.parseLong(sa[14]) + Long.parseLong(sa[15]) + Long.parseLong(sa[16]));
								reader.close();
							}
						} catch (FileNotFoundException e) {
							p.put(C.pDead, Boolean.TRUE);
							Intent intent = new Intent(C.actionDeadProcess);
							intent.putExtra(C.process, (Serializable) p);
							sendBroadcast(intent);
						}
					}
				}
				
				MemoryInfo[] mip = am.getProcessMemoryInfo(arrayPIds);
				int n = 0;
				for (Map<String, Object> entry : mListSelected) {
					List<Integer> l = (List<Integer>) entry.get(C.pTPD);
					if (l == null) {
						l = new ArrayList<Integer>();
						entry.put(C.pTPD, l);
					}
					if (entry.get(C.pDead) == null) {
//						if (mip[n].getTotalPrivateDirty() !=0
//								&& mip[n].getTotalPss() != 0
//								&& mip[n].getTotalSharedDirty() != 0) // To avoid dead processes
							l.add(0, mip[n].getTotalPrivateDirty());
							++n;
					} else l.add(0, 0);
				}
//				Log.d("MemoryInfo entries", String.valueOf(mip.length));
//				Log.d("List Selected entries", String.valueOf(mListSelected.size()));
				
//				Log.d("TotalSharedClean", String.valueOf(mi[0].getTotalSharedClean()));
//				Log.d("TotalSharedDirty", String.valueOf(mi[0].getTotalSharedDirty()));
//				Log.d("TotalPrivateClean", String.valueOf(mi[0].getTotalPrivateClean()));
//				Log.d("TotalPrivateDirty", String.valueOf(mi[0].getTotalPrivateDirty()));
//				Log.d("TotalPss", String.valueOf(mi[0].getTotalPss()));
//				Log.d("Pss", String.valueOf(Debug.getPss()));
//				Log.d("GlobalAllocSize", String.valueOf(Debug.getGlobalAllocSize()));
//				Log.d("NativeHeapSize", String.valueOf(Debug.getNativeHeapSize()/1024));
//				Log.d("NativeHeapAllocatedSize", String.valueOf(Debug.getNativeHeapAllocatedSize()/1024));
			}
			
			if (totalBefore != 0) {
				totalT = total - totalBefore;
				workT = work - workBefore;
				workAMT = workAM - workAMBefore;

				cpuTotal.add(0, restrictPercentage(workT * 100 / (float) totalT));
				cpuAM.add(0, restrictPercentage(workAMT * 100 / (float) totalT));

				if (mListSelected != null && !mListSelected.isEmpty()) {
					int workPT = 0;
					List<Float> l;
					
					synchronized (mListSelected) {
						for (Map<String, Object> p : mListSelected) {
							if (p.get(C.workBefore) == null)
								break;
							l = (List<Float>) p.get(C.pFinalValue);
							if (l == null) {
								l = new ArrayList<Float>();
								p.put(C.pFinalValue, l);
							}
							while (l.size() >= maxSamples)
								l.remove(l.size() - 1);
							
							workPT = (int) ((Float) p.get(C.work) - (Float) p.get(C.workBefore));
							l.add(0, restrictPercentage(workPT * 100 / (float) totalT));
						}
					}
				}
			}
			
			totalBefore = total;
			workBefore = work;
			workAMBefore = workAM;
			
			if (mListSelected != null && !mListSelected.isEmpty())
				for (Map<String, Object> p : mListSelected)
					p.put(C.workBefore, p.get(C.work));
			
			reader.close();

			if (recording)
				record(tensorflow, tensorflow_RNN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	private float restrictPercentage(float percentage) {
		if (percentage > 100)
			return 100;
		else if (percentage < 0)
			return 0;
		else return percentage;
	}


	private float[] gettensorflowinput(ArrayList<Float> myIntegers){

		float cpu_std = 3.962832f;
		float cpuam_std = 0.565675f;
		float memoryam_std = 0.000000f;
		float memused_std = 22950.313937f;
		float memavil_std = 22950.313937f;
		float memfree_std = 16205.462286f;
		float cached_std = 14519.936550f;
		float threshold_std = 0.000000f;

		float cpu_mean = 3.705264f;
		float cpuam_mean = 2.260897f;
		float memoryam_mean = 3.705264f;
		float memused_mean = 771590.796872f;
		float memavil_mean = 778957.203128f;
		float memfree_mean = 343344.630335f;
		float cached_mean = 435617.041246f;
		float threshold_mean = 0.000000f;

		float[] Input = new float[6];

		Input[0]  =  (myIntegers.get(0) - cpu_mean)/cpu_std;
		Input[2]  =  (myIntegers.get(3) - memused_mean)/memused_std;
		Input[3]  =  (myIntegers.get(4) - memavil_mean)/memavil_std;
		Input[4]  =  (myIntegers.get(5) - memfree_mean)/memfree_std;
		Input[5]  =  (myIntegers.get(6) - cached_mean)/cached_std;
		Input[1]  =  (myIntegers.get(1) - cpuam_mean)/cpuam_std;



		return Input;
	}


	public float[] mean_std(List<Float> x) {
		int m=x.size();
		float sum=0;
		for(int i=0;i<m;i++){//求和
			sum+=x.get(i);
		}
		float dAve=sum/m;//求平均值
		float dVar=0;
		for(int i=0;i<m;i++){//求方差
			dVar+=(x.get(i)-dAve)*(x.get(i)-dAve);
		}
		float[] std_mean = new float[2];
		std_mean[0] = dAve;
		std_mean[1] = (float) Math.sqrt(dVar/m);
		return std_mean;
	}

	public float[] mean_std_int(List<Integer> x) {
		int m=x.size();
		float sum=0;
		for(int i=0;i<m;i++){//求和
			sum+=x.get(i);
		}
		float dAve=sum/m;//求平均值
		float dVar=0;
		for(int i=0;i<m;i++){//求方差
			dVar+=(x.get(i)-dAve)*(x.get(i)-dAve);
		}
		float[] std_mean = new float[2];
		std_mean[0] = dAve;
		std_mean[1] = (float) Math.sqrt(dVar/m);
		return std_mean;
	}

	public float[] mean_std_str(List<String> x) {
		int m=x.size();
		float sum=0;
		for(int i=0;i<m;i++){//求和

			sum+=Float.parseFloat(x.get(i));
		}
		float dAve=sum/m;//求平均值
		float dVar=0;
		for(int i=0;i<m;i++){//求方差
			dVar+=(Float.parseFloat(x.get(i))-dAve)*(Float.parseFloat(x.get(i))-dAve);
		}
		float[] std_mean = new float[2];
		std_mean[0] = dAve;
		std_mean[1] = (float) Math.sqrt(dVar/m);
		return std_mean;
	}


	private float[] std(ArrayList<Float> myIntegers){

		float cpu_std = 3.962832f;
		float cpuam_std = 0.565675f;
		float memoryam_std = 0.000000f;
		float memused_std = 22950.313937f;
		float memavil_std = 22950.313937f;
		float memfree_std = 16205.462286f;
		float cached_std = 14519.936550f;
		float threshold_std = 0.000000f;

		float cpu_mean = 3.705264f;
		float cpuam_mean = 2.260897f;
		float memoryam_mean = 3.705264f;
		float memused_mean = 771590.796872f;
		float memavil_mean = 778957.203128f;
		float memfree_mean = 343344.630335f;
		float cached_mean = 435617.041246f;
		float threshold_mean = 0.000000f;

		float[] Input = new float[6];

		Input[0]  =  (myIntegers.get(0) - cpu_mean)/cpu_std;
		Input[2]  =  (myIntegers.get(3) - memused_mean)/memused_std;
		Input[3]  =  (myIntegers.get(4) - memavil_mean)/memavil_std;
		Input[4]  =  (myIntegers.get(5) - memfree_mean)/memfree_std;
		Input[5]  =  (myIntegers.get(6) - cached_mean)/cached_std;
		Input[1]  =  (myIntegers.get(1) - cpuam_mean)/cpuam_std;


		return Input;
	}

	private float distance(float[] input, float[] output){
		float dis = 0;
		for(int i =0; i<6;i++){
			dis += (input[i] - output[i])*(input[i] - output[i]);
		}
		return dis;
	}


	private float[] distance_list(float[] input, float[] output){
		float[] dis = new float[7];
		dis[0] = 0;
		for(int i =1; i<=6;i++){
			float temp = (input[i-1] - output[i-1])*(input[i-1] - output[i-1]);
			dis[i] = temp;
			dis[0] += temp;
		}
		return dis;
	}

	private int max(float[] list){
		int max_temp = 1;
		for(int i = 1;i < list.length;i++){
			if(list[i] > list[max_temp]){
				max_temp = i;
			}
		}
		return max_temp-1;
	}

	private float abs(float input){
		if(input<0){
			return -input;
		}
		else{
			return input;
		}
	}

	// InverseGamma as likelihood for update the gamma distrib parameters
	private double InverseGamma_beta(List<Float> list){
		double beta = 0.5;
		for(int i=0;i<list.size();i++){
			if(list.get(i)>0){
				beta+=1/list.get(i);
			}
		}
		return beta;
	}

	@SuppressWarnings("unchecked")
	private void record(TensorFlowInferenceInterface tensorflow, TensorFlowInferenceInterface tensorflow_RNN) {
		if (mW == null) {
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SeraphimDroid");
			dir.mkdirs();
			mFile = new File(dir, new StringBuilder().append(getString(R.string.app_name)).append("Record-").append(getDate()).append(".csv").toString());
			
			try {
				mW = new BufferedWriter(new FileWriter(mFile));
			} catch (IOException e) {
				notifyError(e);
				return;
			}
		}
		
		try {
			if (topRow) {
				StringBuilder sb = new StringBuilder()
						.append(getString(R.string.app_name))
						.append(" Record,Starting date and time:,")
						.append(getDate())
						.append(",Read interval (ms):,")
						.append(intervalRead)
						.append(",MemTotal (kB),")
						.append(memTotal)
						.append("\nTotal CPU usage (%),SeraphimDroid (Pid ").append(Process.myPid()).append(") CPU usage (%),SeraphimDroid Memory (kB)");
				// can change here to get a constance column name
				if (mListSelected != null && !mListSelected.isEmpty())
					for (Map<String, Object> p : mListSelected)
						sb.append(",").append(p.get(C.pAppName)).append(" (Pid ").append(p.get(C.pId)).append(") CPU usage (%)")
						  .append(",").append(p.get(C.pAppName)).append(" Memory (kB)");
				
				sb.append(",,Memory used (kB),Memory available (MemFree+Cached) (kB),MemFree (kB),Cached (kB),Threshold (kB), dis_auto, dis_RNN");
				
				mW.write(sb.toString());
				mNM.notify(10, mNotificationRecord);
				topRow = false;
			}
			// New change
			StringBuilder sb = new StringBuilder();
			try{
				sb.append("\n").append(cpuTotal.get(0))
						.append(",").append(cpuAM.get(0))
						.append(",").append(memoryAM.get(0));
			}catch (Throwable t){
				// if cpuTotal didn't exit
				sb.append("\n").append(0)
						.append(",").append(0)
						.append(",").append(memoryAM.get(0));
			}

			if (mListSelected != null && !mListSelected.isEmpty())
				for (Map<String, Object> p : mListSelected) {
					if (p.get(C.pDead) != null)
						sb.append(",DEAD,DEAD");
					else sb.append(",").append(((List<Integer>) p.get(C.pFinalValue)).get(0))
							.append(",").append(((List<Integer>) p.get(C.pTPD)).get(0));
				}

			sb.append(",")
					.append(",").append(memUsed.get(0))
					.append(",").append(memAvailable.get(0))
					.append(",").append(memFree.get(0))
					.append(",").append(cached.get(0))
					.append(",").append(threshold.get(0))
					.append(",").append(dis_auto.get(0))
					.append(",").append(dis_RNN.get(0));

			mW.write(sb.toString());
// 			change the output which could be analysed more in the future

			String mynumbers = sb.toString();
			String[] stringFlats = mynumbers.split(",");
			ArrayList<Float> myIntegers = new ArrayList<Float>();

			for(int i=0;i<stringFlats.length;i++){
				try {
					myIntegers.add(Float.parseFloat(stringFlats[i]));
				} catch(Exception e){

				}
			}

			// check the list size
			System.out.println(memoryAM.size());
			// calculate the std and mean to scale data
			float cpu_std = mean_std(cpuTotal)[1];
			float cpuam_std = mean_std(cpuAM)[1];
			float memoryam_std = mean_std_int(memoryAM)[1];
			float memused_std = mean_std_str(memUsed)[1];
			float memavil_std = mean_std_str(memAvailable)[1];
			float memfree_std = mean_std_str(memFree)[1];
			float cached_std = mean_std_str(cached)[1];

			float cpu_mean = mean_std(cpuTotal)[0];
			float cpuam_mean = mean_std(cpuAM)[0];
			float memoryam_mean = mean_std_int(memoryAM)[0];
			float memused_mean = mean_std_str(memUsed)[0];
			float memavil_mean = mean_std_str(memAvailable)[0];
			float memfree_mean = mean_std_str(memFree)[0];
			float cached_mean = mean_std_str(cached)[0];

			// Calculate the Autoencoder input
			float[] Input = new float[6];

			try {
				Input[0] = (cpuTotal.get(0) - cpu_mean) / cpu_std;
				Input[5] = (cpuAM.get(0) - cpuam_mean) / cpuam_std;
			}
			catch (Throwable t){
				Input[0] = 0;
				Input[5] = 0;
			}
			Input[1]  =  (Float.parseFloat(memUsed.get(0)) - memused_mean)/memused_std;
			Input[2]  =  (Float.parseFloat(memAvailable.get(0)) - memavil_mean)/memavil_std;
			Input[3]  =  (Float.parseFloat(memFree.get(0)) - memfree_mean)/memfree_std;
			Input[4]  =  (Float.parseFloat(cached.get(0)) - cached_mean)/cached_std;

			// Input_20 is for RNN input
			float RNN_dis = 0;
			float[] Output_RNN = new float[6];
			for(int i=0;i<6;i++) {Output_RNN[i]=0;}
			if (Input_20.size() > 20*6) {
				float[] Input_RNN = new float[6*20];
				for(int i=0;i<6*20;i++){
					Input_RNN[i] = Input_20.get(i);
				}

				tensorflow_RNN.feed("input",Input_RNN,20,6);
				String outputNode = "output/bias";
				String[] outputNodes = {outputNode};

				boolean enableStats = false;
				tensorflow_RNN.run(outputNodes, enableStats);
				tensorflow_RNN.fetch(outputNode, Output_RNN); // output is a preallocated float[] in the size of the expected output vector
				System.out.print("The RNN distance is: ");
				System.out.println(distance(Input,Output_RNN));

				RNN_dis = distance_list(Input,Output_RNN)[0];
			}

			// add new data to Input_20
			for(int i = 0;i<6; i++){
				Input_20.add(Input[i]);
			}

			tensorflow.feed("input",Input,1,6);


			String outputNode = "output/Relu";
			String[] outputNodes = {outputNode};
			float[] Output = new float[6];
			boolean enableStats = false;
			tensorflow.run(outputNodes, enableStats);
			tensorflow.fetch(outputNode, Output); // output is a preallocated float[] in the size of the expected output vector


			System.out.print("The autoencoder distance is: ");
			System.out.println(distance(Input,Output));


			float dis = distance(Input,Output);
			dis_auto.add(0,dis);
			dis_RNN.add(0,RNN_dis);

// 			Gamma distribution simulation

//			double mean = mean_std(dis_auto)[0];
//			double var = mean_std(dis_auto)[1]*mean_std(dis_auto)[1];
//
//			GammaDistribution gammaDistribution = new GammaDistribution(mean/var*mean, mean/var);
//			double Output1 = 1 - gammaDistribution.cumulativeProbability(dis);
//			System.out.println("suriv");
//			System.out.println(Output1);

			if(abs(dis)>50||abs(RNN_dis)>50||(abs(dis)+abs(RNN_dis)>70)){
				int dis_auto_anomaly = max(distance_list(Input,Output));
				int dis_RNN_anomaly = max(distance_list(Input,Output_RNN));
				String message = "The anomaly is from: ";
				switch(dis_auto_anomaly) {
					case 0:
						message = "The anomaly is from: CPU";
						break;
					case 1:
						message = "The anomaly is from: memUsed";
						break;
					case 2:
						message = "The anomaly is from: memAvailable";
						break;
					case 3:
						message = "The anomaly is from: memFree";
						break;
					case 4:
						message = "The anomaly is from: cached";
						break;
					case 5:
						message = "The anomaly is from: us";
						break;
					default:
						message = "Unknown anomaly";
						break;
				}


//				Intent activityIntent = new Intent(this, ServiceReader.class);
//				PendingIntent contentIntent = (PendingIntent) PendingIntent.getActivities(this,
//						0, new Intent[]{activityIntent},0);
				PendingIntent contentIntent =  TaskStackBuilder.create(this)
//				.addParentStack(ActivityMain.class)
//				.addNextIntent(new Intent(this, ActivityMain.class))
						.addNextIntentWithParentStack(new Intent(this, ActivityMain.class))
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				Intent broadcastIntent = new Intent(this,NotificationReceiver.class);
				broadcastIntent.putExtra("toastMessage",message);
				PendingIntent actionIntent = PendingIntent.getBroadcast(this,
						0,broadcastIntent,PendingIntent.FLAG_UPDATE_CURRENT);

				NotificationManagerCompat notificationManager;

				notificationManager = NotificationManagerCompat.from(getApplicationContext());

				Notification notification = new NotificationCompat.Builder(this)
						.setContentTitle("Warning")
						.setContentText("Anomaly detected!!")
						.setSmallIcon(R.drawable.icon_recording)
						.setDefaults(Notification.DEFAULT_ALL)
						.setPriority(Notification.PRIORITY_HIGH)
						.setContentIntent(contentIntent)
						.setAutoCancel(true)
						.addAction(R.drawable.icon_recording,"More Info",actionIntent)
						.build();

				notificationManager.notify(null, 0, notification);

			}

		} catch (IOException e) {
			notifyError(e);
		}
	}
	
	
	
	
	
	void startRecord() {
		if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			Toast.makeText(this, getString(R.string.w_main_storage_permission), Toast.LENGTH_LONG).show();
			return;
		}
		recording = true;
		sendBroadcast(new Intent(C.actionSetIconRecord));
	}
	
	void stopRecord() {
		recording = false;
		sendBroadcast(new Intent(C.actionSetIconRecord));
		try {
			mW.flush();
			mW.close();
			mW = null;
			
			// http://stackoverflow.com/questions/13737261/nexus-4-not-showing-files-via-mtp
//			MediaScannerConnection.scanFile(this, new String[] { mFile.getAbsolutePath() }, null, null);
			// http://stackoverflow.com/questions/5739140/mediascannerconnection-produces-android-app-serviceconnectionleaked
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(mFile)));

			Toast.makeText(this, new StringBuilder().append(getString(R.string.app_name)).append("Record-").append(getDate()).append(".csv ")
					.append(getString(R.string.notify_toast_saved))
					.append(" " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Seraphimdroid"), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.notify_toast_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		topRow = true;
		mNM.notify(10, mNotificationRead);
	}
	
	boolean isRecording() {
		return recording;
	}
	
	
	
	
	
	void notifyError(final IOException e) {
		e.printStackTrace();
		if (mW != null)
			stopRecord();
		else {
			recording = false;
			sendBroadcast(new Intent(C.actionSetIconRecord));
			
			// http://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ServiceReader.this, getString(R.string.notify_toast_error_2) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
			
			mNM.notify(10, mNotificationRead);
		}
	}
	
	
	
	
	
	private String getDate() {
		Calendar c = Calendar.getInstance();
		DecimalFormat df = new DecimalFormat("00");
		return new StringBuilder()
				.append(df.format(c.get(Calendar.YEAR))).append("-")
				.append(df.format(c.get(Calendar.MONTH) + 1)).append("-")
				.append(df.format(c.get(Calendar.DATE))).append("-")
				.append(df.format(c.get(Calendar.HOUR_OF_DAY))).append("-")
				.append(df.format(c.get(Calendar.MINUTE))).append("-")
				.append(df.format(c.get(Calendar.SECOND))).toString();
	}
	
	
	
	
	
	void setIntervals(int intervalRead, int intervalUpdate, int intervalWidth) {
		this.intervalRead = intervalRead;
		this.intervalUpdate = intervalUpdate;
		this.intervalWidth = intervalWidth;
	}
	
	
	
	
	
	List<Map<String, Object>> getProcesses() {
		return mListSelected != null && !mListSelected.isEmpty() ? mListSelected : null;
	}
	
	void addProcess(Map<String, Object> process) {
		// Integer	   C.pId
		// String	   C.pName
		// Integer	   C.work
		// Integer	   C.workBefore
		// List<Sring> C.finalValue
		if (mListSelected == null)
			mListSelected = Collections.synchronizedList(new ArrayList<Map<String, Object>>());
		mListSelected.add(process);
	}
	
	void removeProcess(Map<String, Object> process) {
		synchronized (mListSelected) {
			Iterator<Map<String, Object>> i = mListSelected.iterator();
			while (i.hasNext())
				if (i.next().get(C.pId).equals(process.get(C.pId))) {
					i.remove();
					Log.i(getString(R.string.w_processes_dead_notification), (String) process.get(C.pName));
				}
		}
	}
	
	
	
	
	
	int getIntervalRead() {
		return intervalRead;
	}
	
	int getIntervalUpdate() {
		return intervalUpdate;
	}
	
	int getIntervalWidth() {
		return intervalWidth;
	}
	
	
	
	
	
	List<Float> getCPUTotalP() {
		return cpuTotal;
	}
	
	List<Float> getCPUAMP() {
		return cpuAM;
	}
	
	List<Integer> getMemoryAM() {
		return memoryAM;
	}
	
	int getMemTotal() {
		return memTotal;
	}
	
	List<String> getMemUsed() {
		return memUsed;
	}
	
	List<String> getMemAvailable() {
		return memAvailable;
	}
	
	List<String> getMemFree() {
		return memFree;
	}
	
	List<String> getCached() {
		return cached;
	}
	
	List<String> getThreshold() {
		return threshold;
	}
}

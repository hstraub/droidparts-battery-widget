/*
 * Copyright (C) 2010 Sergej Shafarenka, beworx.com
 * Portions Copyright (C) 2012 Herbert Straub, herbert@linuxhacker.at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidparts.battery_widget;

import static org.droidparts.battery_widget.BatteryWidget.TAG;
import static org.droidparts.battery_widget.BatteryWidgetProvider.EXT_UPDATE_WIDGETS;

import java.lang.reflect.Field;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import at.linuxhacker.battery.BatteryStatusEvent;
import at.linuxhacker.battery.EventsTracer;

public class BatteryService extends Service {
	// cached values
	int mBatteryChargeLevel = -1; 
	boolean mChargerConnected;
	boolean mScreenOn = false;
	EventsTracer eventsTracer = null;

	private ScreenStateService mScreenStateReceiver;

	private class BatteryStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				
				// see constants in BatteryManager
				
				int rawlevel = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, -1 );
				int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE, -1);
				int plugged = intent.getIntExtra( BatteryManager.EXTRA_PLUGGED, 0 );
				int status = intent.getIntExtra( BatteryManager.EXTRA_STATUS, -1 );
				int level = 0;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				mBatteryChargeLevel = level;
				mChargerConnected = plugged > 0 && level < 100 /* not charging if 100%*/;
	
				BatteryStatusEvent batteryStatus = new BatteryStatusEvent( level, status, plugged, BatteryService.this.mScreenOn );
				BatteryService.this.eventsTracer.addBatteryChangedEvent( batteryStatus );
				
			}
			
			if ( BatteryService.isScreenOn( context ) == true ) {
				BatteryWidget.updateWidgets(context, mBatteryChargeLevel, mChargerConnected);
			}
		}
	}

	private class ScreenStateService extends BroadcastReceiver {
		private BatteryStateReceiver mBatteryStateReceiver;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				Log.d(TAG, "screen is ON");
				BatteryService.this.mScreenOn = true; // FIXME
				BatteryService.this.eventsTracer.addScreenOnEvent( );
				BatteryWidget.updateWidgets(context, mBatteryChargeLevel, mChargerConnected);
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				Log.d(TAG, "screen is OFF");
				BatteryService.this.mScreenOn = false; // FIXME: was ist damit
				BatteryService.this.eventsTracer.addScreenOffEvent( );
			}
		}

		public void registerBatteryReceiver(boolean register, Context context) {
			if (register) {
				if (mBatteryStateReceiver == null) {
					mBatteryStateReceiver = new BatteryStateReceiver();
			        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
					context.registerReceiver(mBatteryStateReceiver, filter);
				}
			} else if (mBatteryStateReceiver != null) {
				context.unregisterReceiver(mBatteryStateReceiver);
				mBatteryStateReceiver = null;
			}

			Log.d(TAG, "battery receiver " + (register ? "ON" : "OFF (sleeping)"));
		}

		public void registerScreenReceiver(boolean register, Context context) {
			if (register) {
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_SCREEN_ON);
				filter.addAction(Intent.ACTION_SCREEN_OFF);
				context.registerReceiver(this, filter);
				registerBatteryReceiver(true, context);
			} else {
				registerBatteryReceiver(false, context);
				context.unregisterReceiver(this);
			}
			
			Log.d( TAG, "registerScreenReceiver " + ( register ? "ON" : "OFF (sleeping)" ) );
		}
	}

	// FIXME: onStart is deprecated -> implement onStartCommand
	public void onStart(Intent intent, int startId) {
		this.eventsTracer = new EventsTracer( this );
		
		if (mScreenStateReceiver == null) {
			mScreenStateReceiver = new ScreenStateService();


			if (isScreenOn(this)) {
				this.mScreenOn = BatteryService.isScreenOn( this );
			}

			mScreenStateReceiver.registerScreenReceiver(true, this);
			Log.d(TAG, "started");
		}

		Bundle ext = intent.getExtras();
		if (ext != null && ext.getBoolean(EXT_UPDATE_WIDGETS, false)) {
			BatteryWidget.updateWidgets(this, mBatteryChargeLevel, mChargerConnected);
		}
	}

	public void onDestroy() {
		if (mScreenStateReceiver != null) {
			mScreenStateReceiver.registerScreenReceiver(false, this);
			mScreenStateReceiver = null;
		}

		Log.d(TAG, "stopped");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static void requestWidgetUpdate(Context context) {
		Intent serviceIntent = new Intent(context, BatteryService.class);
		serviceIntent.putExtra(EXT_UPDATE_WIDGETS, true);
		context.startService(serviceIntent);
	}

	private static boolean isScreenOn(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		try {
			if (sdkVersion >= 7) {
				// >= 2.1
				Boolean bool = (Boolean) PowerManager.class.getMethod("isScreenOn").invoke(pm);
				return bool.booleanValue();
			} else {
				// < 2.1
				Field field = PowerManager.class.getDeclaredField("mService");
				field.setAccessible(true);
				Object/* IPowerManager */service = field.get(pm);
				Long timeOn = (Long) service.getClass().getMethod("getScreenOnTime").invoke(service);
				return timeOn > 0;
			}
		} catch (Exception e) {
			Log.e(TAG, "cannot check whether screen is on", e);
			return true;
		}
	}
}

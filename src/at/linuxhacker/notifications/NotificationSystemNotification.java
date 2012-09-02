/*
 * Copyright (C) 2012 Herbert Straub, herbert@linuxhacker.at
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

package at.linuxhacker.notifications;

import org.droidparts.battery_widget.BatteryStatisticActivity;
import org.droidparts.battery_widget.BatteryWidget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationSystemNotification {
	private Context context = null;

	public NotificationSystemNotification(Context context) {
		this.context = context;
	}

	public void displayNotification( String text ) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = ( NotificationManager ) context.getSystemService( ns );

		int icon = org.droidparts.battery_widget.R.drawable.ic_1_1;
		String tickerText = "hallo";
		long when = System.currentTimeMillis( );

		Notification notification = new Notification( icon, tickerText, when );

		String contentTitle = "Test Notification";
		String contentText = "Hallo World";
		// Intent notificationIntent = new Intent( Intent.ACTION_MAIN, BatteryStatisticActivity.class );
		// Intent notificationIntent = new Intent( Intent.ACTION_VIEW, BatteryStatisticActivity.class );
		Intent notificationIntent = new Intent( Intent.ACTION_VIEW );
		notificationIntent.setClass( context, BatteryStatisticActivity.class );
		notificationIntent.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		PendingIntent contentIntent = PendingIntent.getActivity( context, 0, notificationIntent, 0);

		notification.setLatestEventInfo( context.getApplicationContext( ), contentTitle, contentText,
				contentIntent );
		
		notificationManager.notify( 3, notification );
		Log.d ( BatteryWidget.TAG, "notification sent" );

	}
}

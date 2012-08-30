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

package at.linuxhacker.battery;

import android.content.Context;
import at.linuxhacker.notifications.NotificationDistributor;

public class EventFilter {
	private static final long SPEAKER_SILENT_INTERVALL = 1800;
	
	private Context context = null;
	private NotificationDistributor notificationDistributor = null;
	long lastSpokenTimestamp = 0; // Unix Timestamp in Seconds

	public EventFilter(Context context) {
		this.context = context;
		
		this.notificationDistributor = new NotificationDistributor( context );
	}
	
	public void processPowerPluggedInEvent( BatteryStatusEvent batteryStatusEvent ) {
		String text;
		int minutesToFull = batteryStatusEvent.getMinutesToFull( );
		
		if( minutesToFull > 0 ) {
			text = "Ladezeit " + minutesToFull + " Minuten";
		} else {
			text = "Vollständig aufgeladen";
		}
		this.notificationDistributor.displayTextOnScreenOverlay( text );
		this.notificationDistributor.speakText( text );
		this.markSpokenTimestamp( );
	}
	
	public void processPowerPluggedOutEvent( BatteryStatusEvent batteryStatusEvent ) {
		if( batteryStatusEvent.getLevel( ) != 100 ) {
			String text = "Nicht vollständig aufgeladen. Ladestand "
					+ batteryStatusEvent.getLevel( ) + "%";
			this.notificationDistributor.speakText( text );
			text = "Ladestand " + batteryStatusEvent.getLevel( ) + "%";
			this.notificationDistributor.displayTextOnScreenOverlay( text );
		} else {
			String text = "Vollständig aufgeladen";
			this.notificationDistributor.speakText( text );
			this.notificationDistributor.displayTextOnScreenOverlay( text );
		}
		this.markSpokenTimestamp( );
	}
	
	public void processScreenOnEvent( BatteryStatusEvent batteryStatusEvent ) {
		String text = "Ladestand " + batteryStatusEvent.getLevel( ) + "%";
		
		/*
		 * FIXME: We can produce two events in case of plugging the power cable:
		 * 1. we get a batteryUpdate Event
		 * 2. we get a ScreenOnEvent
		 * 
		 * What should we do?
		 * 
		 * The current solution:
		 * The Object NotificationScreenOverlay process one Message and
		 * drops all later one, until the first message process finished
		 * 
		 */

		if ( ! this.isSpeakerSilentInterval( ) ) {
			this.notificationDistributor.speakText( text );
			this.markSpokenTimestamp( );
		}
		this.notificationDistributor.displayTextOnScreenOverlay( text );
	}

	private void markSpokenTimestamp( ) {
		this.lastSpokenTimestamp = System.currentTimeMillis( ) / 1000;
	}
	
	private boolean isSpeakerSilentInterval( ) {
		if ( System.currentTimeMillis( ) / 1000
				- this.lastSpokenTimestamp
				> EventFilter.SPEAKER_SILENT_INTERVALL ) {
			return false;
		} else {
			return true;
		}
	}
}

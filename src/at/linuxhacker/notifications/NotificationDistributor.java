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

import android.content.Context;

public class NotificationDistributor {
	private Context context = null;
	private NotificationTextToSpeech notificationTextToSpeech = null;
	private NotificationScreenOverlay notificationScreenOverlay = null;
	private NotificationSystemNotification notificationSystemNotification = null;
	
	public NotificationDistributor(Context context) {
		this.context = context;
		
		this.notificationTextToSpeech = new NotificationTextToSpeech( this.context );
		this.notificationScreenOverlay = new NotificationScreenOverlay( this.context );
		this.notificationSystemNotification = new NotificationSystemNotification( context );
	}
	
	public void speakText( String textToSpeak ) {
		this.notificationTextToSpeech.speakText( textToSpeak );
	}
	
	public void displayTextOnScreenOverlay( String textToDisplay ) {
		this.notificationScreenOverlay.displayText( textToDisplay );
	}
	
	public void displaySystemNotification( String text ) {
		this.notificationSystemNotification.displayNotification( text );
	}
}

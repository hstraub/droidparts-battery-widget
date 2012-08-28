package at.linuxhacker.notifications;

import android.content.Context;

public class NotificationDistributor {
	private Context context = null;
	private NotificationTextToSpeech notificationTextToSpeech = null;
	private NotificationScreenOverlay notificationScreenOverlay = null;
	
	public NotificationDistributor(Context context) {
		this.context = context;
		
		this.notificationTextToSpeech = new NotificationTextToSpeech( this.context );
		this.notificationScreenOverlay = new NotificationScreenOverlay( this.context );
	}
	
	public void speakText( String textToSpeak ) {
		this.notificationTextToSpeech.speakText( textToSpeak );
	}
	
	public void displayTextOnScreenOverlay( String textToDisplay ) {
		this.notificationScreenOverlay.displayText( textToDisplay );
	}
}

package at.linuxhacker.battery;

import android.content.Context;
import at.linuxhacker.notifications.NotificationDistributor;

public class EventFilter {
	private Context context = null;
	private NotificationDistributor notificationDistributor = null;

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
		this.notificationDistributor.speakText( text );
		this.notificationDistributor.displayTextOnScreenOverlay( text );
	}
		
}

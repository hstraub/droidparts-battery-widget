package at.linuxhacker.notifications;

import static org.droidparts.battery_widget.BatteryWidget.TAG;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import at.linuxhacker.battery.EventsTracer;

public class NotificationScreenOverlay {
	private Context context = null;
	private boolean currentlyOverlayActiveFlag = false;
	private NotificationScreenOverlayView mView = null;
	
	public NotificationScreenOverlay(Context context) {
		this.context = context;
	}
	
	public void displayText( String textToDisplay ) {
		/*
		 * FIXME We need a Queue system. Look at the textToSpeach implementation
		 */
		
		if ( this.currentlyOverlayActiveFlag ) {
			Log.d( TAG, "showBattaryInformationOverlay: forget: " + textToDisplay );
			return;
		}
		
		Log.d( TAG, "showBattaryInformationOverlay: " + textToDisplay );
		this.currentlyOverlayActiveFlag = true; // We are active, reset in Timer
		
		this.mView = new NotificationScreenOverlayView( this.context );
		this.mView.setDisplayText( textToDisplay );

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT );
		params.gravity = Gravity.RIGHT | Gravity.TOP;
		params.setTitle( "Load Average" );
		WindowManager wm = ( WindowManager ) this.context
				.getSystemService( Context.WINDOW_SERVICE );
		wm.addView( mView, params );
		this.countDownDisplayTimer.start( );
	}

	private CountDownTimer countDownDisplayTimer = new CountDownTimer( 5000, 5000 ) {
		public void onTick( long millisUntilFinished ) {

		}

		public void onFinish( ) {
			NotificationScreenOverlay.this.CountDownTimerDisplayTimerFinished( );
		}
	};

	protected void CountDownTimerDisplayTimerFinished( ) {
		WindowManager wm = ( WindowManager ) this.context
				.getSystemService( Context.WINDOW_SERVICE );
		wm.removeView( this.mView );
		this.currentlyOverlayActiveFlag = false;
		Log.d( TAG, "CountDownTimerDisplayTimerFinished" );
	}
}

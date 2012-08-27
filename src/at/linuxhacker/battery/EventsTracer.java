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

import static org.droidparts.battery_widget.BatteryWidget.TAG;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

public class EventsTracer implements TextToSpeech.OnInitListener {

	private Context context;
	private static final long SILENT_SECONDS_AFTER_SCREEN_OFF = 1800;
	private BatteryStatusEvent actualBatteryStatus = null;
	private BatteryStatusEvent previousBatteryStatus = null;
	private ChargeRate chargeRate;
	boolean isScreenOnFlag;
	public boolean pluggedState = false;
	public String statusText = "leer";
	public boolean batteryFull = false;
	public boolean isCharging;
	private ArrayList<ScreenStatusEvent> screenStatus = new ArrayList<ScreenStatusEvent>( );
	private TextToSpeech textToSpeech;
	BatteryInformationView mView = null;
	private boolean nextScreenOnPluggedStateRisingEdgeFlag = false;
	private boolean nextScreenOnPluggedStateFallingEdgeFlag;
	private boolean currentlyOverlayActiveFlag;

	public EventsTracer(Context context) {
		this.context = context;

		if ( this.textToSpeech == null ) {
			this.textToSpeech = new TextToSpeech( this.context, this );
		}
	}

	public void addBatteryChangedEvent( BatteryStatusEvent batteryStatus ) {
		Log.d( TAG,
				"battery state: level=" + batteryStatus.getLevel( )
						+ ", plugged=" + batteryStatus.getPlugged( )
						+ ", status=" + batteryStatus.getStatus( )
						+ ", mScreenOn=" + batteryStatus.isScreenOn( )
						+ ", timestamp=" + batteryStatus.getTimestamp( ) );

		this.previousBatteryStatus = this.actualBatteryStatus;
		this.actualBatteryStatus = batteryStatus;

		this.calculateChargeRate( );
		this.checkNotificationForUpdateBatteryStatus( );
	}

	public void setScreenStatusToOn( boolean screenOnFlag ) {
		this.isScreenOnFlag = screenOnFlag;
	}

	public void addScreenOnEvent( ) {
		this.screenStatus.add( new ScreenStatusEvent( System
				.currentTimeMillis( ) / 1000, true ) );

		if ( this.actualBatteryStatus != null ) {
			// yes, actualBatteryStatus could be null, after startup the widget
			// and the service. We have to check, if we have the necessary data

			this.checkNotificationForScreenOn( );
		}
	}

	public void addScreenOffEvent( ) {
		this.screenStatus.add( new ScreenStatusEvent( System
				.currentTimeMillis( ) / 1000, false ) );

		// FIXME: we should find a better place
		this.nextScreenOnPluggedStateFallingEdgeFlag = false;
		this.nextScreenOnPluggedStateRisingEdgeFlag = false;
	}

	private long findLastScreenOffTimestamp( ) {
		long timestamp = 0;
		int j = 0;
		for ( int i = this.screenStatus.size( ) - 1; i >= 0; i-- ) {
			if ( !this.screenStatus.get( i ).isScreenOnFlag( ) ) {
				Log.d( TAG,
						"ja(" + i + ") timestamp="
								+ this.screenStatus.get( i ).getTimestamp( )
								+ " und j=" + j );
				// Event ordering is essential!! FIXME: this is not perfect
				if ( j < 0 ) {
					j++;
				} else {
					timestamp = this.screenStatus.get( i ).getTimestamp( );
					break;
				}
			}
		}
		Log.d( TAG, "return Timestamp=" + timestamp );
		return timestamp;
	}

	private void calculateChargeRate( ) {
		if ( this.actualBatteryStatus.getStatus( ) == BatteryManager.BATTERY_STATUS_FULL ) {
			this.isCharging = false;
		} else if ( this.actualBatteryStatus.getStatus( ) == BatteryManager.BATTERY_STATUS_CHARGING ) {
			this.isCharging = true;
		}

		if ( this.actualBatteryStatus != null
				&& this.previousBatteryStatus != null ) {
			if ( this.actualBatteryStatus.getStatus( ) == this.previousBatteryStatus
					.getStatus( ) ) {
				Log.d( TAG, "actualBatteryStatus: update" );
				if ( this.chargeRate == null ) {
					this.chargeRate = new ChargeRate(
							this.actualBatteryStatus.getStatus( ) == BatteryManager.BATTERY_STATUS_DISCHARGING ? ChargeRate.TYPE_DISCHARGE
									: ChargeRate.TYPE_CHARGE,
							this.actualBatteryStatus.getTimestamp( ),
							this.actualBatteryStatus.getLevel( ) );
				} else {
					this.chargeRate.updateChargeRate(
							this.actualBatteryStatus.getTimestamp( ),
							this.actualBatteryStatus.getLevel( ) );
				}
			} else if ( this.actualBatteryStatus.getStatus( ) == BatteryManager.BATTERY_STATUS_CHARGING ) {
				Log.d( TAG, "updateBatteryStatus: new TYPE_CHARGE" );
				this.chargeRate = new ChargeRate( ChargeRate.TYPE_CHARGE,
						this.actualBatteryStatus.getTimestamp( ),
						this.actualBatteryStatus.getLevel( ) );
			} else if ( this.actualBatteryStatus.getStatus( ) == BatteryManager.BATTERY_STATUS_DISCHARGING ) {
				Log.d( TAG, "updateBatteryStatus: new TYPE_DISCHARGE" );
				this.chargeRate = new ChargeRate( ChargeRate.TYPE_DISCHARGE,
						this.actualBatteryStatus.getTimestamp( ),
						this.actualBatteryStatus.getLevel( ) );
			}

			Log.d( TAG, "updateBatteryStatus, chargeRate.valid="
					+ this.chargeRate.valid );
			if ( this.chargeRate.valid ) {
				Log.d( TAG,
						"Charge Rate: "
								+ this.chargeRate.getChargeRatePerMinute( )
								+ "% per Minute" );
			}
		}
	}

	public void checkNotificationForUpdateBatteryStatus( ) {

		// Check for plugged state change on the rising edge
		// Set only Flag - notifications on ScreenOnEvent
		if ( ( this.previousBatteryStatus == null || this.previousBatteryStatus
				.getPlugged( ) == 0 )
				&& this.actualBatteryStatus.getPlugged( ) > 0 ) {
			this.nextScreenOnPluggedStateRisingEdgeFlag = true;
			this.nextScreenOnPluggedStateFallingEdgeFlag = false;
		}

		// Check for unplugged state change on the falling edge
		if ( ( this.previousBatteryStatus == null || this.previousBatteryStatus
				.getPlugged( ) > 0 )
				&& this.actualBatteryStatus.getPlugged( ) == 0 ) {
			this.nextScreenOnPluggedStateFallingEdgeFlag = true;
			this.nextScreenOnPluggedStateRisingEdgeFlag = false;
		}

		// We are in plugged state
		if ( this.actualBatteryStatus.getPlugged( ) > 0 ) {
			this.pluggedState = true;
		} else {
			this.pluggedState = false;
		}

		// Now, the screen can be always on.
		// In this case, we call the notification routine immediately
		if ( this.actualBatteryStatus.isScreenOn( ) ) {
			this.checkNotificationForScreenOn( );
		}
	}

	private void checkNotificationForScreenOn( ) {
		if ( this.nextScreenOnPluggedStateRisingEdgeFlag ) {
			this.nextScreenOnPluggedStateRisingEdgeFlag = false;
			int minutesToFull = 0;

			if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_AC ) {
				minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
						.getLevel( ) ) / 1.5 );
			} else if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_USB ) {
				minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
						.getLevel( ) ) / 0.5 );
			}

			String text;
			if ( minutesToFull > 0 ) {
				text = "Ladezeit " + minutesToFull + " Minuten";
			} else {
				text = "Vollständig aufgeladen";
			}
			this.speakTextAfterScreenOn( text, false );
			this.showBattaryInformationOverlay( text );
		} else if ( this.nextScreenOnPluggedStateFallingEdgeFlag ) {
			this.nextScreenOnPluggedStateFallingEdgeFlag = false;

			if ( this.actualBatteryStatus.getLevel( ) != 100 ) {
				String text = "Nicht vollständig aufgeladen.";
				String text2 = "Ladestand "
						+ this.actualBatteryStatus.getLevel( ) + "%";
				this.speakTextAfterScreenOn( text + text2, false );
				this.showBattaryInformationOverlay( text2 );
			} else {
				String text = "Vollständig aufgeladen";
				this.speakTextAfterScreenOn( text, false );
				this.showBattaryInformationOverlay( text );
			}
		} else {
			// !this.nextScreenOnPluggedStateToOnFlag
			String text = "Ladestand " + this.actualBatteryStatus.getLevel( )
					+ "%";
			this.speakTextAfterScreenOn( text, true );
			this.showBattaryInformationOverlay( text );
		}
	}

	/*
	 * TextToSpeech Part
	 */
	@Override
	public void onInit( int status ) {
		if ( status == TextToSpeech.SUCCESS ) {
			int result = this.textToSpeech.setLanguage( Locale.GERMAN );
			if ( result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED ) {
				Toast.makeText( context, "TextToSpeech nicht verfügbar",
						Toast.LENGTH_LONG );
			}
		}
	}

	private void speakText( String textToSpeak ) {
		Log.d( TAG, "speakText=" + textToSpeak );
		if ( textToSpeak != null ) {
			this.textToSpeech.speak( textToSpeak, TextToSpeech.QUEUE_ADD, null );
		}
	}

	public void speakTextAfterScreenOn( String textToSpeak,
			boolean checkSilentTimeWindow ) {
		if ( !checkSilentTimeWindow ) {
			this.speakText( textToSpeak );
			return;
		}

		if ( ( System.currentTimeMillis( ) / 1000 )
				- this.findLastScreenOffTimestamp( ) > SILENT_SECONDS_AFTER_SCREEN_OFF ) {
			this.speakText( textToSpeak );
		}
	}

	/*
	 * Window Overlay Part
	 */
	private void showBattaryInformationOverlay( String displayText ) {
		/*
		 * FIXME We need a Queue system. Look at the textToSpeach implementation
		 */
		
		if ( this.currentlyOverlayActiveFlag ) {
			Log.d( TAG, "showBattaryInformationOverlay: forget: " + displayText );
			return;
		}
		
		Log.d( TAG, "showBattaryInformationOverlay: " + displayText );
		this.currentlyOverlayActiveFlag = true; // We are active, reset in Timer
		
		this.mView = new BatteryInformationView( this.context );
		this.mView.setDisplayText( displayText );

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
		this.mCountDownDisplayTimer.start( );
	}

	CountDownTimer mCountDownDisplayTimer = new CountDownTimer( 5000, 5000 ) {
		public void onTick( long millisUntilFinished ) {

		}

		public void onFinish( ) {
			EventsTracer.this.CountDownTimerDisplayTimerFinished( );
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

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
import at.linuxhacker.notifications.NotificationScreenOverlayView;

public class EventsTracer  {

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

	NotificationScreenOverlayView mView = null;
	private EventFilter eventFilter = null;

	public EventsTracer(Context context) {
		this.context = context;
		
		this.eventFilter = new EventFilter( context );
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
		this.processBatteryStatusUpdate( );
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

			this.eventFilter.processScreenOnEvent( this.actualBatteryStatus );
		}
	}

	public void addScreenOffEvent( ) {
		this.screenStatus.add( new ScreenStatusEvent( System
				.currentTimeMillis( ) / 1000, false ) );
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

	public void processBatteryStatusUpdate( ) {
		int minutesToFull = 0;

		if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_AC ) {
			minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
					.getLevel( ) ) / 1.0 );
		} else if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_USB ) {
			minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
					.getLevel( ) ) / 0.5 );
		}

		// Check for plugged state change on the rising edge
		// Set only Flag - notifications on ScreenOnEvent
		if ( ( this.previousBatteryStatus == null || this.previousBatteryStatus
				.getPlugged( ) == 0 )
				&& this.actualBatteryStatus.getPlugged( ) > 0 ) {

			this.actualBatteryStatus.setMinutesToFull( minutesToFull );
			this.eventFilter.processPowerPluggedInEvent( this.actualBatteryStatus );
		}

		// Check for unplugged state change on the falling edge
		if ( ( this.previousBatteryStatus == null || this.previousBatteryStatus
				.getPlugged( ) > 0 )
				&& this.actualBatteryStatus.getPlugged( ) == 0 ) {
			
			this.eventFilter.processPowerPluggedOutEvent( this.actualBatteryStatus );
		}

		// We are in plugged state
		if ( this.actualBatteryStatus.getPlugged( ) > 0 ) {
			this.pluggedState = true;
		} else {
			this.pluggedState = false;
		}
	}
	
}



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

import org.droidparts.battery_widget.BatteryWidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.util.Log;
import at.linuxhacker.notifications.NotificationScreenOverlayView;

public class EventCollector {

	private BatteryStatusEvent actualBatteryStatus = null;
	private BatteryStatusEvent previousBatteryStatus = null;
	boolean isScreenOnFlag;
	public boolean pluggedState = false; // FIXME: notwendig?
	// public String statusText = "leer"; FIXME: weg damit
	// public boolean batteryFull = false; FIXME: weg damit
	private Context context = null;
	private ArrayList<BatteryStatusEvent> batteryStatusEventList;
	private ArrayList<ScreenStatusEvent> screenStatusList = new ArrayList<ScreenStatusEvent>( );

	NotificationScreenOverlayView mView = null;
	private EventFilter eventFilter = null;

	public EventCollector(Context context) {
		Log.d( BatteryWidget.TAG, "EventsTracer::Constructor" );
		this.eventFilter = new EventFilter( context );
		this.batteryStatusEventList = new ArrayList<BatteryStatusEvent>(  );
		this.context = context;
		this.loadDbData( );
	}

	public void addBatteryChangedEvent( BatteryStatusEvent batteryStatus ) {
		Log.d( TAG,
				"battery state: level=" + batteryStatus.getLevel( )
						+ ", plugged=" + batteryStatus.getPlugged( )
						+ ", status=" + batteryStatus.getStatus( )
						+ ", mScreenOn=" + batteryStatus.isScreenOn( )
						+ ", timestamp=" + batteryStatus.getTimestamp( ) );

		this.batteryStatusEventList.add( batteryStatus );
		if ( ! this.isScreenOnFlag ) {
			this.flushDataToDb( );
		}
		this.previousBatteryStatus = this.actualBatteryStatus;
		this.actualBatteryStatus = batteryStatus;
		if ( this.actualBatteryStatus == null
				|| this.previousBatteryStatus == null ) {
			return;
		}

		this.processBatteryStatusUpdate( );
	}

	public void setScreenStatusToOn( boolean screenOnFlag ) {
		this.isScreenOnFlag = screenOnFlag;
	}

	public void addScreenOnEvent( ) {
		this.screenStatusList.add( new ScreenStatusEvent( true ) );
		this.isScreenOnFlag = true;

		if ( this.actualBatteryStatus != null ) {
			// yes, actualBatteryStatus could be null, after startup the widget
			// and the service. We have to check, if we have the necessary data

			if ( this.pluggedState ) {
				this.eventFilter.processScreenOnEventPluggedInState( this
						.calculateMinutesToFull( ) );
			} else {
				this.eventFilter
						.processScreenOnEvent( this.actualBatteryStatus );
			}
		}
	}

	public void addScreenOffEvent( ) {
		this.isScreenOnFlag = false;
		this.screenStatusList.add( new ScreenStatusEvent( false ) );
		this.flushDataToDb( );
	}

	public void processBatteryStatusUpdate( ) {
		// We are in plugged state
		if ( this.actualBatteryStatus.getPlugged( ) > 0 ) {
			this.pluggedState = true;
		} else {
			this.pluggedState = false;
		}

		// Check for plugged state change on the rising edge
		// Set only Flag - notifications on ScreenOnEvent
		if ( this.previousBatteryStatus.getPlugged( ) == 0
				&& this.actualBatteryStatus.getPlugged( ) > 0 ) {
			this.actualBatteryStatus.setMinutesToFull( this
					.calculateMinutesToFull( ) );
			this.eventFilter.processPowerPluggedInEvent( this.actualBatteryStatus );
		}

		// Check for unplugged state change on the falling edge
		if ( this.previousBatteryStatus.getPlugged( ) > 0
				&& this.actualBatteryStatus.getPlugged( ) == 0 ) {
			this.eventFilter.processPowerPluggedOutEvent( this.actualBatteryStatus );
		}

		// Check battery full state
		if ( this.actualBatteryStatus.getLevel( ) == 100
				&& this.previousBatteryStatus.getLevel( ) < 100 ) {
			this.eventFilter.processBatteryFullEvent( this.actualBatteryStatus );
		}
	}

	private int calculateMinutesToFull( ) {
		int minutesToFull = -1;

		if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_AC ) {
			minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
					.getLevel( ) ) / 0.8 );
		} else if ( this.actualBatteryStatus.getPlugged( ) == BatteryManager.BATTERY_PLUGGED_USB ) {
			minutesToFull = ( int ) ( ( float ) ( 100 - this.actualBatteryStatus
					.getLevel( ) ) / 0.27 );
		}

		return minutesToFull;
	}
	
	public void flushDataToDb( ) {
		int i, size;
		
		DbHelper dbHelper = new DbHelper( this.context );
		SQLiteDatabase db = dbHelper.getWritableDatabase( );
		ContentValues contentValues = new ContentValues( );
		
		size = this.batteryStatusEventList.size( );
		
		if ( size != 0 ) {
			for ( i = 0; i < size; i++ ) {
				contentValues.clear( );
				contentValues.put( DbHelper.C_TIMESTAMP,
						this.batteryStatusEventList.get( i ).getTimestamp( ) );
				contentValues.put( DbHelper.C_LEVEL,
						this.batteryStatusEventList.get( i ).getLevel( ) );
				contentValues.put( DbHelper.C_PLUGGED,
						this.batteryStatusEventList.get( i ).getPlugged( ) );
				contentValues.put( DbHelper.C_SCREEN_ON,
						this.batteryStatusEventList.get( i ).isScreenOn( ) );
				contentValues
						.put( DbHelper.C_MINUTES_TO_FULL,
								this.batteryStatusEventList.get( i )
										.getMinutesToFull( ) );

				db.insert( DbHelper.T_BATTERY_EVENTS, null, contentValues );
			}
		}
		
		size = this.screenStatusList.size( );
		
		if ( size != 0 ) {
			for ( i = 0; i < size; i++ ) {
				contentValues.clear( );
				contentValues.put( DbHelper.C_TIMESTAMP,
						this.screenStatusList.get( i ).getTimestamp( ) );
				contentValues.put( DbHelper.C_SCREEN_ON,
						this.screenStatusList.get( i ).isScreenOnFlag( ) );
				
				db.insert( DbHelper.T_SCREEN_EVENTS, null, contentValues );
			}
		}
		
		db.close( );
		
		this.batteryStatusEventList.clear( );
		this.screenStatusList.clear( );
	}

	private void loadDbData( ) {
		String columns[] = { DbHelper.C_TIMESTAMP, DbHelper.C_LEVEL,
				DbHelper.C_STATUS , DbHelper.C_PLUGGED, 
				DbHelper.C_SCREEN_ON, DbHelper.C_MINUTES_TO_FULL }; 
		DbHelper dbHelper = new DbHelper( this.context );
		SQLiteDatabase db = dbHelper.getReadableDatabase( );
		Cursor data = db.query( DbHelper.T_BATTERY_EVENTS, columns, null, null, null, null, DbHelper.C_TIMESTAMP + " desc" );
		if ( data.moveToFirst( ) ) {
			this.actualBatteryStatus = this.constructBatteryStatusEventFromCursorPosition ( data );
			Log.d( BatteryWidget.TAG, "DB Read timestamp1=" + this.actualBatteryStatus.getTimestamp( ) );
			
		}

		if ( data.moveToNext( ) ) {
			this.previousBatteryStatus = this.constructBatteryStatusEventFromCursorPosition ( data );
			Log.d( BatteryWidget.TAG, "DB Read timestamp2=" + this.previousBatteryStatus.getTimestamp( ) );
		}
		
		data.close( );
		db.close( );
	}
	
	private BatteryStatusEvent constructBatteryStatusEventFromCursorPosition ( Cursor cursor ) {
		BatteryStatusEvent event = new BatteryStatusEvent( 
				cursor.getLong( 0 ),
				cursor.getInt( 1 ),
				cursor.getInt( 2 ),
				cursor.getInt( 3 ),
				cursor.getInt( 4 ) == 1 ? true : false );
		event.setMinutesToFull( cursor.getInt( 5 ) );
		
		return event;
	}
}

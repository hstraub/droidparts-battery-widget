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
import android.util.Log;

public class ChargeRate {
	public static final int TYPE_CHARGE = 1;
	public static final int TYPE_DISCHARGE = 2;
	
	public long firstTimestamp; // Unix Timestamp in Seconds
	public long lastTimestamp; // Unix Timestamp in Seconds
	public int firstLevel;
	public int lastLevel;
	public boolean valid = false;
	public int type;
	
	public ChargeRate( int type, long startTimestamp, int startLevel ) {
		this.type = type;
		this.firstTimestamp = startTimestamp;
		this.firstLevel = startLevel;
	}
	
	public void updateChargeRate( long timestamp, int level ) {
		this.lastTimestamp = timestamp;
		this.lastLevel = level;
		this.valid = true;
	}
	
	public float getChargeRatePerMinute( ) {
		float chargeRate;
		Log.d( TAG, "getChargeRatePerMinute: " 
				+ "lastLevel=" + this.lastLevel 
				+ " firstLevel=" + this.firstLevel
				+ " lastTimestamp=" + this.lastTimestamp
				+ " firstTimestamp=" + this.firstTimestamp
				);
		
		try {
		chargeRate = ( ( float )  this.lastLevel - ( float ) this.firstLevel )
				/
				( ( float ) this.lastTimestamp - ( float ) this.firstTimestamp )
				* 60.0f;
		} catch ( Exception e ) {
			chargeRate = 0;
			Log.e( TAG, "getChargeRatePerMinute Exception: " + e );
		}
		
		return chargeRate;
	}
}

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

public class BatteryStatusEvent {

	private int level;
	private int status;
	private int plugged;
	private boolean screenOn;
	private long timestamp; // Unix Timestamp in Seconds
	private int minutesToFull = -1; 

	public int getLevel( ) {
		return level;
	}
	
	public int getStatus( ) {
		return status;
	}
	
	public int getPlugged( ) {
		return plugged;
	}
	
	public boolean isScreenOn( ) {
		return screenOn;
	}
	
	public long getTimestamp( ) {
		return timestamp;
	}

	public BatteryStatusEvent(int level, int status, int plugged,
			boolean screenOn) {
		this.timestamp = System.currentTimeMillis( ) / 1000; // Unix Timestamp
																// in seconds
		this.level = level;
		this.status = status;
		this.plugged = plugged;
		this.screenOn = screenOn;
	}

	public int getMinutesToFull( ) {
		return minutesToFull;
	}

	public void setMinutesToFull( int minutesToFull ) {
		this.minutesToFull = minutesToFull;
	}

}

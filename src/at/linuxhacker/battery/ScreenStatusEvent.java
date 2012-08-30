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

public class ScreenStatusEvent {
	private long timestamp; // Unix Timestamp in seconds
	private boolean screenOnFlag;
	
	public ScreenStatusEvent(long timestamp, boolean screenOnFlag) {
		super( );
		this.timestamp = timestamp;
		this.screenOnFlag = screenOnFlag;
	}

	public long getTimestamp( ) {
		return timestamp;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

	public boolean isScreenOnFlag( ) {
		return screenOnFlag;
	}

	public void setScreenOnFlag( boolean screenOnFlag ) {
		this.screenOnFlag = screenOnFlag;
	}
}

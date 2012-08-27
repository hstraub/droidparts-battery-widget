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

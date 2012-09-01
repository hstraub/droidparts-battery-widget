package at.linuxhacker.battery;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.droidparts.battery_widget.BatteryWidget;

import android.os.Environment;
import android.util.Log;

/*
 * Idea: 
 * Write own application LogClass oder use Log4j?
 * 
 * http://stackoverflow.com/questions/6757179/how-to-write-exception-in-log-file-using-android
 * 
 */


public class LocalStorage {
	static final private String DIRNAME = "QuickBattery";
	private String directoryname = null;
	
	public LocalStorage() {
		this.directoryname = Environment.getExternalStorageDirectory( ) 
				+ File.separator + LocalStorage.DIRNAME;
		File directory = new File( this.directoryname );
		directory.mkdirs( );
	}
	
	
	public void writeExceptionLog ( Exception e ) {
		Date now = new Date( );
		SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd_HHmmss" );
		String filename = "exception_" + new String( format.format( now ) )
				+ ".txt";
		String fullPathname = this.directoryname + File.separator + filename;
		File file = new File( fullPathname );
		try {
		PrintWriter writer = new PrintWriter( new FileWriter( file ) );
		writer.println( "Exception in Thread: " + Thread.currentThread( ).getName( )
				+ " Date: " + format.format( now ) );
		writer.print( Log.getStackTraceString( e ) );
		writer.close( );
		} catch ( Exception x ) {
			Log.e( BatteryWidget.TAG, "Cannot write Exception File: " + x );
		}
	}

}

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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.os.Environment;
import android.util.Log;
import at.linuxhacker.battery_widget.BatteryWidget;

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

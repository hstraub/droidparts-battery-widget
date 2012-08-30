package org.droidparts.battery_widget;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BatteryStatisticActivity extends Activity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		
		Log.d( BatteryWidget.TAG, "BatteryStatisticActivity::onCreate" );
		
		
		setContentView( R.layout.activity_battery_statistic );
	}
	
	

}

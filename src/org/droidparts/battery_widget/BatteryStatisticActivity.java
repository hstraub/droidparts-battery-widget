package org.droidparts.battery_widget;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class BatteryStatisticActivity extends Activity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_battery_statistic );
		
		Log.d( BatteryWidget.TAG, "BatteryStatisticActivity::onCreate" );
		
		// TextView textView = ( TextView ) findViewById( R.id.batteryStatistic_text1 );
		// textView.setText( Html.fromHtml( "<html><h1>Battery Statistic</h1><p>Das ist ein Test</p></html>" ) );
		String anzeige = "<html><h1>Battery Statistic</h1><p>Das ist ein Test</p></html>";
		WebView webView = ( WebView ) findViewById( R.id.webView1 );
		if ( webView != null ) {
			webView.loadData( anzeige, "text/html", "utf-8" );
		} else {
			Log.d( BatteryWidget.TAG, "Dreck ist null!!" );
		}
		
	}

}

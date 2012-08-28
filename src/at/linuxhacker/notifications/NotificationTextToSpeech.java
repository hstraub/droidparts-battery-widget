package at.linuxhacker.notifications;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

public class NotificationTextToSpeech implements TextToSpeech.OnInitListener {
	private TextToSpeech textToSpeech = null;
	private Context context = null;
	
	public NotificationTextToSpeech(Context context) {
		this.context = context;

		if ( this.textToSpeech == null ) {
			this.textToSpeech = new TextToSpeech( this.context, this );
		}
	}
	
	public void speakText( String textToSpeak ) {
		this.textToSpeech.speak( textToSpeak, TextToSpeech.QUEUE_ADD, null );
	}
	
	@Override
	public void onInit( int status ) {
		if ( status == TextToSpeech.SUCCESS ) {
			int result = this.textToSpeech.setLanguage( Locale.GERMAN );
			if ( result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED ) {
				Toast.makeText( context, "TextToSpeech nicht verfügbar",
						Toast.LENGTH_LONG );
			}
		}
	}
	
}

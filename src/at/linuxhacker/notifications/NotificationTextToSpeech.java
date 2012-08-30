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

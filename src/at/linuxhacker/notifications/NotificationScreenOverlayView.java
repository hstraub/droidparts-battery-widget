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


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import at.linuxhacker.battery_widget.BatteryWidget;


public class NotificationScreenOverlayView extends View {
    private Paint mLoadPaint;
    private int mNeededWidth = 300;
    private int mNeededHeight = 300;
    private Rect textBounds = null;
    final private int DISTANCE_Y = 30;
    private String displayText;

    private Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		if (msg.what == 1) {
    			updateDisplay();
    			Message m = obtainMessage(1);
    			sendMessageDelayed(m, 2000);
    		}
    	}
    };

    public NotificationScreenOverlayView(Context context) {
        super(context);
        //Toast.makeText(getContext(),"HUDView", Toast.LENGTH_LONG).show();
        //setPadding( 4, 4, 4, 4 );
        
        Log.d( BatteryWidget.TAG, "BatteryInformationView::Constructor" );
        mLoadPaint = new Paint();
        mLoadPaint.setAntiAlias(true);
        mLoadPaint.setTextSize(40);
        mLoadPaint.setARGB( 255, 255, 255, 255 );
        
    }
    
    public void setDisplayText( String displayText ) {
    	this.textBounds = new Rect( );
    	this.mLoadPaint.getTextBounds( displayText, 0, displayText.length( ), textBounds );
    	this.mNeededWidth = this.textBounds.right - this.textBounds.left;
    	this.mNeededHeight = this.textBounds.bottom - this.textBounds.top;
    	this.mNeededHeight = this.mNeededHeight * 2; //FIXME: ascend and descend
    	this.displayText = displayText;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.sendEmptyMessage(1);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(1);
    }
    
    protected void updateDisplay() {
		// TODO Auto-generated method stub
    	invalidate( );
    }

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(mNeededWidth, widthMeasureSpec),
                resolveSize(mNeededHeight, heightMeasureSpec));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int x = ( canvas.getWidth( ) - ( this.textBounds.right - this.textBounds.left ) ) / 2;
        int y = ( canvas.getHeight( ) -( this.textBounds.bottom - this.textBounds.top ) ) / 2;
        
        canvas.drawText( this.displayText, x, y + DISTANCE_Y, mLoadPaint );
    }

}

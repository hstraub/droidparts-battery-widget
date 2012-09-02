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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	public static final String TAG = "DbHelper";
	public static final String DB_NAME = "battery_events.db";
	public static final int DB_VERSION = 1;
	public static final String T_BATTERY_EVENTS = "battery_events";
	public static final String T_SCREEN_EVENTS = "screen_events";
	public static final String T_REPORTS = "reports";
	public static final String T_LOAD_RATES = "load_rates";
	public static final String T_UNLOAD_RATES = "unload_rates";
	
	public static final String C_TIMESTAMP = "timestamp";
	public static final String C_STATUS = "status";
	public static final String C_PLUGGED = "plugged";
	public static final String C_SCREEN_ON = "screen_on";
	public static final String C_LEVEL = "level";
	public static final String C_MINUTES_TO_FULL = "minutes_to_full";
	public static final String C_REPORT_FILE_NAME = "report_filename";
	
	public static final String C_LOAD_TYPE = "load_type";
	public static final String C_MIN_RATE = "min_rate";
	public static final String C_MAX_RATE = "max_rate";
	public static final String C_AVG_RATE = "avg_rate";
	
	public static final int LOAD_TYPE_DISCHARGE_SCREEN_OFF = 0;
	public static final int LOAD_TYPE_DISCHARGE_SCREEN_ON = 1;
	public static final int LOAD_TYPE_CHARGE_POWER = 2;
	public static final int LOAD_TYPE_CHARGE_USB = 3;
	

	public DbHelper(Context context ) {
		super( context, DB_NAME, null, DB_VERSION );
	}

	@Override
	public void onCreate( SQLiteDatabase db ) {
		String sql = "create table " + T_BATTERY_EVENTS + " ( "
				+ C_TIMESTAMP + " integer primary key, " 
				+ C_LEVEL + " integer, "
				+ C_STATUS + " integer, "
				+ C_PLUGGED + " integer, "
				+ C_SCREEN_ON + " integer, "
				+ C_MINUTES_TO_FULL + " integer )";
		db.execSQL( sql );
		
		sql = "create table " + T_SCREEN_EVENTS + " ( "
				+ C_TIMESTAMP + " integer primary key, "
				+ C_SCREEN_ON + " integer )";
		db.execSQL( sql );
		
		sql = "create table " + T_REPORTS + " ( "
				+ C_TIMESTAMP + " integer primary key, "
				+ C_REPORT_FILE_NAME + " text )";
		db.execSQL( sql );
		
		sql = "create table " + T_LOAD_RATES + " ( "
				+ C_LOAD_TYPE + " integer primary key, "
				+ C_MIN_RATE + " integer, "
				+ C_MAX_RATE + " integer, "
				+ C_AVG_RATE + " integer )";
		db.execSQL( sql );
		
		sql = "insert into " + T_LOAD_RATES + " values ( " 
				+ LOAD_TYPE_DISCHARGE_SCREEN_OFF + " , "
				+ " -0.10, -0.20, -0.13 )";
		db.execSQL( sql );
		
		sql = "insert into " + T_LOAD_RATES + " values ( " 
				+ LOAD_TYPE_DISCHARGE_SCREEN_ON + " , "
				+ " -0.30, -0.50, -0.40 )";
		db.execSQL( sql );
		
		sql = "insert into " + T_LOAD_RATES + " values ( " 
				+ LOAD_TYPE_CHARGE_POWER + " , "
				+ " 0.80, 1.00, 0.85 )";
		db.execSQL( sql );
		
		sql = "insert into " + T_LOAD_RATES + " values ( " 
				+ LOAD_TYPE_CHARGE_USB + " , "
				+ " 0.20, 0.45, 0.30 )";
		db.execSQL( sql );
		
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
		// TODO Auto-generated method stub

	}

}

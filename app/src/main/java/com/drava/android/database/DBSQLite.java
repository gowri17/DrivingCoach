package com.drava.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;

import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;
import com.drava.android.model.EndTrip;
import com.drava.android.model.StartTrip;
import com.drava.android.model.TrackTripPath;
import com.drava.android.model.TripViolationInfo;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DravaLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DBSQLite extends SQLiteOpenHelper {
    /**
     * This class create DB and Tables
     * DB version must be change when we alert DB or create any table on onUpgrade method
     */
    static final String TAG = "DBSQLite";
    private static final int DATABASE_VERSION = 1;
    public static String DATABASE_NAME = "drava.sqlite";
    private SQLiteDatabase mDatabase;
    private Context mContext;

    public DBSQLite(Context context, String database_name) {
        super(context, database_name, null, DATABASE_VERSION);
        this.mContext = context;
    }

    /**
     * Method to create or Open the Database
     */
    public static DBSQLite getInstance(Context context) {
        DBSQLite instance = null;
        try {
            instance = new DBSQLite(context, DATABASE_NAME);
        } catch (IllegalStateException e) {
            Log.d(TAG, "Database already created.");
        } catch (SQLException sql) {
            Log.d(TAG, "Unable to Open database");
        } catch (Exception e) {
            Log.d(TAG, "Unable to load the database");
        }
        return instance;
    }

    public SQLiteDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SqlCard.TRIP_TABLE);
        db.execSQL(SqlCard.TRIP_VOILATION_TABLE);
        db.execSQL(SqlCard.TRIP_TRACKING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean createTable(String tableName, String fields) {
        Cursor c = mDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);
        if (c.getCount() == 0) {
            mDatabase.execSQL("CREATE TABLE " + tableName + " (" + fields + ");");
            c.close();
            return (true);
        } else {
            c.close();
            if (AppConstants.DEBUG) {
                Log.e(TAG, "No Table Created");
            }
            return (false);
        }
    }

    public boolean executeQuery(String sqlQuery) {
        try {
            mDatabase.execSQL(sqlQuery);
            return (true);
        } catch (SQLiteException e) {
            if (AppConstants.DEBUG) {
                Log.e("SQLite Error", "Query execution Failed.");
            }
            return (false);
        }
    }

    public Cursor select(String query) {
        try {
            Cursor c = mDatabase.rawQuery(query, null);
            return c;
        } catch (SQLiteException e) {
            if (AppConstants.DEBUG) {
                Log.e(TAG, "Query Fetch Failed" + e.toString());
            }
            return null;
        }
    }

    /**
     * @param whereCondition (without <b>'where'</b> keyword. If you need all count set
     *                       <b>whereCondition</b> to null.
     * @return no. of rows.
     */
    public long getRowCount(String tableName, String whereCondition) {
        String sqlQuery;
        if (whereCondition != null) {
            sqlQuery = "SELECT COUNT(*) FROM " + tableName + " where " + whereCondition;
        } else {
            sqlQuery = "SELECT COUNT(*) FROM " + tableName;
        }

        if (AppConstants.DEBUG) {
            Log.d(TAG, "QUERY:" + sqlQuery);
        }

        SQLiteStatement sqliteStatement = mDatabase.compileStatement(sqlQuery);
        long rowCount = sqliteStatement.simpleQueryForLong();
        sqliteStatement.close();
        return rowCount;
    }

    public boolean isRowAvailable(String tableName, String whereCondition) {
        return getRowCount(tableName, whereCondition) > 0;
    }

    public boolean getRowAvailability(String query) {
        boolean availability = false;

        Cursor c = mDatabase.rawQuery(query, null);
        int count = c.getCount();
        if (count > 0) {
            availability = true;
        } else {
            availability = false;
        }
        c.close();
        return availability;
    }

    public boolean startNewTrip(long tripId, String startTime, String startLatitude, String startLongitude, String tripType, String tripDate, String passenger) {
        ContentValues values = new ContentValues();
        values.put(Columns.Trips.TRIPID, tripId);
        values.put(Columns.Trips.STARTTIME, startTime);
        values.put(Columns.Trips.STARTLATITUDE, startLatitude);
        values.put(Columns.Trips.STARTLONGITUDE, startLongitude);
        values.put(Columns.Trips.TRIPTYPE, tripType);
        values.put(Columns.Trips.TRIPDATE, tripDate);
        values.put(Columns.Trips.ISPASSENGER, passenger);

        long id = mDatabase.insert(Columns.Trips.TABLE, null, values);
        if (id > 0) {
            Log.e("TAG", "Start trip ID " + tripId);
            return true;
        }
        return false;
    }

    public boolean updateEndTrip(long tripId, String endTime, String endLatitude, String endLongitude, String distance, String minSpeed, String maxSpeed) {
        String whereCls = Columns.Trips.TRIPID + " = " + tripId;
        ContentValues values = new ContentValues();
        values.put(Columns.Trips.ENDTIME, endTime);
        values.put(Columns.Trips.ENDLATITUDE, endLatitude);
        values.put(Columns.Trips.ENDLONGITUDE, endLongitude);
        values.put(Columns.Trips.DISTANCE, distance);
        values.put(Columns.Trips.MINSPEED, minSpeed);
        values.put(Columns.Trips.MAXSPEED, maxSpeed);
        int count = getWritableDatabase().update(Columns.Trips.TABLE, values, whereCls, null);
        if (count > 0) {
            Log.e("TAG", "updated trip ID " + tripId + " end trip values");
            AppLog.print(mContext, "updated trip ID " + tripId + " end trip values");
            return true;
        } else {
            Log.e("TAG", "end trip updation fails");
            AppLog.print(mContext, "End Trip fails for Id  " + tripId);
        }
        return false;
    }

    public boolean updateEndTripFromPreference(long tripId, String endTime, String endLatitude, String endLongitude, String distance, String minSpeed, String maxSpeed) {
        String whereCls = Columns.Trips.SERVERTRIPID + " = " + tripId;
        ContentValues values = new ContentValues();
        values.put(Columns.Trips.ENDTIME, endTime);
        values.put(Columns.Trips.ENDLATITUDE, endLatitude);
        values.put(Columns.Trips.ENDLONGITUDE, endLongitude);
        values.put(Columns.Trips.DISTANCE, distance);
        values.put(Columns.Trips.MINSPEED, minSpeed);
        values.put(Columns.Trips.MAXSPEED, maxSpeed);
        int count = getWritableDatabase().update(Columns.Trips.TABLE, values, whereCls, null);
        if (count > 0) {
            Log.e("TAG", "updated trip ID " + tripId + " end trip values");
            return true;
        } else {
            Log.e("TAG", "end trip updation fails");
        }
        return false;
    }

    public boolean updateLatLngTripPath(long tripId, String latitude, String longitude, boolean isVoilated) {
        ContentValues values = new ContentValues();
        values.put(Columns.TripPath.TRIPID, tripId);
        values.put(Columns.TripPath.LATITUDE, latitude);
        values.put(Columns.TripPath.LONGITUDE, longitude);
        values.put(Columns.TripPath.ISVOILATED, isVoilated);

        long id = mDatabase.insert(Columns.TripPath.TABLE, null, values);
        if (id > 0) {
            return true;
        }
        return false;
    }

    public boolean updateServerStartTripId(int serverTripId, long offlineTripId) {
        String whereCls = Columns.Trips.TRIPID + " = " + offlineTripId;
        ContentValues values = new ContentValues();
        values.put(Columns.Trips.SERVERTRIPID, serverTripId);

        long id = mDatabase.update(Columns.Trips.TABLE, values, whereCls, null);
        if (id > 0) {
            return true;
        }
        return false;
    }

    public int getServerIdforEndTrip(long offlineTripId) {
        String selelctQuery = "select * from " + Columns.Trips.TABLE + " where " + Columns.Trips.TRIPID + " = " + offlineTripId;
        Cursor cursor = mDatabase.rawQuery(selelctQuery, null);

        if (!cursor.moveToFirst()) {
            return 0;
        } else {
            cursor.moveToFirst();
            return cursor.getInt(cursor.getColumnIndex(Columns.Trips.SERVERTRIPID));
        }
    }

    public StartTrip getStartTripInfo(long tripId) {
        String selelctQuery = "select * from " + Columns.Trips.TABLE + " where " + Columns.Trips.TRIPID + " = " + tripId;
        Cursor cursor = mDatabase.rawQuery(selelctQuery, null);

        if (!cursor.moveToFirst()) {
            return null;
        } else {
            cursor.moveToFirst();
            StartTrip startTrip = new StartTrip();
            startTrip.startTime = cursor.getString(cursor.getColumnIndex(Columns.Trips.STARTTIME));
            startTrip.startLatitude = cursor.getString(cursor.getColumnIndex(Columns.Trips.STARTLATITUDE));
            startTrip.startLongitude = cursor.getString(cursor.getColumnIndex(Columns.Trips.STARTLONGITUDE));
            startTrip.tripDate = cursor.getString(cursor.getColumnIndex(Columns.Trips.TRIPDATE));
            startTrip.tripType = cursor.getString(cursor.getColumnIndex(Columns.Trips.TRIPTYPE));
            startTrip.serverId = cursor.getInt(cursor.getColumnIndex(Columns.Trips.SERVERTRIPID));
            startTrip.isPassenger = cursor.getString(cursor.getColumnIndex(Columns.Trips.ISPASSENGER));
            return startTrip;
        }
    }

    public EndTrip getEndTripInfo(long tripId) {
        EndTrip endTrip = new EndTrip();
        String selectQuery = "select * from " + Columns.Trips.TABLE + " where " + Columns.Trips.TRIPID + " = " + tripId;
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (!cursor.moveToFirst())
            return null;
        else {
            cursor.moveToFirst();
            endTrip.EndTime = cursor.getString(cursor.getColumnIndex(Columns.Trips.ENDTIME));
            endTrip.EndLatitude = cursor.getString(cursor.getColumnIndex(Columns.Trips.ENDLATITUDE));
            endTrip.EndLongitude = cursor.getString(cursor.getColumnIndex(Columns.Trips.ENDLONGITUDE));
            endTrip.Distance = cursor.getString(cursor.getColumnIndex(Columns.Trips.DISTANCE));
            endTrip.MinSpeed = cursor.getString(cursor.getColumnIndex(Columns.Trips.MINSPEED));
            endTrip.MaxSpeed = cursor.getString(cursor.getColumnIndex(Columns.Trips.MAXSPEED));
        }
//            if(cursor != null){
//                cursor.close();
//            }
        return endTrip;
    }

    public List<TrackTripPath> getTrackTripPathInfo(long tripId) {
        List<TrackTripPath> tripPathList = new ArrayList<>();
        String selectQuery = "select * from " + Columns.TripPath.TABLE + " where " + Columns.TripPath.TRIPID + " = " + tripId;
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (!cursor.moveToFirst())
            return tripPathList;

        do {
            TrackTripPath pathObject = new TrackTripPath();
            pathObject.Latitude = cursor.getString(cursor.getColumnIndex(Columns.TripPath.LATITUDE));
            pathObject.Longitude = cursor.getString(cursor.getColumnIndex(Columns.TripPath.LONGITUDE));
            pathObject.TripId = cursor.getString(cursor.getColumnIndex(Columns.TripPath.TRIPID));
            pathObject.IsViolation = cursor.getString(cursor.getColumnIndex(Columns.TripPath.ISVOILATED));
            tripPathList.add(pathObject);
        } while (cursor.moveToNext());
        return tripPathList;
    }


    public long getFirstTripId() {
        Cursor cursor = null;
        String selectQuery = "select * from " + Columns.Trips.TABLE + " where " + Columns.Trips.SERVERTRIPID + "!=0";
        cursor = mDatabase.rawQuery(selectQuery, null);
//         selectQuery = "select "+ Columns.Trips.TRIPID +" from "+ Columns.Trips.TABLE +" order by "+Columns.Trips.TRIPID+" asc limit 1";

        if (!cursor.moveToFirst()) {

            selectQuery = "select " + Columns.Trips.TRIPID + " from " + Columns.Trips.TABLE + " order by " + Columns.Trips.TRIPID + " asc limit 1";
            cursor = mDatabase.rawQuery(selectQuery, null);
            if (!cursor.moveToFirst()) {
                return 0;
            } else {
                return cursor.getLong(cursor.getColumnIndex(Columns.Trips.TRIPID));
            }
        } else {
            return cursor.getLong(cursor.getColumnIndex(Columns.Trips.TRIPID));
        }
    }

    public /*boolean*/long insertViolationInfo(String tripId, String roadSpeed, String vehicleSpeed, String latitude, String longitude) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Columns.TripVoilation.TRIPID, tripId);
        contentValues.put(Columns.TripVoilation.ROADSPEED, roadSpeed);
        contentValues.put(Columns.TripVoilation.VEHICLESPEED, vehicleSpeed);
        contentValues.put(Columns.TripVoilation.LATITUDE, latitude);
        contentValues.put(Columns.TripVoilation.LONGITUDE, longitude);

        long id = mDatabase.insert(Columns.TripVoilation.TABLE, null, contentValues);
        return id;
//        if(id > 0){
//            return true;
//        }
//        return false;
    }

//    public TripViolationInfo getTripViolationInfo(){
//        List<TripViolationInfo> tripViolationInfoList = new ArrayList<>();
//        Cursor cursor = null;
//        TripViolationInfo tripViolationInfo = new TripViolationInfo();
//        String select_query = "select rowid, * from "+Columns.TripVoilation.TABLE/*+" limit 1"*/;
//        cursor = mDatabase.rawQuery(select_query, null);
//        if(!cursor.moveToFirst()){
//            if(cursor != null){
//                cursor.close();
//            }
//            return tripViolationInfo;
//        }else{
//            tripViolationInfo.rowId = cursor.getString(cursor.getColumnIndex("rowid"));
//            tripViolationInfo.tripId = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.TRIPID));
//            tripViolationInfo.roadSpeed = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.ROADSPEED));
//            tripViolationInfo.vehicleSpeed = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.VEHICLESPEED));
//            tripViolationInfo.latitude = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.LATITUDE));
//            tripViolationInfo.longitude = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.LONGITUDE));
//            AppLog.print(mContext,"Rowid==>"+tripViolationInfo.rowId);
//            if(cursor != null){
//                cursor.close();
//            }
//            return tripViolationInfo;
//        }
//    }

    public List<TripViolationInfo> getTripViolationInfo() {
        List<TripViolationInfo> tripViolationInfoList = new ArrayList<>();
        Cursor cursor = null;

        String select_query = "select rowid, * from " + Columns.TripVoilation.TABLE/*+" limit 1"*/;
        cursor = mDatabase.rawQuery(select_query, null);
        if (!cursor.moveToFirst()) {
//            if (cursor != null) {
//                cursor.close();
//            }
            return tripViolationInfoList;
        } else {
            do {
                TripViolationInfo tripViolationInfo = new TripViolationInfo();
                tripViolationInfo.rowId = cursor.getString(cursor.getColumnIndex("rowid"));
                tripViolationInfo.tripId = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.TRIPID));
                tripViolationInfo.roadSpeed = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.ROADSPEED));
                tripViolationInfo.vehicleSpeed = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.VEHICLESPEED));
                tripViolationInfo.latitude = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.LATITUDE));
                tripViolationInfo.longitude = cursor.getString(cursor.getColumnIndex(Columns.TripVoilation.LONGITUDE));
                AppLog.print(mContext, "Rowid==>" + tripViolationInfo.rowId+"=>TripId==>"+tripViolationInfo.tripId+"=>roadSpeed=>"+tripViolationInfo.roadSpeed
                        +"=>vehicleSpeed=>"+tripViolationInfo.vehicleSpeed+"=>latitude=>"+tripViolationInfo.latitude+"=>longitude=>"+tripViolationInfo.longitude);
                tripViolationInfoList.add(tripViolationInfo);
            } while (cursor.moveToNext());
//            if (cursor != null) {
//                cursor.close();
//            }
            return tripViolationInfoList;
        }
    }

    public long getOfflineTripIdofUnEndedTripduringSwitchOff(){         //R.L v1.1
        long offlineTripId;
        Cursor cursor;
        String query = "select "+ Columns.Trips.TRIPID +" from "+ Columns.Trips.TABLE +" where "/*+ Columns.Trips.SERVERTRIPID+" >0 and "*/+ Columns.Trips.ENDTIME+" IS NULL"+
                " and "+Columns.Trips.ENDLATITUDE+" IS NULL and "+ Columns.Trips.ENDLONGITUDE +" IS NULL";
        cursor = mDatabase.rawQuery(query, null);
        if(!cursor.moveToFirst()){
            offlineTripId = 0;
        }else{
            offlineTripId = cursor.getLong(cursor.getColumnIndex(Columns.Trips.TRIPID));
        }
        AppLog.print(mContext, "unEndedTripIdduringSwitchOff============>"+offlineTripId);
        return offlineTripId;
    }

    public boolean deleteTripEntry(long tripId) {
        return mDatabase.delete(Columns.Trips.TABLE, Columns.Trips.TRIPID + " = " + tripId, null) > 0;
    }

    public boolean deleteTrackPath(long tripId) {
        return mDatabase.delete(Columns.TripPath.TABLE, Columns.TripPath.TRIPID + " = " + tripId, null) > 0;
    }

    public boolean deleteViolationInsertedRow(String rowid) {
        return mDatabase.delete(Columns.TripVoilation.TABLE, "rowid = " + rowid, null) > 0;
    }

    public void deleteAllTableValues() {
        try {
            mDatabase.execSQL("delete from " + Columns.Trips.TABLE);
            mDatabase.execSQL("delete from " + Columns.TripVoilation.TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void backupDatabase() throws IOException {
        String inFileName = "/data/data/com.drava.android/databases/" + DATABASE_NAME;
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory() + "/MYDB.sqlite";
        OutputStream output = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        //Close the streams
        output.flush();
        output.close();
        fis.close();
    }
}

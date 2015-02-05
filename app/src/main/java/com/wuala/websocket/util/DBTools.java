package com.wuala.websocket.util;

/**
 * Created by martin on 04.02.2015.
 */


        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.util.Log;

        import com.wuala.websocket.model.User;

/**
 * Created by csimon on 12/11/13.
 */
public class DBTools extends SQLiteOpenHelper {

    private final static int    DB_VERSION = 1;

    public DBTools(Context context) {
        super(context, "Calyser.db", null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "create table logins (userId Integer primary key autoincrement, "+
                " username text, password text)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        try{
            //recreateDb(sqLiteDatabase);
            if (oldVersion<10){
                String query = "create table logins (userId Integer primary key autoincrement, "+
                        " username text, password text)";
                sqLiteDatabase.execSQL(query);
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
    }

    public User insertUser (User queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", queryValues.getUsername());
        values.put("password", queryValues.getPassword());
        Log.d("DBTools"," username= "+values.getAsString("username")+ " password= "+values.getAsString("password"));
        queryValues.setUserid(database.insert("logins", null, values));
        database.close();
        return queryValues;
    }

    public int updateUserPassword (User queryValues){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", queryValues.getUsername());
        values.put("password", queryValues.getPassword());
        queryValues.setUserid(database.insert("logins", null, values));
        database.close();
        return database.update("logins", values, "userId = ?", new String[] {String.valueOf(queryValues.getUserid())});
    }

    public User getUser (String username){

        Log.d("DBTools","Inside GetUser");
//
        String query2 = "Select * from logins";
        Log.d("DBTools",query2);
        SQLiteDatabase database2 = this.getReadableDatabase();
        Cursor cursor2 = database2.rawQuery(query2, null);
        if (cursor2.moveToFirst()) {
            do {
                Log.d("DBTools", " 0= " + cursor2.getInt(0)+" 1= " + cursor2.getString(1) + " 2= " + cursor2.getString(2));
            } while (cursor2.moveToNext());
        }
        //
        String query1 = "Select count(*) from logins";
        Log.d("DBTools",query1);
        SQLiteDatabase database1 = this.getReadableDatabase();
        Cursor cursor1 = database1.rawQuery(query1, null);
        if (cursor1.moveToFirst()) {
            do {
                Log.d("DBTools", "Count " + cursor1.getInt(0));
            } while (cursor1.moveToNext());
        }
        //
        String query = "Select userId, password from logins where username ='"+username+"'";
        Log.d("DBTools",query);
        User myUser = new User() ;
        myUser.setUsername(username);
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()){
            do {
                Log.d("DBTools","cursor.getLong "+cursor.getLong(0));
                myUser.setUserid(cursor.getLong(0));
                myUser.setPassword(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        return myUser;
    }
}
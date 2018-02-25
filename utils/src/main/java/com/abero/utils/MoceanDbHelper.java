package com.abero.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zskj.utillibpro.utils.MyLogger;

/**
 * Created by abero on 2017/2/21 0021.
 * _id	      url	         data	            time
 * 主键	      请求地址	     json数据	        时间
 */

public class MoceanDbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "moceanhd.db";

    public static final String JSON_TABLE_NAME = "moceanhd_json";

    public static final String ID = "_id";
    public static final String URL = "url";
    public static final String DATA = "data";
    public static final String TIME = "time";

    public static final String HISTORY_TABLE_NAME = "buwaves_history";
    public static final String FAVORITE_TABLE_NAME = "buwaves_favorite";
    public static final String COUNT_TABLE_NAME = "buwaves_his_fav_count";
    public static final String ALL_MOVIES_TABLE_NAME = "all_movies";
    public static final String ALL_SERIES_TABLE_NAME = "all_series";
    private MyLogger mylog = MyLogger.getAberoLog();


    public MoceanDbHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.beginTransaction();
        try {

            //json table
            String sql = "CREATE TABLE IF NOT EXISTS "
                    + JSON_TABLE_NAME + " ("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + URL + " TEXT not null, "
                    + TIME + " TEXT, "
                    + DATA + " TEXT)";
            sqLiteDatabase.execSQL(sql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_url ON " + JSON_TABLE_NAME + " (" + URL + ")");

            //播放历史
            String historySql = "create table if not exists " + HISTORY_TABLE_NAME
                    + " (Id integer primary key, Duration text, Bookmarktime text, Episode integer, Bookmarkdate " +
                    "text, Terminalstateurl text,"
                    + " Name text, Categoryid text, Type text, Seriestype text, Hdtv text, Image text,"
                    + " Score text, Count text, Adult text, Tday text, DetailUrl text)";
            sqLiteDatabase.execSQL(historySql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_id ON " + HISTORY_TABLE_NAME + " ( Id )");


            //收藏
            String favoriteSql = "create table if not exists " + FAVORITE_TABLE_NAME
                    + " (Id integer primary key, FavoriteId integer, ProgramType text,Name text, Categoryid text, " +
                    "Image text,Type " +
                    "text, Adult text, DetailUrl text,"
                    + "Createtime text, Seriestype text,Sumseries text, Newseries text, Hdtv text,Score text, Count " +
                    "text, Tday text,"
                    + "Number integer, TimeshiftLength text, StorageLength text, Terminalstateurl text, CbidUrl text," +
                    " ScheduleListUrl text, CurrentSchedule text"
                    + ")";
            sqLiteDatabase.execSQL(favoriteSql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_fv_id ON " + FAVORITE_TABLE_NAME + " ( Id )");

            //统计
            String countSql = "create table if not exists " + COUNT_TABLE_NAME
                    + " (Id integer primary key,Name text,Total integer"
                    + ")";
            sqLiteDatabase.execSQL(countSql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_count_id ON " + COUNT_TABLE_NAME + " ( Id )");

            //all movies 分类下的表，用于本地筛选
            String allmoviesSql = "create table if not exists " + ALL_MOVIES_TABLE_NAME
                    + " (Id integer unique,Name text,Image text,Displaylevel text COLLATE NOCASE,Language text," +
                    "Country text COLLATE NOCASE,Type text COLLATE NOCASE,"
                    + "Releaseyear text,DetailUrl text)";
            sqLiteDatabase.execSQL(allmoviesSql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_all_movies_id ON " + ALL_MOVIES_TABLE_NAME + " (" +
                    " Id )");

            //all series 分类下的表，用于本地筛选
            String allseriesSql = "create table if not exists " + ALL_SERIES_TABLE_NAME
                    + " (Id integer unique,Name text,Image text,Displaylevel text COLLATE NOCASE,Language text " +
                    "COLLATE NOCASE,Country text COLLATE NOCASE,Type text COLLATE NOCASE,"
                    + "Releaseyear text,DetailUrl text)";
            sqLiteDatabase.execSQL(allseriesSql);
            sqLiteDatabase.execSQL("CREATE UNIQUE INDEX unique_index_all_series_id ON " + ALL_SERIES_TABLE_NAME + " (" +
                    " Id )");

            sqLiteDatabase.setTransactionSuccessful();
            mylog.i("sqlite create");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteDatabase.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        mylog.i("onUpgrade");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + JSON_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FAVORITE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + COUNT_TABLE_NAME);
        // sqLiteDatabase.delete(JSON_TABLE_NAME, null, null);
        // sqLiteDatabase.delete(HISTORY_TABLE_NAME, null, null);
        //sqLiteDatabase.delete(FAVORITE_TABLE_NAME, null, null);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ALL_MOVIES_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ALL_SERIES_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}

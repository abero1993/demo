package com.abero.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteStatement;

import com.zskj.utillibpro.utils.MyLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import dev.jr.moc.data.jsonbeans.GetCollectBean;
import dev.jr.moc.data.jsonbeans.RecentWatchBean;
import dev.jr.moc.data.jsonbeans.VodSeriesBean;
import dev.jr.moc.data.source.ButterflyDataSource;

/**
 * Created by abero on 2017/2/21 0021.
 */

public class DBManager {

    private static DBManager INSTANCE = null;
    private MoceanDbHelper dbHelper;
    public static final long EXPIRED_TIME = (1000 * 60) * (60) * (12); //播放地址缓存半天
    private static final int SQLITE_VERSION = 18;
    private MyLogger myLogger = MyLogger.getBaseLog();

    public DBManager(Context context, int version) {
        dbHelper = new MoceanDbHelper(context, version);
    }

    public static DBManager getInstance(Context context) {
        synchronized (DBManager.class) {
            if (null == INSTANCE)
                INSTANCE = new DBManager(context, SQLITE_VERSION);
            return INSTANCE;
        }
    }

    /**
     * 插入缓存，没有就插入，有就替换
     *
     * @param url  地址
     * @param data json数据
     */
    public synchronized void insertData(String url, String data) {
        try {
            String key = hashKeyForDisk(url);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MoceanDbHelper.URL, key);
            values.put(MoceanDbHelper.DATA, data);
            values.put(MoceanDbHelper.TIME, System.currentTimeMillis());
            db.replace(MoceanDbHelper.JSON_TABLE_NAME, null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据url获取缓存数据
     *
     * @param url 地址
     * @return 数据
     */
    public synchronized String getData(String url) {
        return getData(url, EXPIRED_TIME);
    }


    public synchronized String getData(String url, long expiredTime) {
        String result = "";
        try {
            String key = hashKeyForDisk(url);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + MoceanDbHelper.JSON_TABLE_NAME + " WHERE URL = ?", new
                    String[]{key});

            while (cursor.moveToNext()) {
                String timeStr = cursor.getString(cursor.getColumnIndex(MoceanDbHelper.TIME));
                long time = Long.parseLong(timeStr);
                MyLogger.getBaseLog().i("time=" + time);
                if ((System.currentTimeMillis() - time) < expiredTime)
                    result = cursor.getString(cursor.getColumnIndex(MoceanDbHelper.DATA));
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 删除tab表
     *
     * @return void
     */
    public void removeAll() {
        myLogger.i("removeAll");
        SQLiteDatabase sqlite = null;
        try {
            sqlite = dbHelper.getWritableDatabase();
            sqlite.beginTransaction();
            sqlite.delete(MoceanDbHelper.JSON_TABLE_NAME, null, null);
            sqlite.delete(MoceanDbHelper.HISTORY_TABLE_NAME, null, null);
            sqlite.delete(MoceanDbHelper.FAVORITE_TABLE_NAME, null, null);
            sqlite.delete(MoceanDbHelper.COUNT_TABLE_NAME, null, null);
            sqlite.delete(MoceanDbHelper.ALL_MOVIES_TABLE_NAME, null, null);
            sqlite.delete(MoceanDbHelper.ALL_SERIES_TABLE_NAME, null, null);
            sqlite.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlite != null) {
                try {
                    sqlite.endTransaction();
                    sqlite.close();
                } catch (SQLiteFullException e) {
                    //盒子居然有这个问题
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteTable(String table) {
        myLogger.i("delete table=" + table);
        SQLiteDatabase sqlite = null;
        try {
            sqlite = dbHelper.getWritableDatabase();
            sqlite.beginTransaction();
            sqlite.delete(table, null, null);
            sqlite.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlite != null) {
                sqlite.endTransaction();
                sqlite.close();
            }
        }
    }

    public void removeJsonTable() {
        myLogger.i("removeJsonTable");
        SQLiteDatabase sqlite = null;
        try {
            sqlite = dbHelper.getWritableDatabase();
            sqlite.beginTransaction();
            sqlite.delete(MoceanDbHelper.JSON_TABLE_NAME, null, null);
            sqlite.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlite != null) {
                sqlite.endTransaction();
                sqlite.close();
            }
        }
    }

    /**
     * history insert
     *
     * @param bean
     * @return boolean 成功返回true 否则失败
     */
    public synchronized boolean insertHistory(RecentWatchBean.ListBean bean) {
        SQLiteDatabase db = null;
        boolean res = false;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.replace(MoceanDbHelper.HISTORY_TABLE_NAME, null, getContentValues(bean));
            db.setTransactionSuccessful();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return res;
    }

    private ContentValues getContentValues(RecentWatchBean.ListBean bean) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Id", bean.getId());
        contentValues.put("Episode", bean.getEpisode());
        contentValues.put("Duration", bean.getDuration());
        contentValues.put("bookmarktime", bean.getBookmarktime());
        contentValues.put("Bookmarkdate", bean.getBookmarkdate());
        contentValues.put("Terminalstateurl", bean.getTerminalStateUrl());
        contentValues.put("Name", bean.getName());
        contentValues.put("Categoryid", bean.getCategoryid());
        contentValues.put("Type", bean.getType());
        contentValues.put("Seriestype", bean.getSeriestype());
        contentValues.put("Hdtv", bean.getHdtv());
        contentValues.put("Image", bean.getImage());
        contentValues.put("Score", bean.getScore());
        contentValues.put("Count", bean.getCount());
        contentValues.put("Adult", bean.getAdult());
        contentValues.put("Tday", bean.getTday());
        contentValues.put("DetailUrl", bean.getDetailUrl());
        return contentValues;
    }


    /**
     * history 删除
     *
     * @param id 节目id
     * @return boolean 成功返回true 否则失败
     */
    public synchronized boolean deleteHistory(String id) {
        boolean res = false;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.delete(MoceanDbHelper.HISTORY_TABLE_NAME, "Id = ?", new String[]{id});
            db.setTransactionSuccessful();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return res;
    }


    /**
     * history save
     *
     * @param recentWatchBean
     * @return void
     */
    public synchronized void saveHistroy(RecentWatchBean recentWatchBean) {

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            myLogger.i("save history");
            for (RecentWatchBean.ListBean bean : recentWatchBean.getList()) {
                // insert into Orders(Id, CustomName, OrderPrice, Country) values (7, "Jne", 700, "China");
                db.replace(MoceanDbHelper.HISTORY_TABLE_NAME, null, getContentValues(bean));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * history 查询
     *
     * @param start
     * @param total
     * @return RecentWatchBean
     */

    public synchronized RecentWatchBean queryHistory(int start, int total) {
        SQLiteDatabase db = null;
        RecentWatchBean recentWatchBean = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            myLogger.i("local " + start + " ," + total);
            String sql = "SELECT * FROM " + MoceanDbHelper.HISTORY_TABLE_NAME + " ORDER BY datetime(Bookmarkdate) " +
                    "DESC  LIMIT " + start + ", " + total;
            Cursor cursor = db.rawQuery(sql, null);
            int count = cursor.getCount();
            myLogger.i("select count=" + count);
            recentWatchBean = new RecentWatchBean();
            // recentWatchBean.setTotalNum(count);
            List<RecentWatchBean.ListBean> beanList = new ArrayList<>();
            //cursor.moveToFirst(); 哈哈，真的有问题
            while (cursor.moveToNext()) {
                RecentWatchBean.ListBean bean = parseWatchBean(cursor);
                beanList.add(bean);
            }
            recentWatchBean.setCurrStartIdx(start);
            recentWatchBean.setList(beanList);
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return recentWatchBean;
    }


    /**
     * history 查询
     *
     * @param pid 节目id
     * @return RecentWatchBean
     */
    public RecentWatchBean.ListBean getHistory(int pid) {
        RecentWatchBean.ListBean listBean = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            String sql = "SELECT * FROM " + MoceanDbHelper.HISTORY_TABLE_NAME + " WHERE Id=" + pid;
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.getCount() > 0) {
                myLogger.i("ye,get history ok");
                cursor.moveToFirst();
                listBean = parseWatchBean(cursor);
            }
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return listBean;
    }

    /**
     * 将查找到的数据转换成RecentWatchBean类
     */
    private RecentWatchBean.ListBean parseWatchBean(Cursor cursor) {
        RecentWatchBean.ListBean bean = new RecentWatchBean.ListBean();
        bean.setId(cursor.getInt(cursor.getColumnIndex("Id")));
        bean.setBookmarktime(cursor.getString(cursor.getColumnIndex("Bookmarktime")));
        bean.setBookmarkdate(cursor.getString(cursor.getColumnIndex("Bookmarkdate")));
        bean.setEpisode(cursor.getInt(cursor.getColumnIndex("Episode")));
        bean.setTerminalStateUrl(cursor.getString(cursor.getColumnIndex("Terminalstateurl")));
        bean.setName(cursor.getString(cursor.getColumnIndex("Name")));
        bean.setCategoryid(cursor.getString(cursor.getColumnIndex("Categoryid")));
        bean.setType(cursor.getString(cursor.getColumnIndex("Type")));
        bean.setSeriestype(cursor.getString(cursor.getColumnIndex("Seriestype")));
        bean.setHdtv(cursor.getString(cursor.getColumnIndex("Hdtv")));
        bean.setImage(cursor.getString(cursor.getColumnIndex("Image")));
        bean.setScore(cursor.getString(cursor.getColumnIndex("Score")));
        bean.setCount(cursor.getString(cursor.getColumnIndex("Count")));
        bean.setAdult(cursor.getString(cursor.getColumnIndex("Adult")));
        bean.setTday(cursor.getString(cursor.getColumnIndex("Tday")));
        bean.setDetailUrl(cursor.getString(cursor.getColumnIndex("DetailUrl")));
        return bean;
    }


    /**
     * 统计查询
     */
    public synchronized int getCount(String tabName, String id) {
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            // select count(Id) from Orders where Country = 'China'
            cursor = db.query(tabName,
                    new String[]{"COUNT(Id)"},
                    "Id = ?",
                    new String[]{id},
                    null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            myLogger.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return count;
    }


    /**
     * 收藏本地保存
     */
    public void saveFavoriteList(int favoriteId, String programType, GetCollectBean getCollectBean) {

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            myLogger.i("favoriteId=" + favoriteId);
            List<GetCollectBean.ListBean> beanList;
            if (ButterflyDataSource.PROGRAMTYPE_LIVE.equals(programType))
                beanList = getCollectBean.getChannel();
            else
                beanList = getCollectBean.getList();
            for (GetCollectBean.ListBean listBean : beanList) {
                db.replace(MoceanDbHelper.FAVORITE_TABLE_NAME, null, getContentValues(favoriteId, programType,
                        listBean));
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

    }

    private ContentValues getContentValues(int favoriteId, String programType, GetCollectBean.ListBean bean) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("Id", bean.getId());
        contentValues.put("FavoriteId", favoriteId);
        contentValues.put("ProgramType", programType);
        contentValues.put("Name", bean.getName());
        contentValues.put("Categoryid", bean.getCategoryid());
        contentValues.put("Image", bean.getImage());
        contentValues.put("Type", bean.getType());
        contentValues.put("Adult", bean.getAdult());
        contentValues.put("DetailUrl", bean.getDetailUrl());
        contentValues.put("Createtime", bean.getCreatetime());
        contentValues.put("Seriestype", bean.getSeriestype());
        contentValues.put("Sumseries", bean.getSumseries());
        contentValues.put("Newseries", bean.getNewseries());
        contentValues.put("Hdtv", bean.getHdtv());
        contentValues.put("Score", bean.getScore());
        contentValues.put("Count", bean.getCount());
        contentValues.put("Tday", bean.getTday());
        contentValues.put("Number", bean.getNumber());
        contentValues.put("TimeshiftLength", bean.getTimeshiftLength());
        contentValues.put("StorageLength", bean.getStorageLength());
        contentValues.put("Terminalstateurl", bean.getTerminalStateUrl());
        contentValues.put("CbidUrl", bean.getCbidUrl());
        contentValues.put("ScheduleListUrl", bean.getScheduleListUrl());
        contentValues.put("CurrentSchedule", bean.getCurrentSchedule());
        return contentValues;
    }


    /**
     * 获取本地收藏缓存
     */
    public GetCollectBean getFavoriteList(final int favouriteId, final String programtype, final int start, final
    int total) {
        GetCollectBean getCollectBean = new GetCollectBean();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            myLogger.i("favoriteId=" + favouriteId + " " + start + " -" + total);
            String sql = "SELECT * FROM " + MoceanDbHelper.FAVORITE_TABLE_NAME + " WHERE FavoriteId=" + favouriteId +
                    " AND ProgramType=" + programtype +
                    " ORDER BY datetime(Createtime) " +
                    "DESC  LIMIT " + start + ", " + total;
            Cursor cursor = db.rawQuery(sql, null);
            List<GetCollectBean.ListBean> beanList = new ArrayList<>();
            while (cursor.moveToNext()) {
                GetCollectBean.ListBean bean = parseCollectBean(cursor);
                beanList.add(bean);
            }
            getCollectBean.setCurrStartIdx(start);
            // getCollectBean.setTotalNum(total);
            if (ButterflyDataSource.PROGRAMTYPE_LIVE.equals(programtype))
                getCollectBean.setChannel(beanList);
            else
                getCollectBean.setList(beanList);

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return getCollectBean;
    }

    /**
     * 将查找到的数据转换成ListBean类
     */
    private GetCollectBean.ListBean parseCollectBean(Cursor cursor) {
        GetCollectBean.ListBean bean = new GetCollectBean.ListBean();
        bean.setId(cursor.getInt(cursor.getColumnIndex("Id")));
        bean.setName(cursor.getString(cursor.getColumnIndex("Name")));
        bean.setCategoryid(cursor.getString(cursor.getColumnIndex("Categoryid")));
        bean.setImage(cursor.getString(cursor.getColumnIndex("Image")));
        bean.setType(cursor.getString(cursor.getColumnIndex("Type")));
        bean.setAdult(cursor.getString(cursor.getColumnIndex("Adult")));
        bean.setDetailUrl(cursor.getString(cursor.getColumnIndex("DetailUrl")));
        bean.setCreatetime(cursor.getString(cursor.getColumnIndex("Createtime")));
        bean.setSeriestype(cursor.getString(cursor.getColumnIndex("Seriestype")));
        bean.setSumseries(cursor.getString(cursor.getColumnIndex("Sumseries")));
        bean.setNewseries(cursor.getString(cursor.getColumnIndex("Newseries")));
        bean.setHdtv(cursor.getString(cursor.getColumnIndex("Hdtv")));
        bean.setScore(cursor.getString(cursor.getColumnIndex("Score")));
        bean.setCount(cursor.getString(cursor.getColumnIndex("Count")));
        bean.setTday(cursor.getString(cursor.getColumnIndex("Tday")));

        bean.setNumber(cursor.getInt(cursor.getColumnIndex("Number")));
        bean.setTimeshiftLength(cursor.getString(cursor.getColumnIndex("TimeshiftLength")));
        bean.setStorageLength(cursor.getString(cursor.getColumnIndex("StorageLength")));
        bean.setTerminalStateUrl(cursor.getString(cursor.getColumnIndex("Terminalstateurl")));
        bean.setCbidUrl(cursor.getString(cursor.getColumnIndex("CbidUrl")));
        bean.setScheduleListUrl(cursor.getString(cursor.getColumnIndex("ScheduleListUrl")));
        bean.setCurrentSchedule(cursor.getString(cursor.getColumnIndex("CurrentSchedule")));

        return bean;
    }


    /**
     * 添加收藏
     */

    public boolean insertFavorite(int favoriteId, String programType, GetCollectBean.ListBean bean) {
        boolean res = false;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.replace(MoceanDbHelper.FAVORITE_TABLE_NAME, null, getContentValues(favoriteId, programType, bean));
            db.setTransactionSuccessful();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return res;
    }

    /**
     * 删除收藏
     */

    public boolean deteleFavorite(int id) {
        boolean res = false;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            db.delete(MoceanDbHelper.FAVORITE_TABLE_NAME, "Id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
            res = true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return res;
    }

    /**
     * 统计 ++ -- ,主要是用来统计历史，收藏
     */
    public static final int HISTORY_ID = 957;
    public static final int FAV_MOVIES_ID = 94;
    public static final int FAV_SERIES_ID = 1024;
    public static final int FAV_LIVE_ID = 666;

    public synchronized void saveTotal(int id, int total) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("Id", id);
            contentValues.put("Total", total);
            db.replace(MoceanDbHelper.COUNT_TABLE_NAME, null, contentValues);
            db.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public synchronized int getTotal(int id) {
        int total = 0;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            String sql = "SELECT * FROM " + MoceanDbHelper.COUNT_TABLE_NAME + " WHERE Id=" + id;
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getInt(cursor.getColumnIndex("Total"));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return total;
    }


    public synchronized void saveAllMovies(String table, VodSeriesBean vodSeriesBean) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            for (VodSeriesBean.ListBean bean : vodSeriesBean.getList()) {
                db.replace(table, null, getContentValues(bean));
            }
            db.setTransactionSuccessful();

            myLogger.i("save done");

        } catch (Exception e) {
            myLogger.e("" + e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    private final String ALL = "All";

    public synchronized VodSeriesBean searchAllPrograms(String table, int start, int total, String show, String area,
                                                        String
                                                                type, String year) {

        VodSeriesBean vodSeriesBean = new VodSeriesBean();

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            db.beginTransaction();
            myLogger.i("searchAllMovies=" + " " + start + " - " + total);
            String sql = "";

            if (ALL.equals(show) && ALL.equals(area) && ALL.equals(type) && ALL.equals(year)) {
                sql = "SELECT * FROM " + table +
                        " DESC  LIMIT " + start + " , " + (total - start);
            } else {
                if (!ALL.equals(area)) {
                    sql = "SELECT * FROM " + table + " WHERE Language = '" + area + "'" +
                            (ALL.equals(show) ? "" : " AND Displaylevel = '" + show + "'") +
                            (ALL.equals(type) ? "" : " AND Type = '" + type + "'") +
                            (ALL.equals(year) ? "" : " AND Releaseyear = '" + year + "'") +
                            " LIMIT " + (total - start) + " OFFSET " + start;
                } else if (!ALL.equals(type)) {
                    sql = "SELECT * FROM " + table + " WHERE Type = '" + type + "'" +
                            (ALL.equals(show) ? "" : " AND Displaylevel = '" + show + "'") +
                            (ALL.equals(area) ? "" : " AND Language = '" + area + "'") +
                            (ALL.equals(year) ? "" : " AND Releaseyear = '" + year + "'") +
                            " LIMIT " + (total - start) + " OFFSET " + start;
                } else if (!ALL.equals(year)) {
                    sql = "SELECT * FROM " + table + " WHERE Releaseyear = '" + year + "'" +
                            (ALL.equals(show) ? "" : " AND Displaylevel = '" + show + "'") +
                            (ALL.equals(area) ? "" : " AND Language = '" + area + "'") +
                            (ALL.equals(type) ? "" : " AND Type = '" + type + "'") +
                            " LIMIT " + (total - start) + " OFFSET " + start;
                } else if (!ALL.equals(show)) {
                    sql = "SELECT * FROM " + table + " WHERE Displaylevel = '" + show + "'" +
                            (ALL.equals(area) ? "" : " AND Language = '" + area + "'") +
                            (ALL.equals(year) ? "" : " AND Releaseyear = '" + year + "'") +
                            (ALL.equals(type) ? "" : " AND Type = '" + type + "'") +
                            " LIMIT " + (total - start) + " OFFSET " + start;
                }

            }

            myLogger.i("search all movies sql=" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            List<VodSeriesBean.ListBean> beanList = new ArrayList<>();
            while (cursor.moveToNext()) {
                VodSeriesBean.ListBean bean = parseVodBean(cursor);
                beanList.add(bean);
            }

            vodSeriesBean.setCurrStartIdx(start);
            vodSeriesBean.setList(beanList);
            myLogger.i("query size=" + beanList.size());

            cursor.close();

        } catch (Exception e) {
            myLogger.e("" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return vodSeriesBean;
    }

    public synchronized int getAllProgramsCount(String table) {
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String sql = "SELECT COUNT(*) FROM " + table;
            SQLiteStatement statement = db.compileStatement(sql);
            count = (int) statement.simpleQueryForLong();

            myLogger.i("all movies size=" + count);

        } catch (Exception e) {
            myLogger.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return count;
    }

    private ContentValues getContentValues(VodSeriesBean.ListBean bean) {

        ContentValues contentValues = new ContentValues();
        contentValues.put("Id", bean.getId());
        contentValues.put("Name", bean.getName());
        contentValues.put("Image", bean.getImage());
        contentValues.put("Language", bean.getLanguage());
        contentValues.put("Country", bean.getCountry());
        contentValues.put("Type", bean.getType());
        contentValues.put("Releaseyear", bean.getReleaseyear());
        contentValues.put("DetailUrl", bean.getDetailUrl());
        contentValues.put("Displaylevel", bean.getDisplaylevel());

        return contentValues;
    }

    /**
     * 将查找到的数据转换成ListBean类
     */
    private VodSeriesBean.ListBean parseVodBean(Cursor cursor) {

        VodSeriesBean.ListBean bean = new VodSeriesBean.ListBean();
        bean.setId(cursor.getInt(cursor.getColumnIndex("Id")));
        bean.setName(cursor.getString(cursor.getColumnIndex("Name")));
        bean.setImage(cursor.getString(cursor.getColumnIndex("Image")));
        bean.setType(cursor.getString(cursor.getColumnIndex("Type")));
        bean.setDetailUrl(cursor.getString(cursor.getColumnIndex("DetailUrl")));
        bean.setLanguage(cursor.getString(cursor.getColumnIndex("Language")));
        bean.setCountry(cursor.getString(cursor.getColumnIndex("Country")));
        bean.setReleaseyear(cursor.getString(cursor.getColumnIndex("Releaseyear")));
        bean.setDisplaylevel(cursor.getString(cursor.getColumnIndex("Displaylevel")));

        return bean;
    }


    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}

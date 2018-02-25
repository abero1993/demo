package com.abero.utils;


import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.jr.moc.BaseConstant;
import dev.jr.moc.data.jsonbeans.BookMarkBean;
import dev.jr.moc.data.jsonbeans.ChannelListBean;
import dev.jr.moc.data.jsonbeans.CollectBean;
import dev.jr.moc.data.jsonbeans.CollectClassifyBean;
import dev.jr.moc.data.jsonbeans.CountryListBean;
import dev.jr.moc.data.jsonbeans.EpgServerBean;
import dev.jr.moc.data.jsonbeans.ErrorBean;
import dev.jr.moc.data.jsonbeans.GetCollectBean;
import dev.jr.moc.data.jsonbeans.RecentWatchBean;
import dev.jr.moc.data.jsonbeans.SearchParmBean;
import dev.jr.moc.data.jsonbeans.VodDetailBean;
import dev.jr.moc.data.jsonbeans.VodSeriesBean;
import dev.jr.moc.data.source.ButterflyDataSource;
import dev.jr.moc.data.source.remote.IVideoCategory;

/**
 * Created by abero on 2017/2/10 0010.
 */

public class LocalDataSource extends ButterflyLocalDataSource {

    private static final String TAG = "LocalDataSource";
    private static LocalDataSource INSTANCE;
    private DBManager mDBManager;
    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    private Handler mLocalHandler;

    public static LocalDataSource getInstance(Context context) {
        synchronized (LocalDataSource.class) {
            if (null == INSTANCE) {
                INSTANCE = new LocalDataSource(context);
            }
            return INSTANCE;
        }
    }

    private LocalDataSource(Context context) {
        mDBManager = DBManager.getInstance(context);
        mLocalHandler = new Handler(context.getMainLooper());
    }


    @Override
    public void login(String user, String password, DataCallback callback) {

    }

    @Override
    public void reLogin(DataCallback callback) {

    }

    @Override
    public void active(String tag, String activeCode, String mac, int cid, int pid, int mid, DataCallback callback) {

    }

    @Override
    public void getIndex(final DataCallback callback) {

        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String result = mDBManager.getData(ButterflyKey.INDEX);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJson(result, EpgServerBean.class, callback);
                    }
                });
            }
        });

        /*if (result.equals("")) {
            ErrorBean errorBean = new ErrorBean();
            errorBean.setCode(ErrorBean.NULL_CODE);
            callback.onFail(errorBean);
        } else {
            try {
                Gson gson = new Gson();
                EpgServerBean epgServerBean = gson.fromJson(result, EpgServerBean.class);
                callback.onSuccess(epgServerBean);
            } catch (JsonSyntaxException e) {
                ErrorBean errorBean = new ErrorBean();
                errorBean.setCode(ErrorBean.JSON_ERROR_CODE);
                callback.onFail(errorBean);
            }
        }*/
    }

    private <T> void fromJson(String jsonstr, Class<T> clazz, DataCallback callback) {
        if (jsonstr.equals("")) {
            ErrorBean errorBean = new ErrorBean();
            errorBean.setCode(ErrorBean.NULL_CODE);
            callback.onFail(errorBean);
        } else {
            try {
                Gson gson = new Gson();
                T bean = gson.fromJson(jsonstr, clazz);
                callback.onSuccess(bean);
            } catch (JsonSyntaxException e) {
                ErrorBean errorBean = new ErrorBean();
                errorBean.setCode(ErrorBean.JSON_ERROR_CODE);
                callback.onFail(errorBean);
            }
        }
    }

    @Override
    public void getCategory(final String type, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String result = mDBManager.getData(type);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (IVideoCategory.VIDEO_TYPE_IPTV.equals(type))
                            fromJson(result, CountryListBean.class, callback);
                        else if (IVideoCategory.VIDEO_TYPE_VOD.equals(type))
                            fromJson(result, CountryListBean.class, callback);
                        else if (IVideoCategory.VIDEO_TYPE_MYTUBU.equals(type))
                            fromJson(result, CountryListBean.class, callback);
                        else
                            callback.onFail(new ErrorBean());
                    }
                });

            }
        });
    }

    @Override
    public void getVodProgramList(final String url, final int total, final int page, boolean isforce, final DataCallback
            callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData(url + total + page);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJson(jsonstr, VodSeriesBean.class, callback);
                    }
                });

            }
        });
    }

    @Override
    public void getAllMoviesProgramList(final String url, final int total, final int page, final String show, final
    String area, final String type, final String year, final DataCallback callback) {

        getAllPrograms(MoceanDbHelper.ALL_MOVIES_TABLE_NAME, url, total, page, show, area, type, year, callback);
    }

    @Override
    public void getAllSeriesProgramList(String url, int total, int page, String show, String area, String type,
                                        String year, DataCallback callback) {
        getAllPrograms(MoceanDbHelper.ALL_SERIES_TABLE_NAME, url, total, page, show, area, type, year, callback);
    }

    private void getAllPrograms(final String table, final String url, final int total, final int page, final String
            show, final String area, final String type, final String year, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData(url + total + page + show + area + type + year);
                if (!TextUtils.isEmpty(jsonstr)) {
                    mLocalHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myLogger.i("get all movies form query cache");
                            fromJson(jsonstr, VodSeriesBean.class, callback);
                        }
                    });
                } else {

                    final int count = mDBManager.getAllProgramsCount(table);
                    if (count > 0) {
                        final VodSeriesBean vodSeriesBean = mDBManager.searchAllPrograms(table, page * total, (page
                                        + 1) *
                                        total, show,
                                area, type, year);
                        vodSeriesBean.setTotalNum(count);
                        mDBManager.insertData(url + total + page + show + area + type + year, new Gson().toJson
                                (vodSeriesBean));
                        mLocalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                myLogger.i("get all movies form query");
                                callback.onSuccess(vodSeriesBean);
                            }
                        });
                    } else {
                        mLocalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                myLogger.i("get all movies local error");
                                ErrorBean errorBean = new ErrorBean();
                                errorBean.setCode(ErrorBean.NULL_CODE);
                                callback.onFail(errorBean);
                            }
                        });
                    }

                }
            }
        });
    }

    @Override
    public void getVodDetail(final String detailUrl, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData(detailUrl);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJson(jsonstr, VodDetailBean.class, callback);
                    }
                });
            }
        });
    }

    @Override
    public void getVodTerminalState(String terminalState, DataCallback callback) {

    }

    @Override
    public void getRelativeRecommend(String relativeRecommendUrl, DataCallback callback) {

    }

    @Override
    public void getLiveChannelList(final String url, final int total, final int page, boolean isforce, final
    DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData(url + total + page);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJson(jsonstr, ChannelListBean.class, callback);
                    }
                });

            }
        });
    }

    @Override
    public void getLiveDetail(String detailUrl, DataCallback callback) {

    }

    @Override
    public void getEPGLanguagleList(final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData("getEPGLanguagleList");
                myLogger.i("getEPGLanguagleList =" + jsonstr);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (jsonstr.equals("")) {
                            ErrorBean errorBean = new ErrorBean();
                            errorBean.setCode(ErrorBean.NULL_CODE);
                            callback.onFail(errorBean);
                        } else {
                            try {
                                Gson gson = new Gson();
                                List<String> lists = gson.fromJson(jsonstr, new TypeToken<List<String>>() {
                                }.getType());
                                callback.onSuccess(lists);
                            } catch (JsonSyntaxException e) {
                                ErrorBean errorBean = new ErrorBean();
                                errorBean.setCode(ErrorBean.JSON_ERROR_CODE);
                                callback.onFail(errorBean);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void getLiveSchedule(String scheduleUrl, DataCallback callback) {

    }

    @Override
    public void getLiveTerminalState(String terminalstateUrl, DataCallback callback) {

    }

    @Override
    public void getFilter(final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData("filter");
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJsonList(jsonstr, callback);
                    }
                });

            }
        });
    }

    private void fromJsonList(String jsonstr, DataCallback callback) {
        if (jsonstr.equals("")) {
            ErrorBean errorBean = new ErrorBean();
            errorBean.setCode(ErrorBean.NULL_CODE);
            callback.onFail(errorBean);
        } else {
            try {
                Gson gson = new Gson();
                List<SearchParmBean> lists = gson.fromJson(jsonstr, new
                        TypeToken<List<SearchParmBean>>() {
                        }.getType());
                callback.onSuccess(lists);
            } catch (JsonSyntaxException e) {
                ErrorBean errorBean = new ErrorBean();
                errorBean.setCode(ErrorBean.JSON_ERROR_CODE);
                callback.onFail(errorBean);
            }
        }
    }

    @Override
    public void search(String tag, SearchParmBean filter, String keyWord, int start, int total, DataCallback
            callback) {

    }

    @Override
    public void getFavouritesCategory(final int type, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String jsonstr = mDBManager.getData("" + type);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fromJson(jsonstr, CollectClassifyBean.class, callback);
                    }
                });

            }
        });
    }

    @Override
    public void favourite(final String url, final int favouriteId, final String action, final GetCollectBean.ListBean
            bean,
                          final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == url) {
                    mLocalHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ErrorBean errorBean = new ErrorBean();
                            errorBean.setCode(ErrorBean.UNKNOWN_ERROR_CODE);
                            callback.onFail(errorBean);
                        }
                    });

                } else {

                    boolean res = false;
                    String des;
                    int count = 0;
                    int total;
                    int totalId;
                    final CollectBean collectBean = new CollectBean();
                    String programType;

                    if (url.contains(ButterflyDataSource.SOURCETYPE_LIVETV)) {
                        programType = PROGRAMTYPE_LIVE;
                    } else if (url.contains(ButterflyDataSource.SOURCETYPE_SERIES)) {
                        programType = PROGRAMTYPE_SERIE;
                    } else {
                        programType = PROGRAMTYPE_MOVIE;
                    }

                    totalId = getFavoriteTotalId(programType);
                    total = mDBManager.getTotal(totalId);
                    myLogger.i("total=" + total);

                    if (ACTION_COLLECT.equals(action)) {
                        des = "local Collection";
                        count = mDBManager.getCount(MoceanDbHelper.FAVORITE_TABLE_NAME, "" + bean.getId());
                        res = mDBManager.insertFavorite(favouriteId, programType, bean);
                        if (0 == count)
                            total++;
                        myLogger.i("count=" + count);
                    } else {
                        des = "local Cancle Collection";
                        res = mDBManager.deteleFavorite(bean.getId());
                        total--;
                    }

                    if (res) {
                        des = des + " success";
                        collectBean.setReturnCode("0");
                        mDBManager.saveTotal(totalId, total);
                    } else {
                        des = des + " failed";
                        collectBean.setReturnCode("1");
                    }

                    myLogger.i("local favorite :" + des + " " + res);
                    collectBean.setDescription(des);
                    mLocalHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(collectBean);
                        }
                    });

                }

            }
        });
    }

    @Override
    public void getFavourites(final int favouriteId, final String programtype, final int start, final int total,
                              boolean isRefresh, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {

                final GetCollectBean getCollectBean = mDBManager.getFavoriteList(favouriteId, programtype, start,
                        total);

                if (getCollectBean != null && getFavoriteTotalId(programtype) >= 0) {
                    getCollectBean.setTotalNum(mDBManager.getTotal(getFavoriteTotalId(programtype)));
                    myLogger.i("get local favorite total=" + getCollectBean.getTotalNum());
                }

                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        ErrorBean errorBean = new ErrorBean();
                        errorBean.setCode(ErrorBean.UNKNOWN_ERROR_CODE);

                        if (PROGRAMTYPE_LIVE.equals(programtype)) {
                            if (getCollectBean != null && getCollectBean.getChannel() != null && getCollectBean
                                    .getChannel().size() >= 0)
                                callback.onSuccess(getCollectBean);
                            else
                                callback.onFail(errorBean);
                        } else {
                            if (getCollectBean != null && getCollectBean.getList() != null && getCollectBean.getList
                                    ().size() >= 0)
                                callback.onSuccess(getCollectBean);
                            else
                                callback.onFail(errorBean);
                        }
                    }
                });

            }
        });
    }

    @Override
    public void getFavourite(final int pid, final String programtype, final DataCallback callback) {

        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final int count = mDBManager.getCount(MoceanDbHelper.FAVORITE_TABLE_NAME, "" + pid);
                myLogger.i("pid count=" + count);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(count);
                    }
                });

            }
        });
    }

    @Override
    public void bookmark(final String pid, String sourcetype, String bookmarktime, final String action, final
    RecentWatchBean
            .ListBean bean, final DataCallback callback) {

        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final BookMarkBean bookBean = new BookMarkBean();
                int total = mDBManager.getTotal(DBManager.HISTORY_ID);
                myLogger.i("total=" + total);
                if (ACTION_ADD == action) {
                    int count = mDBManager.getCount(MoceanDbHelper.HISTORY_TABLE_NAME, pid);
                    myLogger.i("count=" + count);
                    boolean res = mDBManager.insertHistory(bean);
                    if (res) {
                        bookBean.setReturnCode("0");
                        bookBean.setDescription("local bookmark success");
                        if (0 == count)
                            mDBManager.saveTotal(DBManager.HISTORY_ID, ++total);
                    } else {
                        bookBean.setReturnCode("1");
                        bookBean.setDescription("local bookmark failed");
                    }

                } else {
                    boolean res = mDBManager.deleteHistory(pid);
                    myLogger.i("detele " + pid + " =" + res);
                    if (res) {
                        bookBean.setReturnCode("0");
                        bookBean.setDescription("local Delete bookmark  success");
                        mDBManager.saveTotal(DBManager.HISTORY_ID, --total);
                    } else {
                        bookBean.setReturnCode("1");
                        bookBean.setDescription("local Delete bookmark  failed");
                    }
                }

                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(bookBean);
                    }
                });

            }

        });
    }


    @Override
    public void getBookmarkList(final int start, final int total, HistoryStrategy strategy, final DataCallback
            callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final RecentWatchBean recentWatchBean = mDBManager.queryHistory(start, total);
                if (recentWatchBean != null)
                    recentWatchBean.setTotalNum(mDBManager.getTotal(DBManager.HISTORY_ID));

                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (recentWatchBean != null && recentWatchBean.getList() != null && recentWatchBean.getList()
                                .size()
                                >= 0) {
                            myLogger.i("get local history=" + recentWatchBean.getList().size() + " total=" +
                                    recentWatchBean.getTotalNum());
                            callback.onSuccess(recentWatchBean);

                        } else {
                            myLogger.i("get local null");
                            ErrorBean errorBean = new ErrorBean();
                            errorBean.setCode(ErrorBean.UNKNOWN_ERROR_CODE);
                            callback.onFail(errorBean);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void getBookmark(final int pid, final DataCallback callback) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final RecentWatchBean.ListBean listBean = mDBManager.getHistory(pid);
                mLocalHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listBean != null) {
                            callback.onSuccess(listBean);
                        } else {
                            ErrorBean errorBean = new ErrorBean();
                            errorBean.setCode(ErrorBean.UNKNOWN_ERROR_CODE);
                            callback.onFail(errorBean);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void heart(int streamStatus, DataCallback callback) {

    }

    @Override
    public void cancleAll(String tag) {

    }

    @Override
    public void clean() {
        INSTANCE = null;
    }

    @Override
    public void insertData(final String url, final String data) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDBManager.insertData(url, data);
            }
        });
    }

    @Override
    public void dirtyData(String url) {
        insertData(url, "");
    }

    @Override
    public void saveHistory(final RecentWatchBean recentWatchBean) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDBManager.saveHistroy(recentWatchBean);
                mDBManager.saveTotal(DBManager.HISTORY_ID, recentWatchBean.getTotalNum());
                if (BaseConstant.Version.develop == BaseConstant.appVersion)
                    myLogger.i("save history total=" + mDBManager.getTotal(DBManager.HISTORY_ID));
            }
        });
    }

    @Override
    public void saveFavoriteList(final int favoriteId, final String programType, final GetCollectBean getCollectBean) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDBManager.saveFavoriteList(favoriteId, programType, getCollectBean);

                int totalId = getFavoriteTotalId(programType);
                if (totalId > 0)
                    mDBManager.saveTotal(totalId, getCollectBean.getTotalNum());

                if (BaseConstant.Version.develop == BaseConstant.appVersion)
                    myLogger.i("save favoritelist total=" + mDBManager.getTotal(totalId));

            }
        });
    }

    @Override
    public void saveAllMovies(String url, final VodSeriesBean vodSeriesBean) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                myLogger.i("save all movies");
                //mDBManager.deleteTable(MoceanDbHelper.JSON_TABLE_NAME);
                mDBManager.deleteTable(MoceanDbHelper.ALL_MOVIES_TABLE_NAME);
                mDBManager.saveAllMovies(MoceanDbHelper.ALL_MOVIES_TABLE_NAME, vodSeriesBean);
            }
        });
    }

    @Override
    public void saveAllSeries(String url, final VodSeriesBean vodSeriesBean) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                myLogger.i("save all series");
                //mDBManager.deleteTable(MoceanDbHelper.JSON_TABLE_NAME);
                mDBManager.deleteTable(MoceanDbHelper.ALL_SERIES_TABLE_NAME);
                mDBManager.saveAllMovies(MoceanDbHelper.ALL_SERIES_TABLE_NAME, vodSeriesBean);
            }
        });
    }

    private int getFavoriteTotalId(String programType) {

        if (null == programType)
            return -1;

        int totalId = -1;
        if (PROGRAMTYPE_LIVE.equals(programType))
            totalId = DBManager.FAV_LIVE_ID;
        else if (PROGRAMTYPE_SERIE.equals(programType))
            totalId = DBManager.FAV_SERIES_ID;
        else if (PROGRAMTYPE_MOVIE.equals(programType))
            totalId = DBManager.FAV_MOVIES_ID;

        return totalId;
    }
}

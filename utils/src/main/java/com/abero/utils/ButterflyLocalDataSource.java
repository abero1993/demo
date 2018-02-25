package com.abero.utils;

import dev.jr.moc.data.jsonbeans.GetCollectBean;
import dev.jr.moc.data.jsonbeans.RecentWatchBean;
import dev.jr.moc.data.jsonbeans.VodSeriesBean;
import dev.jr.moc.data.source.ButterflyDataSource;

/**
 * Created by Administrator on 2017/5/22.
 */

public abstract class ButterflyLocalDataSource extends ButterflyDataSource {

    public abstract void insertData(String url, String data);

    public abstract void dirtyData(String url);

    public abstract void saveHistory(RecentWatchBean recentWatchBean);

    public abstract void saveFavoriteList(int favoriteId, String programType, GetCollectBean getCollectBean);

    public abstract void saveAllMovies(String url, VodSeriesBean vodSeriesBean);

    public abstract void saveAllSeries(String url, VodSeriesBean vodSeriesBean);
}

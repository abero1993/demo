package com.abero.utils.play;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.share.model.ShareLinkContent;
import com.google.gson.Gson;
import com.motube.mydanmaku.DanmakuBean;
import com.motube.mydanmaku.DanmakuTask;
import com.ocean.motube.R;
import com.ocean.motube.hj.entity.GetSubscribeBean;
import com.ocean.motube.hj.entity.MediaBean;
import com.ocean.motube.hj.entity.MediaComment;
import com.ocean.motube.hj.entity.PlayMessageBean;
import com.ocean.motube.hj.entity.PlaylistBean;
import com.ocean.motube.hj.http.ApiException;
import com.ocean.motube.hj.http.HttpMethods;
import com.ocean.motube.hj.local.LoginInfoConfig;
import com.ocean.motube.hj.subscribers.ProgressSubscriber;
import com.ocean.motube.hj.subscribers.SubscriberOnNextListener;
import com.ocean.motube.util.MyLogger;
import com.ocean.motube.util.WiFiUtils;
import com.ocean.motube.view.setting.SettingConfig;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by abero on 2017/9/14.
 */

public class VideoPlayPresenter implements VideoPlayContract.Presenter, DanmakuTask.TaskCallback {

    private MyLogger myLogger = MyLogger.getAberoLog();
    public static final String APP_ID = "wxd930ea5d5a258f4f";
    private IWXAPI api;
    private HttpMethods httpMethods;
    private VideoPlayContract.View playView;
    private long mediaId;
    private String playUrl;
    private List<PlaylistBean> list;
    private boolean isFavorited = false;
    private boolean isFollow = false;
    private boolean isShowDanmaku = true;
    private boolean isLike = false;
    private String uploadName;
    private String avatar;
    private int currentHD = 0;
    private long progress;
    private PlayMessageBean playMessageBean;
    private DanmakuTask danmakuTask;
    private DanmakuBean.DataBean dataBean;
    private boolean isUse234G = false;
    private boolean isExpand = false;

    //reply
    private String commentUser = "heilahfhfievnkdfjdlsls";
    private long commentId;

    public VideoPlayPresenter(HttpMethods httpMethods, VideoPlayContract.View playView, long mediaId) {
        this.httpMethods = httpMethods;
        this.playView = playView;
        this.playView.setPresenter(this);
        this.mediaId = mediaId;
        danmakuTask = new DanmakuTask(this);
        dataBean = new DanmakuBean.DataBean();
        dataBean.setRadio_id((int) mediaId);
        dataBean.setType("vod");
    }


    @Override
    public void start() {
        getPlayRecord();
        dataBean.setDirection(danmakuSetting.getDirection());
        dataBean.setSize(danmakuSetting.getTextSize());
        dataBean.setColor(danmakuSetting.getTextColor());
        dataBean.setUser_id(LoginInfoConfig.getUsername());
    }

    private void getPlayRecord() {
        isUse234G = SettingConfig.isUse234G();

        httpMethods.watchHistory(new ProgressSubscriber<List<MediaBean>>(new SubscriberOnNextListener<List<MediaBean>>() {
            @Override
            protected void onNext(List<MediaBean> list) {
                for (MediaBean bean : list) {
                    if (bean.getMediaId() == mediaId) {
                        progress = (long) bean.getPauseTime();
                        myLogger.i("progress=" + progress);
                        break;
                    }
                }
                getVideoDetail();
            }

            @Override
            protected void onError(ApiException apiException) {
                getVideoDetail();
            }
        }), 0, 1000, true);
    }

    @Override
    public void getVideoDetail() {

        if (!WiFiUtils.isNetworkAvailable()) {
            playView.showError(getString(R.string.no_connection));
            return;
        }

        playView.showContentLoading(true);
        //BaseApplication baseApplication = (BaseApplication) playView.getContext().getApplicationContext();
        //baseApplication.login();
        httpMethods.getPlayMessage(new ProgressSubscriber<PlayMessageBean>(new SubscriberOnNextListener<PlayMessageBean>() {
                    @Override
                    protected void onNext(PlayMessageBean bean) {
                        myLogger.i("detail=" + new Gson().toJson(bean));
                        if (!playView.isActive())
                            return;
                        playMessageBean = bean;
                        playView.showContentLoading(false);
                        if (bean != null) {
                            int views = bean.getMediaViewNum();
                            String viewsStr = "";
                            if (views > Math.pow(10, 6)) {
                                viewsStr = "" + (views / Math.pow(10.f, 6)) + "M";
                            } else if (views > Math.pow(10, 3)) {
                                viewsStr = "" + (views / Math.pow(10.f, 3)) + "K";
                            } else {
                                viewsStr = "" + views;
                            }

                            int favNum = bean.getMediaFavNum();
                            String favNumStr = "";
                            if (favNum > Math.pow(10, 6)) {
                                favNumStr = "" + (favNum / Math.pow(10.f, 6)) + "M";
                            } else if (favNum > Math.pow(10, 3)) {
                                favNumStr = "" + (favNum / Math.pow(10.f, 3)) + "K";
                            } else {
                                favNumStr = "" + favNum;
                            }

                            int likeNum = bean.getMediaAgreeNum();
                            String likeNumStr = "";
                            if (likeNum > Math.pow(10, 6)) {
                                likeNumStr = "" + (likeNum / Math.pow(10.f, 6)) + "M";
                            } else if (likeNum > Math.pow(10, 3)) {
                                likeNumStr = "" + (likeNum / Math.pow(10.f, 3)) + "K";
                            } else {
                                likeNumStr = "" + likeNum;
                            }


                            playView.showContent(bean.getMediaName(), viewsStr, favNumStr, likeNumStr,
                                    bean.getMediaDate(), bean.getMediaDesc());

                            if (bean.getTags() != null && bean.getTags().size() > 0) {
                               /* bean.getTags().add("赌石");
                                bean.getTags().add("Love All");
                                bean.getTags().add("TOP AV");*/
                                playView.showTags(bean.getTags());
                                if (bean.getTags().size() > 3)
                                    playView.shrinkTagShow();
                                else
                                    playView.showTagsLessThanThree();
                            }

                            playView.showUser(bean.getUserAvatar(), bean.getMediaUsername());

                            List<PlayMessageBean.PlayUrlsBean> urlsBean = bean.getPlayUrls();
                            if (urlsBean != null && urlsBean.size() > 0) {
                                playUrl = queryPlayUrl(urlsBean);
                                if (WiFiUtils.isNetworkAvailable()) {
                                    if (WiFiUtils.isWifi() || isUse234G) {
                                        playView.playVideo(playUrl);
                                    } else {
                                        playView.showNotWifiDialog(playUrl);
                                    }
                                } else {
                                    playView.showPlayError(R.drawable.play_nonet, getString(R.string
                                            .home_no_network_text));
                                }
                            } else {
                                playView.showPlayError(R.drawable.play_shit, getString(R.string.play_no_url_error));
                            }

                            playView.prepareDanmaku();
                            //danmakuTask.startTaskSchedule(mediaId, "vod");
                            uploadName = bean.getMediaUsername();
                            avatar = bean.getUserAvatar();
                            getFollow(uploadName);
                        }
                    }

                    @Override
                    protected void onError(ApiException apiException) {
                        myLogger.e("detail=" + apiException.getErrcode());
                        if (!playView.isActive())
                            return;

                        playView.showContentLoading(false);
                        if (!TextUtils.isEmpty(apiException.getMessage())) {
                            if (apiException.getMessage().contains("retry"))
                                playView.showError(apiException.getMessage());
                            else
                                playView.showError(playView.getContext().getString(R.string.text_tap_to_retry,
                                        apiException.getMessage()));
                        } else {
                            playView.showError(getString(R.string.home_loading_exception_text));
                        }
                    }
                })
                /*Modify by HJ 播单ID begin*/
                //, mediaId, true);
                , 0, mediaId, true);
                /*Modify by HJ 播单ID end*/

        getComments(mediaId, true);
        getFavorite(mediaId);
    }


    private void getFollow(String userName) {
        httpMethods.getSubscribe(new ProgressSubscriber<GetSubscribeBean>(new SubscriberOnNextListener<GetSubscribeBean>() {
            @Override
            protected void onNext(GetSubscribeBean o) {
                myLogger.i("get flow =" + o);
                isFollow = o.getIsFollow() == 1 ? true : false;
                updateFollow();
            }

            @Override
            protected void onError(ApiException apiException) {
                myLogger.e("get flow =" + apiException.getErrcode());
                updateFollow();
            }
        }), userName);
    }

    private void updateFollow() {
        if (!playView.isActive())
            return;

        playView.updateFollow(isFollow);
    }


    private void getFavorite(long mediaId) {
        httpMethods.getFavorite(new ProgressSubscriber<String>(new SubscriberOnNextListener<String>() {
            @Override
            protected void onNext(String o) {
                myLogger.i("getfav=" + o);
                isFavorited = false;
                updateFavorite();
            }

            @Override
            protected void onError(ApiException apiException) {
                myLogger.e("getfav=" + apiException.getErrcode());
                if (205 == apiException.getErrcode())
                    isFavorited = true;
                updateFavorite();
            }
        }), mediaId);
    }

    private void updateFavorite() {
        if (!playView.isActive())
            return;

        playView.updateFavorite(isFavorited);
    }

    private void getComments(long mediaId, boolean isCache) {
        httpMethods.getMediaComment(new ProgressSubscriber<List<MediaComment>>(new SubscriberOnNextListener<List<MediaComment>>() {
            @Override
            protected void onNext(List<MediaComment> list) {
                if (!playView.isActive())
                    return;

                if (list != null && list.size() > 0) {
                    for (MediaComment comment : list)
                        myLogger.i("commentuser=" + comment.getCommentUsername() + " id=" + comment.getComentId() + "" +
                                " pid=" + comment.getParentId() + " com=" + comment.getCommentContent());
                    myLogger.i("commments=" + list.size());
                    playView.showComments(list);
                } else {
                    playView.showNoComments();
                }
            }

            @Override
            protected void onError(ApiException apiException) {

            }
        }), mediaId, 0, 1000, isCache);
    }

    @Override
    public void onReplay() {
        if (TextUtils.isEmpty(playUrl))
            return;

        playView.showPlayEnd(false);
        playView.showPlayLoading(true);
        if (WiFiUtils.isNetworkAvailable()) {
            if (WiFiUtils.isWifi() || isUse234G) {
                playView.playVideo(playUrl);
            } else {
                playView.showNotWifiDialog(playUrl);
            }
        } else {
            playView.showPlayError(R.drawable.play_nonet, getString(R.string
                    .home_no_network_text));
        }

    }

    @Override
    public void addPlaylist() {
        playView.showContentLoading(true);
        httpMethods.playlist(new ProgressSubscriber<List<PlaylistBean>>(new SubscriberOnNextListener<List<PlaylistBean>>() {
            @Override
            protected void onNext(List<PlaylistBean> list) {
                if (!playView.isActive())
                    return;
                playView.showContentLoading(false);
                if (list != null && list.size() > 0) {
                    VideoPlayPresenter.this.list = list;
                    playView.showPlaylistDialog(list);
                } else {
                    playView.showNoPlaylist();
                }
            }

            @Override
            protected void onError(ApiException apiException) {

            }
        }), 0, 1000, true);
    }

    @Override
    public void onPlaylistClick(int position) {
        if (list != null && position < list.size()) {
            List<Long> mediaIds = Arrays.asList((long) mediaId);
            httpMethods.savePlaylistMedia(new ProgressSubscriber<Object>(new SubscriberOnNextListener<Object>() {
                @Override
                protected void onNext(Object object) {
                    if (!playView.isActive())
                        return;

                    myLogger.i("save playlist");
                    playView.actionSuccessToast(R.string.toast_add_to_playlist_success);
                }

                @Override
                protected void onError(ApiException apiException) {
                    if (!playView.isActive())
                        return;

                    myLogger.e("error=" + apiException.getErrcode());
                    if (6286 == apiException.getErrcode())
                        playView.actionSuccessToast(R.string.toast_add_to_playlist_exist);
                    else
                        playView.actionFailedToast(apiException.getMessage());

                }

            }), list.get(position).getPlaylistId(), mediaIds, 0);
            myLogger.i("pllaylistid =" + list.get(position).getPlaylistId() + " medaiid=" + mediaId);
            String str = new Gson().toJson(mediaIds);
            myLogger.i("list=" + str.substring(1, str.length() - 1));
        }

    }

    @Override
    public void sendComment(String message) {

        playView.showContentLoading(true);
        long id = 0;
        if (!TextUtils.isEmpty(commentUser) && message.contains(commentUser)) {
            id = commentId;
            message = message.substring(commentUser.length());
        }

        if (message.trim().length() == 0) {
            playView.toastInputComments();
            playView.showContentLoading(false);
        } else {
            httpMethods.saveComment(new ProgressSubscriber<String>(new SubscriberOnNextListener<String>() {
                @Override
                protected void onNext(String o) {
                    myLogger.i("send comment success");
                    if (!playView.isActive())
                        return;
                    playView.showContentLoading(false);
                    playView.actionSuccessToast(R.string.toast_send_msg_success);
                    getComments(mediaId, false);
                }

                @Override
                protected void onError(ApiException apiException) {
                    myLogger.e("send comment=" + apiException.getErrcode());
                    if (!playView.isActive())
                        return;

                    playView.showContentLoading(false);
                    if (TextUtils.isEmpty(apiException.getMessage()))
                        playView.actionFailedToast(R.string.toast_send_msg_failed);
                    else
                        playView.actionFailedToast(apiException.getMessage());
                }
            }), mediaId, message, id);
        }
    }

    @Override
    public void onFavorite() {
        isFavorited = !isFavorited;
        playView.updateFavorite(isFavorited);
        if (isFavorited) {
            httpMethods.addFavorite(new ProgressSubscriber<String>(new SubscriberOnNextListener() {
                @Override
                protected void onNext(Object o) {
                    myLogger.i("add fav success");
                }

                @Override
                protected void onError(ApiException apiException) {
                    myLogger.e("add fav =" + apiException.getErrcode());
                    onFavoriteFailed(apiException);
                }
            }), mediaId);
        } else {
            List<Long> list = new ArrayList<>();
            list.add((long) mediaId);
            httpMethods.removeFavorite(new ProgressSubscriber<String>(new SubscriberOnNextListener() {
                @Override
                protected void onNext(Object o) {
                    myLogger.i("remore fav success");
                }

                @Override
                protected void onError(ApiException apiException) {
                    myLogger.e("remore fav=" + apiException.getErrcode());
                    onFavoriteFailed(apiException);
                }
            }), list);
        }
    }

    @Override
    public void onFollow() {
        isFollow = !isFollow;
        updateFollow();
        playView.showContentLoading(true);
        ProgressSubscriber<String> subscriber = new ProgressSubscriber<>(new SubscriberOnNextListener<String>() {
            @Override
            protected void onNext(String o) {
                myLogger.i("add follow =" + o);
                if (!playView.isActive())
                    return;
                playView.showContentLoading(false);
            }

            @Override
            protected void onError(ApiException apiException) {
                myLogger.e("remove follow =" + apiException.getErrcode());
                if (!playView.isActive())
                    return;

                playView.showContentLoading(false);

                if (6093 == apiException.getErrcode())
                    return;

                isFollow = !isFollow;
                updateFollow();
                if (TextUtils.isEmpty(apiException.getMessage()))
                    playView.actionFailedToast(R.string.toast_remove_follow_failed);
                else
                    playView.actionFailedToast(apiException.getMessage());
            }
        });
        if (isFollow) {
            httpMethods.addSubscribe(subscriber, uploadName);
        } else {
            List<String> list = new ArrayList<>();
            list.add(uploadName);
            httpMethods.removeSubscribe(subscriber, list);
        }
    }

    @Override
    public void onDanmakuStyleSelect(int direction) {
        playView.showDanmakuStyle(direction);
        danmakuSetting.putDirection(direction);
        dataBean.setDirection(direction);
    }

    @Override
    public void onDanmakuTextSizeSelect(int textsize) {
        playView.showDanmakuTextSize(textsize);
        danmakuSetting.putTextSize(textsize);
        dataBean.setSize(textsize);
    }

    @Override
    public void onDanmakuTextColorSelect(String color) {
        danmakuSetting.putTextColor(color);
        dataBean.setColor(color);
    }

    @Override
    public void onPen() {
        playView.showDanmakuTextSize(dataBean.getSize());
        playView.showDanmakuStyle(dataBean.getDirection());
        playView.updateCurentColor(dataBean.getColorStr());
        playView.hideOverlay();
        playView.showSendDanmaku(true);
    }

    @Override
    public void sendDanmaku(String dan) {
        if (TextUtils.isEmpty(dan)) {
            playView.toast(R.string.toast_send_danmaku_null);
        } else {
            playView.closeKeyboard();
            playView.showSendDanmaku(false);
            dataBean.setContent(dan);
            dataBean.setCreate_time("" + playView.getPosition());
            danmakuTask.sendDanmaku(dataBean);
            playView.showDanmaku(dataBean);
        }
    }

    @Override
    public void saveRecord() {
        progress = getProgress();
        myLogger.i("save record =" + progress);
        httpMethods.addWatchHistory(new ProgressSubscriber<Object>(new SubscriberOnNextListener() {
            @Override
            protected void onNext(Object o) {
                myLogger.i("add history=" + o);
            }

            @Override
            protected void onError(ApiException apiException) {
                myLogger.e("add history=" + apiException.getErrcode());
            }
        }), mediaId, progress);
    }

    @Override
    public void onDanmakuSwitch() {
        isShowDanmaku = !isShowDanmaku;
        playView.changeDanmakuSwitch(isShowDanmaku);
        if (isShowDanmaku)
            danmakuTask.startTaskSchedule((int) mediaId, "vod");
        else
            danmakuTask.stopTaskSchedule();
    }

    @Override
    public void onStart() {
        danmakuTask.startTaskSchedule((int) mediaId, "vod");
    }

    @Override
    public void onStop() {
        danmakuTask.stopTaskSchedule();
    }

    @Override
    public void onLike() {
        isLike = !isLike;
        playView.updateLike(isLike);
        if (isLike)
            httpMethods.saveLike(new ProgressSubscriber<String>(new SubscriberOnNextListener<String>() {
                @Override
                protected void onNext(String o) {
                    myLogger.i("like =" + o);
                }

                @Override
                protected void onError(ApiException apiException) {
                    myLogger.e("like =" + apiException.getErrcode());
                    if (!playView.isActive())
                        return;

                    if (205 == apiException.getErrcode()) {
                        isLike = true;
                    } else {
                        isLike = false;
                        playView.toastLikeError("" + apiException.getErrcode());
                    }

                    playView.updateLike(isLike);

                }
            }), mediaId, 0, false);
    }

    @Override
    public void onAvatarClick() {
        playView.toPresonalShowActivity(uploadName, uploadName, avatar);
    }

    @Override
    public void onWechatShare(final boolean isMoments) {

        if (playMessageBean != null) {

            Glide.with(playView.getContext()).load(playMessageBean.getMediaThumbPath()).asBitmap().into(new SimpleTarget<Bitmap>() {

                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    myLogger.i("onResourceReady");
                    share(isMoments, resource);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    myLogger.i("onLoadFailed");
                    super.onLoadFailed(e, errorDrawable);
                    share(isMoments, null);
                }
            });

        }

    }

    @Override
    public void onShareAction(int position) {

        switch (position) {
            case 0:
                onFaceBookShare();
                break;
            case 1:
                onWechatShare(false);
                break;
            case 2:
                onWechatShare(true);
                break;

            default:
                break;
        }
    }

    @Override
    public void resolutionAction() {
        playView.showResolutionPop(playMessageBean.getPlayUrls(), currentHD);
    }

    @Override
    public void onHDselect(int position) {
        if (currentHD == position)
            return;

        currentHD = position;
        if (playMessageBean.getPlayUrls() != null && currentHD < playMessageBean.getPlayUrls().size()) {
            playUrl = playMessageBean.getPlayUrls().get(currentHD).getPlayUrl();
            playView.showResolutionText(playMessageBean.getPlayUrls().get(currentHD).getClarity());
            onReplay();
        }

    }

    @Override
    public void userMobileNetworkPlay() {
        playView.playVideo(playUrl);
    }

    @Override
    public void onVideoPrepared(IMediaPlayer mp) {
        if (progress > 0 && (mp.getDuration() - progress) > 30 * 1000) {
            playView.seekTo(progress);
            playView.showSeekToTips(String.valueOf(progress));
        }
    }

    @Override
    public void onReplyComment(String user, long commentId) {

        playView.showSendDialog();
        if (!LoginInfoConfig.getUsername().equals(user)) {
            this.commentUser = "@" + user + ":";
            this.commentId = commentId;
            playView.replaySomeone(commentUser);
        } else {
            commentUser = "@" + LoginInfoConfig.getUsername();
            this.commentId = commentId;
            playView.replaySomeone("");
        }
    }

    @Override
    public void onTagFold() {
        myLogger.i("onTagFold");
        if (!isExpand)
            playView.expandTagShow();
        else
            playView.shrinkTagShow();
        isExpand = !isExpand;
    }

    private void onFaceBookShare() {

        if (!TextUtils.isEmpty(playUrl) && playMessageBean != null) {

            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://www.bbc.com/zhongwen/simp/world"))
                    //.setImageUrl(Uri.parse(playMessageBean.getMediaThumbPath()))
                    //.setContentTitle(playMessageBean.getMediaName())
                    //.setContentDescription(playMessageBean.getMediaDesc())
                    .setQuote(playMessageBean.getMediaName())
                    .build();

            playView.showFaceBookDialog(content);
        }
    }

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {

        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        } else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {

            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i,
                    j), null);

            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 20,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                if (needRecycle)
                    bmp.recycle();
                return arrayOfByte;
            } catch (Exception e) {
                // F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void share(boolean isMoments, Bitmap bitmap) {

        myLogger.i("share");

        if (null == api)
            api = WXAPIFactory.createWXAPI(playView.getContext(), APP_ID);

        int targetScene = SendMessageToWX.Req.WXSceneSession;
        if (isMoments)
            targetScene = SendMessageToWX.Req.WXSceneTimeline;

        WXVideoObject video = new WXVideoObject();
        video.videoUrl = playUrl;
        WXMediaMessage msg = new WXMediaMessage(video);
        if (playMessageBean != null) {
            msg.title = playMessageBean.getMediaName();
            msg.description = playMessageBean.getMediaDesc();
        }
        if (bitmap != null)
            msg.thumbData = bmpToByteArray(bitmap, false);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        req.scene = targetScene;
        api.sendReq(req);
    }


    private void onFavoriteFailed(ApiException apiException) {
        if (!playView.isActive())
            return;
        if (TextUtils.isEmpty(apiException.getMessage())) {
            if (isFavorited)
                playView.actionFailedToast(R.string.toast_add_fav_failed);
            else
                playView.actionFailedToast(R.string.toast_remove_fav_failed);
            isFavorited = !isFavorited;
            updateFavorite();
        } else {
            playView.actionFailedToast(apiException.getMessage());
        }
    }

    private String getString(int reid) {
        return playView.getContext().getString(reid);
    }

    static class HDComparator implements Comparator<PlayMessageBean.PlayUrlsBean> {

        @Override
        public int compare(PlayMessageBean.PlayUrlsBean o1, PlayMessageBean.PlayUrlsBean o2) {

            PlayMessageBean.PlayUrlsBean bean1 = o1;
            PlayMessageBean.PlayUrlsBean bean2 = o2;

            if (!TextUtils.isEmpty(bean2.getClarity()) && !TextUtils.isEmpty(bean1.getClarity())) {
                try {
                    int hd2 = Integer.parseInt(bean2.getClarity().substring(0, bean2.getClarity
                            ().length() - 2));
                    int hd1 = Integer.parseInt(bean1.getClarity().substring(0, bean1.getClarity()
                            .length() - 2));
                    return hd2 > hd1 ? 1 : -1;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            return 0;
        }

    }

    private String queryPlayUrl(List<PlayMessageBean.PlayUrlsBean> urlsBean) {
        String url = "http://91.215.159.248:8080/TV0711?AuthInfo" +
                "=d1a44699df8c35dd9b312b3ebaa5c9bcb5c8424b7e5dfd799e01e85cf6b2eb08e3c74dfc4625c25c858aff331442474d7a0977bc627fac1bdea4357299e07cac2138b8f10116ad5a6a8465e6701ff748";
        url = "";

        if (urlsBean != null && urlsBean.size() > 0) {
            Collections.sort(urlsBean, new HDComparator());
        }

        url = urlsBean.get(0).getPlayUrl();
        currentHD = 0;
        playView.showResolutionText(urlsBean.get(0).getClarity());
        return url;
    }

    @Override
    public void onReceive(DanmakuBean danmakuBean) {

        if (!playView.isActive() || null == danmakuBean)
            return;


        for (int i = 0; i < danmakuBean.getData().size(); i++) {
            DanmakuBean.DataBean bean = danmakuBean.getData().get(i);
            if (bean.getUser_id().equals(dataBean.getUser_id()) && bean.getCreate_time().equals
                    (dataBean.getCreate_time())) {
                danmakuBean.getData().remove(i);
                break;
            }

        }
        playView.showDanmaku(danmakuBean, 5000);
    }

    @Override
    public void onSendSuccess() {
        if (!playView.isActive())
            return;
        playView.clearInput();
    }

    @Override
    public void onSendFailed(String error) {
        if (!playView.isActive())
            return;
        playView.actionFailedToast(error);
    }


    @Override
    public long getProgress() {
        if (!playView.isActive())
            return 0;
        return playView.getPosition();
    }
}

package com.abero.utils.play;

import android.content.Context;

import com.facebook.share.model.ShareLinkContent;
import com.motube.mydanmaku.DanmakuBean;
import com.ocean.motube.BasePresenter;
import com.ocean.motube.BaseView;
import com.ocean.motube.hj.entity.MediaComment;
import com.ocean.motube.hj.entity.PlayMessageBean;
import com.ocean.motube.hj.entity.PlaylistBean;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by abero on 2017/9/14.
 */

public interface VideoPlayContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void showContentLoading(boolean isShow);

        void showContent(String videoName, String viewsStr, String favNumStr, String likeNumStr, String date, String desc);

        void showTags(List<String> tags);

        void showTagsLessThanThree();

        void shrinkTagShow();

        void expandTagShow();

        void showUser(String avatar, String userName);

        void showError(String error);

        void showPlayLoading(boolean isShow);

        void playVideo(String url);

        void seekTo(long progress);

        void stopPlay();

        void showPlayError(int reid, String error);

        Context getContext();

        void showOverlay(boolean forceCheck);

        void showPlayEnd(boolean isShow);

        void showNoComments();

        void showComments(List<MediaComment> list);

        void toastInputComments();

        void onBackPressed();

        void showPlaylistDialog(List<PlaylistBean> list);

        void showNoPlaylist();

        void actionSuccessToast(int reid);

        void actionFailedToast(String error);

        void actionFailedToast(int reid);

        void updateFavorite(boolean isFavorited);

        void updateFollow(boolean isFollow);

        void updateLike(boolean isLike);

        void toastLikeError(String error);

        void showDanmakuStyle(int direction);

        void showDanmakuTextSize(int textSize);

        void updateCurentColor(String color);

        void hideOverlay();

        void changeDanmakuSwitch(boolean isShow);

        void showSendDanmaku(boolean isShow);

        void toast(int reid);

        void closeKeyboard();

        void prepareDanmaku();

        void showDanmaku(DanmakuBean.DataBean bean);

        void showDanmaku(DanmakuBean bean, int alarm);

        long getPosition();

        void clearInput();

        void showSocialShareDialog();

        void toPresonalShowActivity(String userId, String userName, String avatar);

        void showNotWifiDialog(String url);

        void showFaceBookDialog(ShareLinkContent content);

        void showResolutionPop(List<PlayMessageBean.PlayUrlsBean> beanList, int currentPosition);

        void showResolutionText(String clarity);

        void showSeekToTips(String progress);

        void showSendDialog();

        void replaySomeone(String someone);

    }

    interface Presenter extends BasePresenter {

        void getVideoDetail();

        void onReplay();

        void addPlaylist();

        void onPlaylistClick(int position);

        void sendComment(String string);

        void onFavorite();

        void onFollow();

        void onDanmakuStyleSelect(int direction);

        void onDanmakuTextSizeSelect(int textsize);

        void onDanmakuTextColorSelect(String color);

        void onPen();

        void sendDanmaku(String dan);

        void saveRecord();

        void onDanmakuSwitch();

        void onStart();

        void onStop();

        void onLike();

        void onAvatarClick();

        void onWechatShare(boolean isMoments);

        void onShareAction(int position);

        void resolutionAction();

        void onHDselect(int position);

        void userMobileNetworkPlay();

        void onVideoPrepared(IMediaPlayer mp);

        void onReplyComment(String commentUser, long commentId);

        void onTagFold();

    }

}

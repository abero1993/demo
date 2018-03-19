package com.abero.utils.play;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ViewStubCompat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.motube.ijkplayer.IVideoEventListener;
import com.motube.ijkplayer.IjkVideoView;
import com.motube.mydanmaku.DanmakuBean;
import com.motube.mydanmaku.RefitDanmakuView;
import com.ocean.motube.BaseApplication;
import com.ocean.motube.R;
import com.ocean.motube.dialog.FragmentDialog;
import com.ocean.motube.hj.entity.MediaComment;
import com.ocean.motube.hj.entity.PlayMessageBean;
import com.ocean.motube.hj.entity.PlaylistBean;
import com.ocean.motube.util.AndroidDevices;
import com.ocean.motube.util.AndroidUtil;
import com.ocean.motube.util.MyLogger;
import com.ocean.motube.util.NavigationBarUtils;
import com.ocean.motube.util.UIUtils;
import com.ocean.motube.view.presonalshow.InExhibitionBean;
import com.ocean.motube.view.presonalshow.PresonalShowActivity;
import com.ocean.motube.widget.GridSpaceItemDecoration;
import com.ocean.motube.widget.NoDataView;
import com.ocean.motube.widget.favlike.FavView;
import com.ocean.motube.widget.favlike.ThumbsUpView;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by abero on 2017/9/14.
 */

public class VideoPlayFragment extends Fragment implements VideoPlayContract.View, View.OnClickListener,
        MyTouchListener {

    private MyLogger logger = MyLogger.getAberoLog();
    private VideoPlayContract.Presenter presenter;
    private ProgressBar contentProgressBar;
    private ProgressBar loadingProgressBar;
    private View contentView;
    private TextView videoNameTextView;
    private TextView favNumTextView;
    private TextView likeNumTextView;
    private TextView viewsTextView;
    private TextView dateTextView;
    private TextView descTextView;
    private View detailFoldView;
    private TextView userTextView;
    private ImageView avatarImageView;
    private ImageView introImageView;
    private Button followButton;
    private NoDataView contentNoDataView;
    private NoDataView playNodataView;
    private IjkVideoView ijkVideoView;
    private ImageView tagfoldImageView;
    private TagsAdapter tagsAdapter;
    private RecyclerView tagsRecyclerView;
    private View rootView;
    private ValueAnimator recyclerViewAnim;
    private boolean isIntro = false;

    //play end
    private ViewStubCompat completeView;
    private ImageView replayImageView;
    private ImageView nextImageView;

    //overlay
    private ActionBar actionBar;
    private ViewGroup actionBarView;
    private ImageView backImageView;
    private ImageView shareImageView;
    private ImageView addImageView;
    private ViewGroup hubView;
    private ImageView playImageView;
    private TextView positionTextView;
    private SeekBar seekBar;
    private TextView lengthTextView;
    private ImageView expendImageView;
    private ImageView switchImageView;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;

    //handler
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = -1;
    private static final int SHOW_OVERLAY = 1;
    private static final int SHOW_PROGRESS = 3;
    private static final int FADE_OUT = 4;
    private static final int SHOW_DANMAKULAY = 5;
    private static final int HIDE_VOL_BRIGHTNESS_INFO = 6;
    private static final int HIDE_FOR_RE_INFO = 7;
    private static final int SHOW_VIDEO_LOADING = 8;
    private static final int HIDE_VIDEO_LOADING = 9;
    private int overlayTimeout = 0;
    private boolean showing = false;
    private boolean dragging = false;

    //Volume
    private AudioManager audioManager;
    private int audioMax;
    private float vol;

    // Brightness
    private boolean isFirstBrightnessGesture = true;
    private float restoreAutoBrightness = -1f;

    //vol brighness info
    private View vbView;
    private ImageView vbImageView;
    private SeekBar vbSeekbar;

    //facebook wecaht
    private CallbackManager callbackManager;
    private ShareDialog facebookShareDialog;

    //hd
    private TextView resolutionText;
    PopupWindow popupWindow;

    private enum VBtype {
        VOL, BRIGHTNESS
    }

    // seek
    private View forwardRewindView;
    private ImageView forwardRewindImageView;
    private TextView forwardRewindText;

    private enum FRtype {
        FORWARD, REWIND
    }

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_MOVE = 3;
    private static final int TOUCH_SEEK = 4;
    private int touchAction = TOUCH_NONE;
    private GestureDetectorCompat detector = null;
    private float initTouchY, touchY = -1f, touchX = -1f;
    DisplayMetrics screen = new DisplayMetrics();
    private int surfaceYDisplayRange, surfaceXDisplayRange;
    private static final int TOUCH_FLAG_AUDIO_VOLUME = 1;
    private static final int TOUCH_FLAG_BRIGHTNESS = 1 << 1;
    private static final int TOUCH_FLAG_SEEK = 1 << 2;

    // small full screen
    private FrameLayout playerRootView;
    private int playerNormalHeight = 0;
    private ImageView writeImageView;

    //danmaku
    private final int ROLL_STYLE = 3;
    private final int TOP_STYLE = 1;
    private final int BOT_STYLE = 2;
    private final int BIG_T = 36;
    private final int NORMAL_T = 24;
    private final int SMALL_T = 18;

    private RefitDanmakuView danmakuView;
    private ViewGroup sendDanmakuView;
    private ImageView rollStyleImageView;
    private ImageView topStyleImageView;
    private ImageView botStyleImageView;
    private ImageView colorImageView;
    private ImageView bitTImageView;
    private ImageView normalTImageView;
    private ImageView smallTImageView;
    private ImageView colorImage;
    private ImageView colorBackImgeView;
    private RecyclerView colorListView;
    private View danmakuSettingBView;
    private View danmakuSettingAView;
    private EditText danmakuEditText;
    private ImageView sendImageView;
    private TextView commentNumText;
    private RecyclerView recyclerView;
    private CommmentAdapter commmentAdapter;
    private ColorAdapter colorAdapter;
    //comment
    private ViewGroup sendCommentView;
    private TextView commenText;
    private Dialog commentDialog;
    private EditText commentEditView;
    private FavView favoriteImage;
    private ThumbsUpView likeImage;
    //social share
    private Dialog shareDialog;
    private GridView shareGridView;
    private TextView dimShareText;


    public static VideoPlayFragment newInstance() {
        VideoPlayFragment fragment = new VideoPlayFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {

        logger.i("onCreateView");

        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_play_video, container, false);
        rootView = view;
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onStart() {
        logger.i("onStart");
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        logger.i("onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        presenter.start();
    }

    private void initData() {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    private void initView(View view) {
        contentProgressBar = (ProgressBar) view.findViewById(R.id.video_progress);
        contentView = view.findViewById(R.id.video_content);
        videoNameTextView = (TextView) view.findViewById(R.id.video_name);
        viewsTextView = (TextView) view.findViewById(R.id.video_play_count_text);
        favNumTextView = (TextView) view.findViewById(R.id.video_play_fav_text);
        likeNumTextView = (TextView) view.findViewById(R.id.video_play_like_text);
        dateTextView = (TextView) view.findViewById(R.id.video_upload_time);
        descTextView = (TextView) view.findViewById(R.id.video_desc);
        detailFoldView = view.findViewById(R.id.video_detail_fold);
        contentNoDataView = (NoDataView) view.findViewById(R.id.video_nodata_view);
        ijkVideoView = (IjkVideoView) view.findViewById(R.id.video_player);
        playNodataView = (NoDataView) view.findViewById(R.id.player_error);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.player_progressbar);
        avatarImageView = (ImageView) view.findViewById(R.id.video_upload_av);
        userTextView = (TextView) view.findViewById(R.id.video_upload_user);
        introImageView = (ImageView) view.findViewById(R.id.video_intro_image);
        followButton = (Button) view.findViewById(R.id.video_btn_follow);
        tagfoldImageView = (ImageView) view.findViewById(R.id.video_tag_fold);
        tagsRecyclerView = (RecyclerView) view.findViewById(R.id.video_detail_tags_recycler);
        introImageView.setOnClickListener(this);
        followButton.setOnClickListener(this);
        avatarImageView.setOnClickListener(this);
        userTextView.setOnClickListener(this);
        tagfoldImageView.setOnClickListener(this);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.player_overlay_title);
        actionBarView = (ViewGroup) actionBar.getCustomView();
        backImageView = (ImageView) actionBarView.findViewById(R.id.video_overlay_back);
        shareImageView = (ImageView) actionBarView.findViewById(R.id.video_overlay_share);
        addImageView = (ImageView) actionBarView.findViewById(R.id.video_overlay_add);
        switchImageView = (ImageView) actionBarView.findViewById(R.id.video_overlay_danmaku);
        backImageView.setOnClickListener(this);
        addImageView.setOnClickListener(this);
        shareImageView.setOnClickListener(this);
        switchImageView.setOnClickListener(this);

        hubView = (ViewGroup) view.findViewById(R.id.player_stub_hub);
        playImageView = (ImageView) view.findViewById(R.id.player_hub_play);
        positionTextView = (TextView) view.findViewById(R.id.player_hub_position);
        seekBar = (SeekBar) view.findViewById(R.id.player_hub_seekbar);
        lengthTextView = (TextView) view.findViewById(R.id.player_hub_length);
        expendImageView = (ImageView) view.findViewById(R.id.player_hub_expand);
        playImageView.setOnClickListener(this);
        expendImageView.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        playerRootView = (FrameLayout) view.findViewById(R.id.video_player_root);
        writeImageView = (ImageView) view.findViewById(R.id.player_pen);
        writeImageView.setOnClickListener(this);


        danmakuView = (RefitDanmakuView) view.findViewById(R.id.video_danmaku_view);
        sendDanmakuView = (ViewGroup) view.findViewById(R.id.danmaku_send_root);
        rollStyleImageView = (ImageView) view.findViewById(R.id.danmaku_style_roll);
        topStyleImageView = (ImageView) view.findViewById(R.id.danmaku_style_top);
        botStyleImageView = (ImageView) view.findViewById(R.id.danmaku_style_bottom);
        bitTImageView = (ImageView) view.findViewById(R.id.danmaku_t_big);
        normalTImageView = (ImageView) view.findViewById(R.id.danmaku_t_normal);
        smallTImageView = (ImageView) view.findViewById(R.id.danmaku_t_small);
        danmakuEditText = (EditText) view.findViewById(R.id.danmaku_edit);
        sendImageView = (ImageView) view.findViewById(R.id.danmaku_send);
        commentNumText = (TextView) view.findViewById(R.id.video_comment_num);
        recyclerView = (RecyclerView) view.findViewById(R.id.video_comment_recycler);
        favoriteImage = (FavView) view.findViewById(R.id.player_comment_fav);
        likeImage = (ThumbsUpView) view.findViewById(R.id.player_comment_like);
        colorImage = (ImageView) view.findViewById(R.id.danmaku_color);
        colorBackImgeView = (ImageView) view.findViewById(R.id.danmaku_color_back);
        colorListView = (RecyclerView) view.findViewById(R.id.danmaku_color_list);
        danmakuSettingAView = view.findViewById(R.id.danmaku_layout_a);
        danmakuSettingBView = view.findViewById(R.id.danmaku_layout_b);
        resolutionText = (TextView) view.findViewById(R.id.player_hub_hd);
        resolutionText.setOnClickListener(this);
        favoriteImage.setOnClickListener(this);
        likeImage.setOnClickListener(this);
        sendImageView.setOnClickListener(this);
        rollStyleImageView.setOnClickListener(this);
        topStyleImageView.setOnClickListener(this);
        botStyleImageView.setOnClickListener(this);
        bitTImageView.setOnClickListener(this);
        normalTImageView.setOnClickListener(this);
        smallTImageView.setOnClickListener(this);
        sendDanmakuView.setOnClickListener(this);
        colorImage.setOnClickListener(this);
        colorBackImgeView.setOnClickListener(this);
        colorAdapter = new ColorAdapter(getContext());
        colorListView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayout.HORIZONTAL, false));
        colorListView.setAdapter(colorAdapter);
        colorAdapter.notifyDataSetChanged();
        colorAdapter.setOnItemListener(new ColorAdapter.OnItemClickListener() {
            @Override
            public void onClick(String color) {
                presenter.onDanmakuTextColorSelect(color);
            }
        });

        danmakuEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                presenter.sendDanmaku(danmakuEditText.getText().toString());
                return true;
            }
        });

        sendCommentView = (ViewGroup) view.findViewById(R.id.player_comment_root);
        commenText = (TextView) view.findViewById(R.id.player_comment_edit);
        commenText.setOnClickListener(this);


        contentNoDataView.addOnTextClickListener(new NoDataView.TextClickListener() {
            @Override
            public void OnTextClickListener(View view) {
                presenter.getVideoDetail();
            }
        });

        playNodataView.addOnTextClickListener(new NoDataView.TextClickListener() {
            @Override
            public void OnTextClickListener(View view) {
                presenter.onReplay();
            }
        });

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(screen);
        audioManager = (AudioManager) BaseApplication.getContext().getSystemService(AUDIO_SERVICE);
        audioMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        surfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        surfaceXDisplayRange = Math.max(screen.widthPixels, screen.heightPixels);


        dimStatusBar(false);
        actionBar.hide();
    }

    private void cleanUI() {
        if (detector != null) {
            detector.setOnDoubleTapListener(null);
            detector = null;
        }
    }

    private void initPlayendStub() {
        if (rootView != null) {
            ViewStubCompat vs = (ViewStubCompat) rootView.findViewById(R.id.player_end_stub);
            if (vs != null) {
                vs.inflate();
                completeView = vs;
                replayImageView = (ImageView) rootView.findViewById(R.id.player_end_replay);
                nextImageView = (ImageView) rootView.findViewById(R.id.player_end_next);
                replayImageView.setOnClickListener(this);
                nextImageView.setOnClickListener(this);
            }
        }
    }


    @Override
    public void setPresenter(VideoPlayContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showContentLoading(boolean isShow) {
        contentNoDataView.setVisibility(View.INVISIBLE);
        if (isShow)
            contentProgressBar.setVisibility(View.VISIBLE);
        else
            contentProgressBar.setVisibility(View.GONE);

    }

    public void showPlayLoading(boolean isShow) {
        playNodataView.setVisibility(View.INVISIBLE);
        if (isShow)
            loadingProgressBar.setVisibility(View.VISIBLE);
        else
            loadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showContent(String videoName, String viewsStr, String favNumStr, String likeNumStr, String date,
                            String desc) {
        contentView.setVisibility(View.VISIBLE);
        sendCommentView.setVisibility(View.VISIBLE);
        contentNoDataView.setVisibility(View.GONE);
        videoNameTextView.setText(videoName);
        viewsTextView.setText(viewsStr);
        favNumTextView.setText(favNumStr);
        likeNumTextView.setText(likeNumStr);
        dateTextView.setText(date);
        descTextView.setText(desc);
    }

    @Override
    public void showTags(List<String> tags) {
        tagsAdapter = new TagsAdapter(tags);
        tagsRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        tagsRecyclerView.addItemDecoration(new GridSpaceItemDecoration(3, getResources().getDimensionPixelSize(R
                .dimen.player_tag_time_line_space)));
        tagsRecyclerView.setVisibility(View.VISIBLE);
        tagsRecyclerView.setAdapter(tagsAdapter);

    }

    @Override
    public void showTagsLessThanThree() {
        tagfoldImageView.setVisibility(View.GONE);
    }

    private int getFirtItemHeight() {

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.player_tag_item_height);
        int height = itemHeight;
        logger.i("fist tag height=" + height);
        return height;
    }

    private int getTagRecyclerHeight() {

        int itemHeight = getResources().getDimensionPixelSize(R.dimen.player_tag_item_height);
        int space = getResources().getDimensionPixelSize(R.dimen.player_tag_time_line_space);
        int height = itemHeight;
        int lines = tagsAdapter.getItemCount() / 3 + (tagsAdapter.getItemCount() % 3 == 0 ? 0 : 1);
        logger.i("lines=" + lines);
        lines--;
        if (lines > 0)
            height = (itemHeight + space) * lines + itemHeight;
        else
            height = itemHeight;
        logger.i("tag heigth=" + height);
        return height;
    }

    @Override
    public void shrinkTagShow() {
        logger.i("shrinkTagShow");
        tagfoldImageView.setVisibility(View.VISIBLE);
        tagfoldImageView.animate().rotation(0);
        if (recyclerViewAnim != null && recyclerViewAnim.isRunning())
            return;
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tagsRecyclerView.getLayoutParams();
        recyclerViewAnim = ValueAnimator.ofInt(getTagRecyclerHeight(), getFirtItemHeight());
        recyclerViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = (int) animation.getAnimatedValue();
                params.height = h;
                tagsRecyclerView.setLayoutParams(params);
                tagsRecyclerView.requestLayout();
            }
        });

        recyclerViewAnim.setDuration(500);
        recyclerViewAnim.start();
    }

    @Override
    public void expandTagShow() {
        logger.i("expandTagShow");
        tagfoldImageView.animate().rotation(180);
        if (recyclerViewAnim != null && recyclerViewAnim.isRunning())
            return;
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tagsRecyclerView.getLayoutParams();
        recyclerViewAnim = ValueAnimator.ofInt(getFirtItemHeight(), getTagRecyclerHeight());
        recyclerViewAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = (int) animation.getAnimatedValue();
                params.height = h;
                tagsRecyclerView.setLayoutParams(params);
                tagsRecyclerView.requestLayout();
            }
        });

        recyclerViewAnim.setDuration(500);
        recyclerViewAnim.start();
    }


    @Override
    public void showUser(String avatar, String userName) {
        Glide.with(getActivity())
                .load(avatar)
                //.dontAnimate()
                .asBitmap()
                //.crossFade()//渐显动画
                .placeholder(R.mipmap.head_no_mine)
                .error(R.mipmap.head_no_mine)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(new BitmapImageViewTarget(avatarImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create
                                (getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        circularBitmapDrawable.setAntiAlias(true);
                        avatarImageView.setImageDrawable(circularBitmapDrawable);
                    }
                });

        userTextView.setText(userName);

    }

    @Override
    public void showError(String error) {
        logger.i("showError");
        contentNoDataView.setVisibility(View.VISIBLE);
        contentNoDataView.setText(error, ContextCompat.getColor(getContext(), R.color.video_detail_text_color));
    }

    private void updatePlayPause() {
        if (ijkVideoView.isPlaying())
            playImageView.setImageResource(R.drawable.icon_play);
        else
            playImageView.setImageResource(R.drawable.icon_zanting);
    }

    private void updateDuration(int length) {
        lengthTextView.setText(stringForTime(length));
    }

    private void updatePosition(int position) {
        positionTextView.setText(stringForTime(position));
    }

    private int setOverlayProgress() {
        int time = ijkVideoView.getCurrentPosition();
        int length = ijkVideoView.getDuration();
        if (length >= 0)
            updateDuration(length);
        updatePosition(time);
        seekBar.setMax(length);
        seekBar.setProgress(time);
        return time;
    }

    private void showVolBrightnessInfo(VBtype type, int progress, int lenght) {

        if (isPortrait())
            return;

        ViewStubCompat vs = (ViewStubCompat) rootView.findViewById(R.id.player_vol_brightness_stub);
        if (vs != null) {
            vs.inflate();
            vbView = rootView.findViewById(R.id.player_vol_brightness_root);
            vbImageView = (ImageView) rootView.findViewById(R.id.player_vol_brightness_image);
            vbSeekbar = (SeekBar) rootView.findViewById(R.id.player_vod_brightness_seek);
        }

        if (VBtype.VOL == type && 0 == progress)
            vbImageView.setImageResource(R.drawable.icon_voice_no);
        else if (VBtype.VOL == type && progress > 0)
            vbImageView.setImageResource(R.drawable.icon_voice);
        else if (VBtype.BRIGHTNESS == type && 0 == progress)
            vbImageView.setImageResource(R.drawable.icon_light_no);
        else
            vbImageView.setImageResource(R.drawable.icon_light);

        vbSeekbar.setMax(lenght);
        vbSeekbar.setProgress(progress);
        vbView.setVisibility(View.VISIBLE);
        hanler.removeMessages(HIDE_VOL_BRIGHTNESS_INFO);
        hanler.sendEmptyMessageDelayed(HIDE_VOL_BRIGHTNESS_INFO, 1000);
    }

    private void showForwardRewindInfo(FRtype type, String info, boolean seek) {

        if (isPortrait())
            return;

        if (null == forwardRewindView) {
            ViewStubCompat vs = (ViewStubCompat) rootView.findViewById(R.id.player_forward_rewind_stub);
            if (vs != null) {
                vs.inflate();
                forwardRewindView = rootView.findViewById(R.id.player_forward_rewind_root);
                forwardRewindImageView = (ImageView) rootView.findViewById(R.id.player_forward_rewind_image);
                forwardRewindText = (TextView) rootView.findViewById(R.id.player_forward_rewind_text);
            }
        } else {
            if (FRtype.FORWARD == type)
                forwardRewindImageView.setImageResource(R.drawable.play_kuaijin);
            else
                forwardRewindImageView.setImageResource(R.drawable.play_kuaitui);
            forwardRewindText.setText(info);
            forwardRewindView.setVisibility(View.VISIBLE);

            if (seek) {
                hanler.removeMessages(HIDE_FOR_RE_INFO);
                hanler.sendEmptyMessage(HIDE_FOR_RE_INFO);
            }
        }
    }

    private boolean canShowProgress() {
        return !dragging && showing;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private Handler hanler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (null == getActivity())
                return true;

            switch (msg.what) {
                case SHOW_OVERLAY:
                    showOverlay();
                    break;
                case SHOW_PROGRESS:
                    int pos = setOverlayProgress();
                    if (canShowProgress()) {
                        msg = hanler.obtainMessage(SHOW_PROGRESS);
                        hanler.sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case FADE_OUT:
                    logger.i("FADE_OUT");
                    hideOverlay();
                    break;

                case SHOW_DANMAKULAY:
                    logger.i("SHOW_DANMAKULAY");
                    //showKeyboard();
                    break;

                case HIDE_VOL_BRIGHTNESS_INFO:
                    if (vbView != null)
                        vbView.setVisibility(View.INVISIBLE);
                    break;

                case HIDE_FOR_RE_INFO:
                    if (forwardRewindView != null)
                        forwardRewindView.setVisibility(View.INVISIBLE);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    break;

                case SHOW_VIDEO_LOADING:
                    showPlayLoading(true);
                    break;

                case HIDE_VIDEO_LOADING:
                    showPlayLoading(false);
                    break;

                default:
                    break;
            }
            return true;
        }
    });

    @Override
    public void playVideo(String url) {
       // url="http://123.249.76.181:8082/TV051?AuthInfo" +
        //        "=3998bc7414c29ba64f263dcacb807b0fa217523c71c524834758033aaa78e2cee3c74dfc4625c25c6de2151ca282f5de82a379e51e9b5432556843f232c084b9a1f273f8dec0c6c43c9efef60597ea69";
        ijkVideoView.setVideoPath(url);
        ijkVideoView.start();
        ijkVideoView.setVideoEventListener(videoEventListener);
    }

    @Override
    public void seekTo(long progress) {
        ijkVideoView.seekTo((int) progress);
    }

    private IVideoEventListener videoEventListener = new IVideoEventListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            logger.i("onPrepared");
            //showPlayEnd(false);
            showPlayLoading(false);
            showPlayEnd(false);
            hanler.sendEmptyMessageDelayed(SHOW_OVERLAY, 200);
            if (presenter != null)
                presenter.onVideoPrepared(mp);
        }

        @Override
        public void onCompletion(IMediaPlayer mp) {
            logger.i("onCompletion");
            showSendDanmaku(false);
            showPlayEnd(true);
        }

        @Override
        public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
            logger.i("onInfo");
            return false;
        }

        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            logger.i("onError");
            showPlayError(R.drawable.play_shit, getString(R.string.text_play_error));
            return false;
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            logger.i("onBufferingUpdate=" + percent);
        }

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            logger.i("onSeekComplete");
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }


        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
            logger.i("onTimedText=" + text);
        }

        @Override
        public void onBufferingStart(int arg) {
            logger.i("onBufferingStart");
            if (isActive())
                hanler.sendEmptyMessage(SHOW_VIDEO_LOADING);
        }

        @Override
        public void onBufferingEnd(int arg) {
            logger.i("onBufferingEnd");
            if (isActive())
                hanler.sendEmptyMessage(HIDE_VIDEO_LOADING);
        }
    };

    @Override
    public void stopPlay() {
        ijkVideoView.stopPlayback();
    }

    @Override
    public void showPlayError(int reid, String error) {
        loadingProgressBar.setVisibility(View.INVISIBLE);
        playNodataView.setVisibility(View.VISIBLE);
        playNodataView.setImageResource(reid);
        playNodataView.setText(error, Color.WHITE);
    }

    private void showOverlay() {
        showOverlay(false);
    }

    @Override
    public void showOverlay(boolean forceCheck) {
        if (forceCheck)
            overlayTimeout = 0;
        showOverlayTimeout(0);
    }

    @Override
    public void showPlayEnd(boolean isShow) {
        if (null == completeView)
            initPlayendStub();
        if (isShow)
            completeView.setVisibility(View.VISIBLE);
        else
            completeView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoComments() {
        commentNumText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        initCommentRecycler(null);
    }

    @Override
    public void showComments(List<MediaComment> list) {
        commentNumText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        commentNumText.setText(getString(R.string.text_video_comments, list.size()));
        initCommentRecycler(list);

    }

    @Override
    public void toastInputComments() {
        Toast.makeText(this.getContext(), R.string.toast_send_empty_comment, Toast.LENGTH_SHORT).show();
    }

    private void initCommentRecycler(List<MediaComment> list) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //recyclerView.addItemDecoration(new SimplePaddingDecoration(getContext(), UIUtils.dip2px(8), R.color.white));
        commmentAdapter = new CommmentAdapter(getContext(), list);
        commmentAdapter.setOnReplyListener(onReplyListener);
        recyclerView.setAdapter(commmentAdapter);
        commmentAdapter.notifyDataSetChanged();
    }

    private CommmentAdapter.OnReplyListener onReplyListener = new CommmentAdapter.OnReplyListener() {
        @Override
        public void onReply(String userName, long commentId) {
            presenter.onReplyComment(userName, commentId);
        }
    };

    @Override
    public void onBackPressed() {
        if (isPortrait()) {
            getActivity().finish();
        } else {
            onExpendScreen();
        }
    }

    @Override
    public void showPlaylistDialog(List<PlaylistBean> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.Theme_AppCompat_Light_Dialog);
        BaseAdapter baseAdapter = new PlaylistAdapter(getContext(), list);
        builder.setAdapter(baseAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logger.i("onClick=" + which);
                presenter.onPlaylistClick(which);
            }
        });
        Dialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void showNoPlaylist() {
        Toast.makeText(getContext(), R.string.toast_no_playlist, Toast.LENGTH_LONG).show();
    }

    @Override
    public void actionSuccessToast(int reid) {
        Toast.makeText(getContext(), reid, Toast.LENGTH_LONG).show();
    }

    @Override
    public void actionFailedToast(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void actionFailedToast(int reid) {
        actionFailedToast(getString(reid));
    }

    @Override
    public void updateFavorite(final boolean isFavorited) {

        if (isFavorited)
            favoriteImage.fav();
        else
            favoriteImage.unfav();

    }

    @Override
    public void updateFollow(boolean isFollow) {
        if (isFollow) {
            followButton.setBackgroundResource(R.drawable.shape_followed_btn_bg);
            followButton.setTextColor(0xffafafaf);
            followButton.setText(R.string.text_followed);
        } else {
            followButton.setBackgroundResource(R.drawable.shape_not_follow_btn_bg);
            followButton.setTextColor(0xffffffff);
            followButton.setText(R.string.text_follow);
        }
    }

    @Override
    public void updateLike(boolean isLike) {
        if (isLike)
            likeImage.like();
        else
            likeImage.unlike();
    }

    @Override
    public void toastLikeError(String error) {
        Toast.makeText(this.getContext(), getResources().getString(R.string.toast_like_error, error), Toast
                .LENGTH_LONG).show();
    }

    @Override
    public void showDanmakuStyle(int direction) {

        rollStyleImageView.setImageResource(R.drawable.position_gundong_normal);
        topStyleImageView.setImageResource(R.drawable.position_up_normal);
        botStyleImageView.setImageResource(R.drawable.position_down_normal);
        if (ROLL_STYLE == direction) {
            rollStyleImageView.setImageResource(R.drawable.position_gundong_highlight);
        } else if (TOP_STYLE == direction) {
            topStyleImageView.setImageResource(R.drawable.position_up_highlight);
        } else if (BOT_STYLE == direction) {
            botStyleImageView.setImageResource(R.drawable.position_down_highlight);
        } else {
            rollStyleImageView.setImageResource(R.drawable.position_gundong_highlight);
        }
    }

    @Override
    public void showDanmakuTextSize(int textSize) {

        bitTImageView.setImageResource(R.drawable.text_big_normal);
        normalTImageView.setImageResource(R.drawable.text_zhong_normal);
        smallTImageView.setImageResource(R.drawable.text_small_normal);
        if (BIG_T == textSize) {
            bitTImageView.setImageResource(R.drawable.text_big_highlight);
        } else if (NORMAL_T == textSize) {
            normalTImageView.setImageResource(R.drawable.text_zhong_highlight);
        } else if (SMALL_T == textSize) {
            smallTImageView.setImageResource(R.drawable.text_small_highlight);
        } else {
            normalTImageView.setImageResource(R.drawable.text_zhong_highlight);
        }
    }

    @Override
    public void updateCurentColor(String color) {
        colorAdapter.updateCurrentPosition(color);
    }

    private void showOverlayTimeout(int timeout) {
        if (timeout != 0)
            overlayTimeout = timeout;
        if (overlayTimeout == 0)
            overlayTimeout = ijkVideoView.isPlaying() ? OVERLAY_TIMEOUT : OVERLAY_INFINITE;
        hanler.sendEmptyMessage(SHOW_PROGRESS);
        updatePlayPause();
        if (!showing) {
            showing = true;
            hubView.setVisibility(View.VISIBLE);
            if (!isPortrait()) {
                writeImageView.setVisibility(View.VISIBLE);
                sendCommentView.setVisibility(View.INVISIBLE);
            }
            dimStatusBar(false);
            sendDanmakuView.setVisibility(View.INVISIBLE);
        }

        hanler.removeMessages(FADE_OUT);
        logger.i("timeout=" + overlayTimeout);
        if (overlayTimeout != OVERLAY_INFINITE)
            hanler.sendMessageDelayed(hanler.obtainMessage(FADE_OUT), overlayTimeout);

    }

    public void hideOverlay() {
        logger.i("hideOverlay");
        if (showing) {
            hanler.removeMessages(FADE_OUT);
            hanler.removeMessages(SHOW_PROGRESS);

            if (!isPortrait())
                dimStatusBar(true);
            hubView.setVisibility(View.INVISIBLE);
            writeImageView.setVisibility(View.INVISIBLE);
            sendDanmakuView.setVisibility(View.INVISIBLE);
            if (vbView != null)
                vbView.setVisibility(View.INVISIBLE);
            showing = false;

            if (popupWindow != null)
                popupWindow.dismiss();
        }


    }

    @Override
    public void changeDanmakuSwitch(boolean isShow) {
        if (!isShow) {
            switchImageView.setImageResource(R.drawable.danmu_off);
            writeImageView.setVisibility(View.INVISIBLE);
            danmakuView.hide();
        } else {
            switchImageView.setImageResource(R.drawable.danmu_on);
            writeImageView.setVisibility(View.VISIBLE);
            danmakuView.show();
        }
    }

    public void showSendDanmaku(boolean isShow) {
        if (isShow) {
            sendDanmakuView.setVisibility(View.VISIBLE);
            //hanler.sendEmptyMessageDelayed(SHOW_DANMAKULAY, 100);
        } else {
            sendDanmakuView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void toast(int reid) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!ijkVideoView.isInPlaybackState())
            return false;

        if (null == detector) {
            detector = new GestureDetectorCompat(getContext(), gestureListener);
            detector.setOnDoubleTapListener(gestureListener);
        }

        if (detector != null && detector.onTouchEvent(event))
            return true;

        float x_changed, y_changed;
        if (touchX != -1f && touchY != -1f) {
            y_changed = event.getRawY() - touchY;
            x_changed = event.getRawX() - touchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

        // coef is the gradient's move to determine a neutral zone
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);
        float delta_y = Math.max(1f, (Math.abs(initTouchY - event.getRawY()) / screen.xdpi + 0.5f) * 2f);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // Audio
                touchY = initTouchY = event.getRawY();
                vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchAction = TOUCH_NONE;
                // Seek
                touchX = event.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:
                // Mouse events for the core
                // No volume/brightness action if coef < 2 or a secondary display is connected
                //TODO : Volume action when a secondary display is connected
                //logger.i("ACTION_MOVE=" + coef);

                if (touchAction != TOUCH_SEEK && coef > 2) {
                    if (Math.abs(y_changed / surfaceYDisplayRange) < 0.05)
                        return false;
                    touchY = event.getRawY();
                    touchX = event.getRawX();
                    // Volume (Up or Down - Right side)
                    if ((int) touchX > (4 * screen.widthPixels /
                            7f)) {
                        doVolumeTouch(y_changed);
                        hideOverlay();
                    }
                    // Brightness (Up or Down - Left side)
                    if ((int) touchX < (3 * screen.widthPixels /
                            7f)) {
                        doBrightnessTouch(y_changed);
                        hideOverlay();
                    }
                } else {
                    // Seek (Right or Left move)
                    doSeekTouch(Math.round(delta_y), xgesturesize, false);
                }

                break;

            case MotionEvent.ACTION_UP:
                // Seek
                if (touchAction == TOUCH_SEEK)
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                touchX = -1f;
                touchY = -1f;
                break;
        }
        return touchAction != TOUCH_NONE;
    }

    private void doVolumeTouch(float y_changed) {
        logger.i("doVolumeTouch");
        if (isPortrait())
            return;

        if (touchAction != TOUCH_NONE && touchAction != TOUCH_VOLUME)
            return;
        float delta = -((y_changed / (float) screen.heightPixels) * audioMax);
        vol += delta;
        int vol = (int) Math.min(Math.max(this.vol, 0), audioMax);
        if (delta != 0f) {
            setAudioVolume(vol);
        }
    }

    private void setAudioVolume(int vol) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);

        touchAction = TOUCH_VOLUME;
        //vol = vol * 100 / audioMax;
        //showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
        showVolBrightnessInfo(VBtype.VOL, vol, audioMax);
    }

    private void initBrightnessTouch() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(getContext())) {
                    Permissions.checkWriteSettingsPermission(getActivity(), Permissions.PERMISSION_SYSTEM_BRIGHTNESS);
                    return;
                }
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                restoreAutoBrightness = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        getActivity().getWindow().setAttributes(lp);
        isFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float y_changed) {
        logger.i("doBrightnessTouch");
        if (isPortrait())
            return;

        if (touchAction != TOUCH_NONE && touchAction != TOUCH_BRIGHTNESS)
            return;
        if (isFirstBrightnessGesture) initBrightnessTouch();
        touchAction = TOUCH_BRIGHTNESS;

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / surfaceYDisplayRange;

        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);
        //showInfoWithVerticalBar(getString(R.string.brightness) + "\n" + (int) brightness + '%', 1000, (int)
        // brightness);
        showVolBrightnessInfo(VBtype.BRIGHTNESS, (int) brightness, 100);
    }

    private void setWindowBrightness(float brightness) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        getActivity().getWindow().setAttributes(lp);
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        logger.i("doSeekTouch=" + seek);
        if (coef == 0)
            coef = 1;
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1 || !ijkVideoView.canSeekForward())
            return;

        if (touchAction != TOUCH_NONE && touchAction != TOUCH_SEEK)
            return;
        touchAction = TOUCH_SEEK;

        long length = ijkVideoView.getDuration();
        long time = ijkVideoView.getCurrentPosition();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length))
            jump = (int) (length - time);
        if ((jump < 0) && ((time + jump) < 0))
            jump = (int) -time;

        //Jump !
        FRtype type;
        if (jump > 0)
            type = FRtype.FORWARD;
        else
            type = FRtype.REWIND;
        if (seek && length > 0)
            ijkVideoView.seekTo((int) (time + jump));

       /* if (length > 0)
            //Show the jump's size
            showInfo(String.format("%s%s (%s)%s",
                    jump >= 0 ? "+" : "",
                    Tools.millisToString(jump),
                    Tools.millisToString(time + jump),
                    coef > 1 ? String.format(" x%.1g", 1.0/coef) : ""), 50);
        else
            showInfo(R.string.unseekable_stream, 1000);*/
        if (length > 0)
            showForwardRewindInfo(type, stringForTime((int) (time + jump)) + "/" + stringForTime((int) length), seek);

        if (0 == length && seek)
            Toast.makeText(getContext(), R.string.unseekable_stream, Toast.LENGTH_SHORT).show();
    }

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            logger.i("onSingleTapUp");
            hanler.sendEmptyMessageDelayed(showing ? FADE_OUT : SHOW_OVERLAY, 200);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            logger.i("onDoubleTap");
            return super.onDoubleTap(e);
        }
    };


    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updatePosition(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            logger.i("onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            logger.i("onStopTrackingTouch");
            int progress = seekBar.getProgress();
            ijkVideoView.seekTo(progress);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.video_overlay_back:
                onBackPressed();
                break;
            case R.id.video_overlay_add:
                presenter.addPlaylist();
                break;

            case R.id.video_overlay_share:
                showSocialShareDialog();
                break;

            case R.id.video_overlay_danmaku:
                presenter.onDanmakuSwitch();
                break;

            case R.id.player_hub_play:
                if (ijkVideoView.isPlaying()) {
                    ijkVideoView.pause();
                    playImageView.setImageResource(R.drawable.icon_zanting);
                } else {
                    ijkVideoView.start();
                    playImageView.setImageResource(R.drawable.icon_play);
                }
                //showOverlay(true);
                break;

            case R.id.player_hub_hd:
                presenter.resolutionAction();
                break;

            case R.id.player_hub_expand:
                onExpendScreen();
                break;

            case R.id.player_end_replay:
                presenter.onReplay();
                break;

            case R.id.player_end_next:

                break;

            case R.id.player_pen:
                presenter.onPen();
                break;

            case R.id.danmaku_send:
                presenter.sendDanmaku(danmakuEditText.getText().toString());
                break;
            case R.id.danmaku_style_roll:
                presenter.onDanmakuStyleSelect(ROLL_STYLE);
                break;
            case R.id.danmaku_style_top:
                presenter.onDanmakuStyleSelect(TOP_STYLE);
                break;
            case R.id.danmaku_style_bottom:
                presenter.onDanmakuStyleSelect(BOT_STYLE);
                break;
            case R.id.danmaku_t_big:
                presenter.onDanmakuTextSizeSelect(BIG_T);
                break;
            case R.id.danmaku_t_normal:
                presenter.onDanmakuTextSizeSelect(NORMAL_T);
                break;
            case R.id.danmaku_t_small:
                presenter.onDanmakuTextSizeSelect(SMALL_T);
                break;

            case R.id.danmaku_color:
                changeDanmakuSettingStyle(true);
                break;

            case R.id.danmaku_color_back:
                changeDanmakuSettingStyle(false);
                break;


            case R.id.video_intro_image:
                logger.i("intro");
                if (!isIntro) {
                    introImageView.animate().rotation(180);
                    detailFoldView.setVisibility(View.VISIBLE);
                } else {
                    introImageView.animate().rotation(0);
                    detailFoldView.setVisibility(View.GONE);
                }
                isIntro = !isIntro;
                break;

            case R.id.video_upload_av:
            case R.id.video_upload_user:
                presenter.onAvatarClick();
                break;

            case R.id.video_btn_follow:
                presenter.onFollow();
                break;

            case R.id.player_comment_edit:
                showSendDialog();
                break;

            case R.id.player_comment_fav:
                presenter.onFavorite();
                break;

            case R.id.player_comment_like:
                presenter.onLike();
                break;

            case R.id.video_tag_fold:
                logger.i("video_tag_fold");
                presenter.onTagFold();
                break;

            default:
                break;

        }
    }

    private void changeDanmakuSettingStyle(boolean isShowColors) {
        if (isShowColors) {
            danmakuSettingAView.setVisibility(View.GONE);
            danmakuSettingBView.setVisibility(View.VISIBLE);
        } else {
            danmakuSettingAView.setVisibility(View.VISIBLE);
            danmakuSettingBView.setVisibility(View.GONE);
        }
    }

    public void showSendDialog() {

        if (null == commentDialog) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_send_comment, null);
            commentDialog = new Dialog(getContext(), R.style.FullDiaolog_style);
            Window win = commentDialog.getWindow();
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //lp.height= WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = (int) getResources().getDimension(R.dimen.dialog_send_comment_layout_height);
            lp.gravity = Gravity.BOTTOM;
            win.setAttributes(lp);

            commentEditView = (EditText) view.findViewById(R.id.dialog_send_comment_edit);
            final TextView sendTextView = (TextView) view.findViewById(R.id.dialog_send_comment_send);
            commentEditView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().trim().length() > 0) {
                        sendTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                        sendTextView.setEnabled(true);
                    } else {
                        sendTextView.setTextColor(ContextCompat.getColor(getContext(), R.color
                                .video_detail_text__light_color));
                        sendTextView.setEnabled(false);
                    }
                }

            });

            sendTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.sendComment(commentEditView.getText().toString());
                    commentDialog.dismiss();
                    commentEditView.setText("");
                }
            });

            //commentDialog.show();
            commentDialog.setContentView(view);

        }

        commentDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                logger.i("onShow");
                UIUtils.showSoftInput(getActivity());
            }
        });

        commentDialog.show();

    }

    @Override
    public void replaySomeone(String someone) {
        if (commentEditView != null) {
            commentEditView.setText("");
            SpannableString spannableString = new SpannableString(someone);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#0099EE"));
            spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            commentEditView.setText(spannableString);
            commentEditView.setSelection(commentEditView.getText().length());
        }
    }

    private void onExpendScreen() {
        if (isPortrait()) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerRootView.getLayoutParams();
            if (0 == playerNormalHeight)
                playerNormalHeight = params.height;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            playerRootView.requestLayout();
            expendImageView.setImageResource(R.drawable.icon_quanback);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerRootView.getLayoutParams();
            params.height = playerNormalHeight;
            playerRootView.requestLayout();
            expendImageView.setImageResource(R.drawable.icon_quanping);
        }

        hideOverlay();
        showOverlay();
        logger.i("onExpendScreen");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        logger.i("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(screen);
        surfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        surfaceXDisplayRange = Math.max(screen.widthPixels, screen.heightPixels);
        resetOverlayLayout();

    }

    private void resetOverlayLayout() {
        //处理有导航栏的情况
        if (null == hubView || null == writeImageView || null == switchImageView || null == sendDanmakuView)
            return;

        Point point = NavigationBarUtils.getNavigationBarSize(this.getContext());
        if (point != null && point.y > 0) {
            logger.i("x=" + point.x + " y=" + point.y);
            boolean isBottom = NavigationBarUtils.isNavigationAtBottom(this.getContext());

            int navHeight = 119;
            if (isBottom)
                navHeight = point.y;
            else
                navHeight = point.x;

            logger.i("navHeight=" + navHeight);
            if (navHeight > 200) {
                logger.e("navHeith too height");
                navHeight = 119;
            }

            RelativeLayout.LayoutParams hubParams = (RelativeLayout.LayoutParams) hubView.getLayoutParams();
            RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) writeImageView.getLayoutParams();
            RelativeLayout.LayoutParams danmakuParams = (RelativeLayout.LayoutParams) sendDanmakuView.getLayoutParams();

            if (isPortrait()) {
                hubParams.bottomMargin = 0;
                hubParams.rightMargin = 0;
                imageParams.rightMargin = UIUtils.dip2px(16);
                danmakuParams.bottomMargin = 0;
                danmakuParams.rightMargin = 0;

            } else {
                if (isBottom) {
                    hubParams.bottomMargin = hubParams.bottomMargin + navHeight;
                    danmakuParams.bottomMargin = danmakuParams.bottomMargin + navHeight;
                    logger.i("margin portrait right=" + hubParams.rightMargin);
                } else {
                    hubParams.rightMargin = hubParams.rightMargin + navHeight;
                    imageParams.rightMargin = imageParams.rightMargin + navHeight;
                    danmakuParams.rightMargin = danmakuParams.rightMargin + navHeight;
                    logger.i("margin notpor right=" + hubParams.rightMargin);
                }
            }

            logger.i("margin right=" + hubParams.rightMargin);
            hubView.setLayoutParams(hubParams);
            hubView.requestLayout();
            writeImageView.setLayoutParams(imageParams);
            writeImageView.requestLayout();
            sendDanmakuView.setLayoutParams(danmakuParams);
            sendDanmakuView.requestLayout();
        }

        if (isPortrait()) {
            switchImageView.setVisibility(View.INVISIBLE);
            writeImageView.setVisibility(View.INVISIBLE);
            sendCommentView.setVisibility(View.VISIBLE);
            resolutionText.setVisibility(View.GONE);
        } else {
            switchImageView.setVisibility(View.VISIBLE);
            writeImageView.setVisibility(View.VISIBLE);
            sendCommentView.setVisibility(View.GONE);
            resolutionText.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void dimStatusBar(boolean dim) {
        logger.i("dimStatusBar=" + dim);
        if (dim)
            actionBar.hide();
        else
            actionBar.show();

        if (!AndroidUtil.isHoneycombOrLater)
            return;
        int visibility = 0;
        int navbar = 0;

        if (AndroidUtil.isJellyBeanOrLater) {
            visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (!isPortrait())
                navbar = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        if (dim) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                navbar |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            else
                visibility |= View.STATUS_BAR_HIDDEN;
            if (!AndroidDevices.hasCombBar()) {
                navbar |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (AndroidUtil.isKitKatOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
                if (AndroidUtil.isJellyBeanOrLater)
                    visibility |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        } else {
            actionBar.show();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (AndroidUtil.isICSOrLater)
                visibility |= View.SYSTEM_UI_FLAG_VISIBLE;
            else
                visibility |= View.STATUS_BAR_VISIBLE;
        }

        if (AndroidDevices.hasNavBar())
            visibility |= navbar;
        getActivity().getWindow().getDecorView().setSystemUiVisibility(visibility);
    }

    private void hideNivagation() {
        View decorView = getActivity().getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @SuppressWarnings("deprecation")
    private int getScreenRotation() {
        WindowManager wm = (WindowManager) BaseApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        try {
            Method m = display.getClass().getDeclaredMethod("getRotation");
            return (Integer) m.invoke(display);
        } catch (Exception e) {
            return Surface.ROTATION_0;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int getScreenOrientation(int mode) {
        switch (mode) {
            case 99: //screen orientation user
                return AndroidUtil.isJellyBeanMR2OrLater ?
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR :
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR;
            case 101: //screen orientation landscape
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            case 102: //screen orientation portrait
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        }
        /*
         mScreenOrientation = 100, we lock screen at its current orientation
         */
        WindowManager wm = (WindowManager) BaseApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int rot = getScreenRotation();
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        @SuppressWarnings("deprecation")
        boolean defaultWide = display.getWidth() > display.getHeight();
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide;
        if (defaultWide) {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                default:
                    return 0;
            }
        } else {
            switch (rot) {
                case Surface.ROTATION_0:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                case Surface.ROTATION_180:
                    // SCREEN_ORIENTATION_REVERSE_PORTRAIT only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                case Surface.ROTATION_270:
                    // SCREEN_ORIENTATION_REVERSE_LANDSCAPE only available since API
                    // Level 9+
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                default:
                    return 0;
            }
        }
    }

    private boolean isPortrait() {
        int orientation = getScreenOrientation(100);
        boolean portrait = orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        return portrait;
    }

    private void showKeyboard(EditText editText) {
        logger.i("showKeyboard");
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, 0);
    }

    public void closeKeyboard() {
        logger.i("hide keyborad");
        View view = getActivity().getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void prepareDanmaku() {
        danmakuView.prepare();
        danmakuView.removeAllLiveDanmakus();
    }

    @Override
    public void showDanmaku(DanmakuBean.DataBean bean) {
        danmakuView.addDanMu(bean, 500, 1, (byte) 0);
    }

    @Override
    public void showDanmaku(DanmakuBean bean, int alarm) {
        danmakuView.addListDanmaku(bean, alarm);
    }

    @Override
    public long getPosition() {
        return ijkVideoView.getCurrentPosition();
    }

    @Override
    public void clearInput() {
        danmakuEditText.setText("");
    }

    @Override
    public void showSocialShareDialog() {

        if (null == shareDialog) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_social_share, null);
            shareDialog = new Dialog(getContext(), R.style.FullDiaolog_style_translucence);
            Window win = shareDialog.getWindow();
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //lp.height= WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            win.setAttributes(lp);

            shareGridView = (GridView) view.findViewById(R.id.dialog_social_share_gridview);
            dimShareText = (TextView) view.findViewById(R.id.dialog_social_share_cancle);
            shareGridView.setAdapter(new SocialShareAdapter(getContext()));
            dimShareText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareDialog.dismiss();
                }
            });

            shareGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    presenter.onShareAction(position);
                    shareDialog.dismiss();
                }
            });

            shareDialog.setContentView(view);

        }

        shareDialog.show();


    }

    @Override
    public void toPresonalShowActivity(String userId, String userName, String avatar) {
        InExhibitionBean bean = new InExhibitionBean();
        bean.setUserName(userName);
        bean.setUserAvatar(avatar);
        PresonalShowActivity.actionStart(bean, getContext());
    }

    @Override
    public void showNotWifiDialog(final String url) {
        FragmentDialog.Builder builder = new FragmentDialog.Builder(getFragmentManager());
        builder.setMessage(getString(R.string.dialog_not_wifi_play))
                .setPositiveButton(getString(R.string.text_sure), new FragmentDialog.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logger.d("OK");
                        presenter.userMobileNetworkPlay();
                    }
                }).setNegativeButton(getString(R.string.text_cancle), new FragmentDialog.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                logger.d("NO");
            }
        }).show();

    }


    @Override
    public void showFaceBookDialog(ShareLinkContent content) {
        logger.i("showFaceBookDialog");
        if (null == callbackManager)
            callbackManager = CallbackManager.Factory.create();

        facebookShareDialog = new ShareDialog(this);
        facebookShareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {
                logger.i("onSuccess");
                //分享成功的回调，在这里做一些自己的逻辑处理
            }

            @Override
            public void onCancel() {
                logger.i("onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                logger.e("onError=" + error.getMessage());
            }
        });

        if (facebookShareDialog.canShow(ShareLinkContent.class))
            facebookShareDialog.show(content, ShareDialog.Mode.FEED);
        else
            Toast.makeText(this.getContext(), R.string.toast_no_install_facebook, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showResolutionPop(List<PlayMessageBean.PlayUrlsBean> beanList, int currentPosition) {

        if (null == beanList)
            return;

        View popView = LayoutInflater.from(this.getContext()).inflate(R.layout.pop_hd_select, null);
        RecyclerView recyclerView = (RecyclerView) popView.findViewById(R.id.pop_hd_recycler_view);
        HDselectAdapter adapter = new HDselectAdapter(this.getContext(), beanList, currentPosition);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setListener(new HDselectAdapter.OnHDselectListener() {
            @Override
            public void onSelect(int position) {
                presenter.saveRecord();
                presenter.onHDselect(position);
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(popView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        Rect rect = locateView(resolutionText);
        logger.i("location left=" + rect.left + "  right=" + rect.right + " top=" + rect.top + " bottom=" + rect
                .bottom);
        int width = rect.right - rect.left;
        int height = (int) getContext().getResources().getDimension(R.dimen.player_hub_hd_pop_item_height) * beanList
                .size();
        logger.i("pop view height=" + height);
        popupWindow.showAtLocation(resolutionText, Gravity.NO_GRAVITY, rect.left - width / 2, rect.top - height);
    }

    @Override
    public void showResolutionText(String clarity) {
        resolutionText.setText(clarity);
    }

    @Override
    public void showSeekToTips(String progress) {

    }

    private Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        logger.i("onPause");
        super.onPause();
        ijkVideoView.pause();
        danmakuView.pause();
        presenter.saveRecord();
    }

    @Override
    public void onResume() {
        super.onResume();
        showPlayLoading(true);
        ijkVideoView.resume();
        ijkVideoView.start();
        danmakuView.resume();
    }

    @Override
    public void onStop() {
        logger.i("onStop");
        super.onStop();
        stopPlay();
        hanler.removeMessages(FADE_OUT);
        hanler.removeMessages(SHOW_OVERLAY);
        presenter.onStop();
    }

    @Override
    public void onDetach() {
        logger.i("onDetach");
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        logger.i("onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}

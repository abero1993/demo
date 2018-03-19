package com.abero.utils.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ocean.motube.BaseApplication;
import com.ocean.motube.R;
import com.ocean.motube.hj.entity.MediaComment;
import com.ocean.motube.util.MyLogger;

import java.util.List;

/**
 * Created by abero on 2017/9/21.
 */

public class CommmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MediaComment> list;
    private OnReplyListener onReplyListener;
    private MyLogger myLogger = MyLogger.getAberoLog();

    public CommmentAdapter(Context context, List<MediaComment> list) {
        this.list = list;
        if (list != null) {
            for (MediaComment comment : list) {
                if (comment.getParentId() != 0) {
                    for (MediaComment parent : list)
                        if (comment.getParentId() == parent.getComentId())
                            comment.setCommentContent(context.getResources().getString(R.string.text_reply_tips, parent
                                    .getCommentUsername()) + comment.getCommentContent());
                }

                myLogger.i("alter commentuser=" + comment.getCommentUsername() + " id=" + comment.getComentId() + "" +
                        " pid=" + comment.getParentId() + " com=" + comment.getCommentContent());
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (0 == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_not_comment_item, parent,
                    false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_comment_item, parent, false);
            return new CommentHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (1 == getItemViewType(position)) {
            CommentHolder commentHolder = (CommentHolder) holder;
            commentHolder.onBind(position, list.get(position), onReplyListener);
        }
    }

    @Override
    public int getItemCount() {
        if (null == list)
            return 1;
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (null == list)
            return 0;
        else
            return 1;
    }

    public interface OnReplyListener {
        void onReply(String userName, long commentId);
    }

    public void setOnReplyListener(OnReplyListener listener) {
        this.onReplyListener = listener;
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView userTextView;
        private TextView dateTextView;
        private TextView msgTextView;

        public CommentHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.player_commment_item_av);
            userTextView = (TextView) itemView.findViewById(R.id.player_comment_item_user);
            dateTextView = (TextView) itemView.findViewById(R.id.player_comment_item_date);
            msgTextView = (TextView) itemView.findViewById(R.id.player_comment_item_msg);

        }

        public void onBind(int position, final MediaComment comment, final OnReplyListener listener) {

            Glide.with(BaseApplication.getContext())
                    .load(comment.getUserAvatar())
                    //.dontAnimate()
                    .asBitmap()
                    //.crossFade()//渐显动画
                    .placeholder(R.drawable.head_no_play)
                    .error(R.drawable.head_fail_play)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create
                                    (BaseApplication.getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            circularBitmapDrawable.setAntiAlias(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            userTextView.setText(comment.getCommentUsername());
            dateTextView.setText(comment.getCommentTime());
            if (comment.getParentId() != 0) {
                SpannableString spannableString = new SpannableString(comment.getCommentContent());
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#0099EE"));
                spannableString.setSpan(colorSpan, comment.getCommentContent().indexOf('@'), comment
                        .getCommentContent().indexOf(':'), Spanned
                        .SPAN_INCLUSIVE_EXCLUSIVE);
                msgTextView.setText(spannableString);
            } else {
                msgTextView.setText(comment.getCommentContent());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onReply(comment.getCommentUsername(), comment.getComentId());
                }
            });
        }


    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}

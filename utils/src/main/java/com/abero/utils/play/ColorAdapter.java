package com.abero.utils.play;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ocean.motube.R;

/**
 * Created by abero on 2017/9/28.
 */

public class ColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    String[] colors = new String[]{"#EB6877", "#5fFFFFFF", "#FFF100", "#5FF2A0", "#B28850", "#EA68A2", "#00A0E9"};

    private Context context;
    private int current = 0;
    private OnItemClickListener listener;

    public ColorAdapter(Context context) {
        this.context = context;
    }

    public interface OnItemClickListener {
        void onClick(String color);
    }

    public void setOnItemListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateCurrentPosition(String color) {
        if (!TextUtils.isEmpty(color)) {
            for (int i = 0; i < colors.length; i++)
                if (colors[i].equals(color))
                    current = i;
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView view = (ImageView) LayoutInflater.from(context).inflate(R.layout.danmaku_settting_color_item,
                parent, false);
        return new ColorHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ColorHolder colorHolder = (ColorHolder) holder;
        colorHolder.onBind(colors[position], position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }

    class ColorHolder extends RecyclerView.ViewHolder {

        public ColorHolder(View itemView) {
            super(itemView);
        }

        public void onBind(final String color, final int position) {
            GradientDrawable drawable = (GradientDrawable) itemView.getBackground();
            drawable.setColor(Color.parseColor(color));
            ImageView imageView = (ImageView) itemView;
            if (current == position)
                imageView.setImageResource(R.drawable.icon_gou);
            else
                imageView.setImageDrawable(null);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onClick(colors[position]);
                    current = position;
                    notifyDataSetChanged();
                }
            });
        }


    }

}

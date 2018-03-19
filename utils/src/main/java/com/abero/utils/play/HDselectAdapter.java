package com.abero.utils.play;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ocean.motube.R;
import com.ocean.motube.hj.entity.PlayMessageBean;

import java.util.List;

/**
 * Created by abero on 2017/11/6.
 */

public class HDselectAdapter extends RecyclerView.Adapter<HDselectAdapter.HDviewHolder> {

    private Context context;
    private List<PlayMessageBean.PlayUrlsBean> list;
    private OnHDselectListener listener;
    private int currentPosition;

    public HDselectAdapter(Context context, List<PlayMessageBean.PlayUrlsBean> list, int currentPostion) {
        this.currentPosition = currentPostion;
        this.context = context;
        this.list = list;
    }

    @Override
    public HDviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_hd_select_recyclerview_item,
                parent, false);
        return new HDviewHolder(view);
    }

    @Override
    public void onBindViewHolder(HDviewHolder holder, int position) {
        holder.onBind(context, position, list.get(position), listener, currentPosition == position ? true : false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void setListener(OnHDselectListener listener) {
        this.listener = listener;
    }

    interface OnHDselectListener {
        void onSelect(int position);
    }

    public static class HDviewHolder extends RecyclerView.ViewHolder {

        TextView text;
        public HDviewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.pop_hd_select_recyclerview_item_text);
        }


        public void onBind(Context context, final int position, PlayMessageBean.PlayUrlsBean bean, final
        OnHDselectListener listener,
                           boolean isHighlight) {
            text.setText(bean.getClarity());
            if (isHighlight)
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.pop_hd_text_bg_higtlight));
            else
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.pop_hd_text_bg));
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onSelect(position);
                }
            });
        }

    }
}

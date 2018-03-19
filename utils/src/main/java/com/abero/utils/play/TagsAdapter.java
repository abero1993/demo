package com.abero.utils.play;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ocean.motube.R;

import java.util.List;

/**
 * Created by abero on 2017/11/30.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagHolderView> {

    private List<String> list;

    public TagsAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public TagHolderView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_tag_item, parent, false);
        return new TagHolderView(view);
    }

    @Override
    public void onBindViewHolder(TagHolderView holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TagHolderView extends RecyclerView.ViewHolder {

        private TextView textView;

        public TagHolderView(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.recycler_tag_item_text);
        }

        public void onBind(String tag) {
            textView.setText(tag);
        }
    }
}

package com.abero.utils.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ocean.motube.R;
import com.ocean.motube.hj.entity.PlaylistBean;

import java.util.List;

/**
 * Created by abero on 2017/9/26.
 */

public class PlaylistAdapter extends BaseAdapter {


    List<PlaylistBean> list;
    private Context context;

    public PlaylistAdapter(Context context, List<PlaylistBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View         view         = LayoutInflater.from(context).inflate(R.layout.player_playlist_item, parent, false);
        PlaylistBean playlistBean = list.get(position);
        ImageView    imageView    = (ImageView) view.findViewById(R.id.player_playlist_item_image);
        TextView     numText      = (TextView) view.findViewById(R.id.player_playlist_item_views);
        TextView     nameText     = (TextView) view.findViewById(R.id.player_playlist_item_name);
        Glide.with(context).load(playlistBean.getPlaylistPicture()).placeholder(R.drawable.head_no)
                .into(imageView);
        numText.setText(""+ playlistBean.getPlaylistMediaNum());
        nameText.setText(playlistBean.getPlaylistName());

        return view;
    }

}

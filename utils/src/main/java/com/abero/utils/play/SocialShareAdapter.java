package com.abero.utils.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ocean.motube.R;

/**
 * Created by abero on 2017/9/30.
 */

public class SocialShareAdapter extends BaseAdapter {


    private Context context;
    private ShareItem[] shareItems = new ShareItem[3];

    public SocialShareAdapter(Context context) {
        this.context = context;
        shareItems[0] = new ShareItem(1, context.getString(R.string.text_fackbook), R.drawable.logo_facebook);
        shareItems[1] = new ShareItem(2, context.getString(R.string.text_wechat), R.drawable.logo_wechat);
        shareItems[2] = new ShareItem(3, context.getString(R.string.text_moments), R.drawable.logo_moments);
    }

    public class ShareItem {
        public int id;
        public String type;
        public int reid;

        public ShareItem(int id, String type, int reid) {
            this.id = id;
            this.type = type;
            this.reid = reid;
        }
    }


    @Override
    public int getCount() {
        return shareItems.length;
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
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_social_share_item, parent, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialog_social_share_item_image);
        TextView textView = (TextView) view.findViewById(R.id.dialog_social_share_text);
        imageView.setImageResource(shareItems[position].reid);
        textView.setText(shareItems[position].type);
        return view;
    }
}

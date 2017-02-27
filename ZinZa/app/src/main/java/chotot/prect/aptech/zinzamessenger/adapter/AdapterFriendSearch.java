package chotot.prect.aptech.zinzamessenger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import chotot.prect.aptech.zinzamessenger.R;
import chotot.prect.aptech.zinzamessenger.model.User;

/**
 * Created by ASUS on 02/27/2017.
 */

public class AdapterFriendSearch extends BaseAdapter {
    private Context mContext;
    private int mLayout;
    private List<User> mListUser;

    public AdapterFriendSearch(Context mContext, int mLayout, List<User> mListUser) {
        this.mContext = mContext;
        this.mLayout = mLayout;
        this.mListUser = mListUser;
    }

    @Override
    public int getCount() {
        return mListUser.size();
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
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(mLayout,null);
        ImageView avatarUser = (ImageView)convertView.findViewById(R.id.imgFriendSearch);
        TextView nameUser = (TextView)convertView.findViewById(R.id.txtNameFriendSearch);
        if(!mListUser.get(position).getmAvatar().equals("")){
            Picasso.with(mContext).load(mListUser.get(position).getmAvatar()).into(avatarUser);
        }
        nameUser.setText(mListUser.get(position).getmUsername());
        return convertView;
    }
}

package com.cabily.adapter;

/**
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cabily.pojo.HomePojo;
import com.cabily.utils.ImageLoader;
import com.casperon.app.cabily.R;

import java.util.ArrayList;


public class BookMyRide_Adapter extends BaseAdapter {

    private ArrayList<HomePojo> data;
    private ImageLoader imageLoader;
    private LayoutInflater mInflater;
    private Context context;

    public BookMyRide_Adapter(Context c, ArrayList<HomePojo> d) {
        context = c;
        mInflater = LayoutInflater.from(context);
        data = d;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }


    public class ViewHolder {
        private ImageView image;
        private TextView name;
        private TextView time;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.bookmyride_single, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.bookmyride_single_carname);
            holder.time = (TextView) view.findViewById(R.id.bookmyride_single_time);
            holder.image = (ImageView) view.findViewById(R.id.bookmyride_single_car_image);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(data.get(position).getCat_name());
       // holder.time.setText(data.get(position).getCat_time());

        if (data.get(position).getSelected_Cat().equalsIgnoreCase(data.get(position).getCat_id())) {
            imageLoader.DisplayImage(String.valueOf(data.get(position).getIcon_active()), holder.image);
        } else {
            imageLoader.DisplayImage(String.valueOf(data.get(position).getIcon_normal()), holder.image);
        }


        return view;
    }
}


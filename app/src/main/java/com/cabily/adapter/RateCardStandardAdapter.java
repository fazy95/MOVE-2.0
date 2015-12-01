package com.cabily.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cabily.pojo.RateCard_StdPojo;
import com.cabily.utils.ImageLoader;
import com.casperon.app.cabily.R;

import java.util.ArrayList;

/**
 * Created by Prem Kumar and Anitha on 10/20/2015.
 */
public class RateCardStandardAdapter extends BaseAdapter
{

    private ArrayList<RateCard_StdPojo> data;
    private ImageLoader imageLoader;
    private LayoutInflater mInflater;
    private Context context;

    public RateCardStandardAdapter(Context c,ArrayList<RateCard_StdPojo> d)
    {
        context=c;
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
    public int getViewTypeCount()
    {
        return 1;
    }


    public class ViewHolder
    {
        private TextView price;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        ViewHolder holder;
        if(convertView == null)
        {
            view = mInflater.inflate(R.layout.ratecard_standard_single, parent, false);
            holder = new ViewHolder();
            holder.price = (TextView) view.findViewById(R.id.ratecard_standard_single_price);
            view.setTag(holder);
        }
        else
        {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        holder.price.setText(data.get(position).getStdrate_title()+context.getResources().getString(R.string.ratecard_lable_space)+context.getResources().getString(R.string.ratecard_lable_single_space)+data.get(position).getStdrate_fare());
        return view;
    }
}



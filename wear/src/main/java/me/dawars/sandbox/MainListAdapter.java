package me.dawars.sandbox;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Dawars on 11.05.2015.
 */
public class MainListAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private String[] listItems;

    public MainListAdapter(Context context, String[] listItems) {
        mInflater = LayoutInflater.from(context);
        this.listItems = listItems;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(
                mInflater.inflate(android.R.layout.simple_list_item_1, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        TextView view = (TextView) holder.itemView.findViewById(android.R.id.text1);
        view.setText(listItems[position].toString());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return listItems.length;
    }
}

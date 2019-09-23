package com.channelize.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;


public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
    private MyListData[] listdata;
    private OnItemClickListener onItemClickListener;

    // RecyclerView recyclerView;
    public MyListAdapter(MyListData[] listdata, OnItemClickListener onItemClickListener) {
        this.listdata = listdata;
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MyListData myListData = listdata[position];
        holder.textView.setText(listdata[position].getEmail());
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public View rootView;
        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            this.imageView = itemView.findViewById(R.id.imageView);
            this.textView = itemView.findViewById(R.id.textView);
        }
    }
}

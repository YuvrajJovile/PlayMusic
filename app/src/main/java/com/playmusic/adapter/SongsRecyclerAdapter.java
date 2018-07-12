package com.playmusic.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.playmusic.R;
import com.playmusic.adapter.iListner.IRecyclerClickListener;
import com.playmusic.adapter.viewHolder.SongsRecyclerViewHolder;
import com.playmusic.model.SongsModel;

import java.util.ArrayList;

public class SongsRecyclerAdapter extends RecyclerView.Adapter<SongsRecyclerViewHolder> {

    private ArrayList<SongsModel> mSongsList;
    private IRecyclerClickListener<SongsModel> mIRecyclerListner;


    public SongsRecyclerAdapter(ArrayList<SongsModel> mSongsList, IRecyclerClickListener<SongsModel> mIRecyclerListner) {
        this.mSongsList = mSongsList;
        this.mIRecyclerListner = mIRecyclerListner;
    }

    @Override
    public SongsRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SongsRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_songs_item, parent, false), mIRecyclerListner);
    }

    @Override
    public void onBindViewHolder(SongsRecyclerViewHolder holder, int position) {
        holder.populateData(mSongsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSongsList.size();
    }
}

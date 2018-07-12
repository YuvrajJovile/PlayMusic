package com.playmusic.adapter.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.playmusic.R;
import com.playmusic.adapter.iListner.IRecyclerClickListener;
import com.playmusic.model.SongsModel;


public class SongsRecyclerViewHolder extends RecyclerView.ViewHolder {

    private IRecyclerClickListener<SongsModel> mIRecyclerListner;
    private TextView mSongTitle;
    private ImageView mSongImage;

    public SongsRecyclerViewHolder(View itemView, IRecyclerClickListener<SongsModel> pIRecyclerListner) {
        super(itemView);
        this.mIRecyclerListner = pIRecyclerListner;
        mSongTitle = itemView.findViewById(R.id.tv_song_title);
        mSongImage = itemView.findViewById(R.id.iv_song_image);

    }

    public void populateData(final SongsModel pSongsModel) {

        if (pSongsModel != null) {
            mSongTitle.setText(pSongsModel.getTitle());
            mSongTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIRecyclerListner.onClick(pSongsModel, getAdapterPosition());
                }
            });
        }

        Glide.with(itemView).load((getAdapterPosition() % 2 == 0) ? R.drawable.ic_compact_disc : R.drawable.ic_shuffle).apply(new RequestOptions().circleCrop()).into(mSongImage);
    }
}

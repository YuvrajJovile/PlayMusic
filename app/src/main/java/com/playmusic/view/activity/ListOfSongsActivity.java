package com.playmusic.view.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.playmusic.R;
import com.playmusic.adapter.SongsRecyclerAdapter;
import com.playmusic.adapter.iListner.IRecyclerClickListener;
import com.playmusic.model.SongsModel;
import com.playmusic.util.CodeSnipet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static com.playmusic.util.IContstants.bundleDatas.CURRENT_SONG;
import static com.playmusic.util.IContstants.bundleDatas.IS_SHUFFLED;
import static com.playmusic.util.IContstants.bundleDatas.SONGS_LIST;

public class ListOfSongsActivity extends AppCompatActivity {


    private final String TAG = "ListOfSongsActivity";
    private final int SONGS_REQUEST_CODE = 200;
    private RecyclerView mSongsRecyclerView;
    private SongsRecyclerAdapter mSongsRecyclerAdapter;
    private ArrayList<SongsModel> mSongsList;
    private TextView mNoSongsFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);
        mSongsRecyclerView = findViewById(R.id.rv_songs_list);
        mNoSongsFound = findViewById(R.id.tv_no_songs_found);
        mSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkForPermission();
        FloatingActionButton lFabShuffle = findViewById(R.id.fab_shuffle);
        lFabShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSongsList != null && !mSongsList.isEmpty()) {
                    int maxInt = mSongsList.size();
                    int lShuffledVal = new Random().nextInt(maxInt);
                    CodeSnipet.showLog(TAG, "ShuffledVal==" + lShuffledVal);
                    navigateToPlayActivity(lShuffledVal);
                }
            }
        });

    }

    private void setOrNotifyAdapter() {
        if (mSongsRecyclerAdapter == null) {
            mSongsRecyclerAdapter = new SongsRecyclerAdapter(mSongsList, new IRecyclerClickListener<SongsModel>() {
                @Override
                public void onClick(SongsModel data, int pos) {
                    navigateToPlayActivity(pos);
                }
            });
            mSongsRecyclerView.setAdapter(mSongsRecyclerAdapter);
        } else {
            mSongsRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void navigateToPlayActivity(int pos) {

        startActivity(new Intent(ListOfSongsActivity.this, MusicPlayActivity.class)
                .putParcelableArrayListExtra(SONGS_LIST, mSongsList)
                .putExtra(CURRENT_SONG, pos)
                .putExtra(IS_SHUFFLED, true));
    }

    private void checkForPermission() {

        if (ContextCompat.checkSelfPermission(ListOfSongsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request for permission
            ActivityCompat.requestPermissions(ListOfSongsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SONGS_REQUEST_CODE);

        } else {
            //permission granted
            getSongsFromDevice();
        }

    }

    private void getSongsFromDevice() {
        mSongsList = new ArrayList<>();
        Cursor lCursor = null;
        try {
            ContentResolver lContentResolver = getContentResolver();
            Uri lSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            lCursor = lContentResolver.query(lSongsUri, null, null, null, null);
            if (lCursor != null && lCursor.moveToFirst()) {

                do {
                    String lTitle = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String lPath = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String lImagePath = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String lArtist = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String lTemp2 = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String lTemp3 = lCursor.getString(lCursor.getColumnIndex(MediaStore.Audio.Media.YEAR));



                    CodeSnipet.showLog(TAG, "imageID==" + lImagePath + " artist==" + " year==");
                    mSongsList.add(new SongsModel(lArtist,lTitle, lPath, lImagePath));
                } while (lCursor.moveToNext());

            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (lCursor != null) {
                lCursor.close();
            }
        }

        Collections.sort(mSongsList, new Comparator<SongsModel>() {
            @Override
            public int compare(SongsModel songsModel, SongsModel t1) {
                String lFirstObj = songsModel.getTitle().toLowerCase();
                String lSecondObj = t1.getTitle().toLowerCase();
                if (lFirstObj.compareTo(lSecondObj) > 1)
                    return 1;
                else if (lFirstObj.compareTo(lSecondObj) < 1) {
                    return -1;
                }
                return 0;
            }
        });

        if (mSongsList.size() > 0) {
            setOrNotifyAdapter();
            mNoSongsFound.setVisibility(View.GONE);
        } else {
            mNoSongsFound.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case SONGS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(ListOfSongsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //request for permission
                        getSongsFromDevice();
                    } else {
                        //no permission granted
                    }
                }
            }
            break;
        }

    }
}

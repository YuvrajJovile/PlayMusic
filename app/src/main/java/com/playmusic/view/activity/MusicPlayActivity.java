package com.playmusic.view.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.playmusic.R;
import com.playmusic.model.SongsModel;
import com.playmusic.service.BackgroundPlayService;
import com.playmusic.util.CodeSnipet;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import static com.playmusic.util.IContstants.bundleDatas.ALBUM_DATA;
import static com.playmusic.util.IContstants.bundleDatas.CURRENT_SONG;
import static com.playmusic.util.IContstants.bundleDatas.IMAGE_DATA;
import static com.playmusic.util.IContstants.bundleDatas.IS_SHUFFLED;
import static com.playmusic.util.IContstants.bundleDatas.SONGS_LIST;
import static com.playmusic.util.IContstants.bundleDatas.TITLE_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_FAVORITE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_MUSIC_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_NEXT;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PLAYBACK_TIME;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PLAY_PAUSE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PREVIOUS;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SEEK;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SHUFFLE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SONG_TITLE_AND_DURATION;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_STOP;
import static com.playmusic.util.IContstants.serviceIntentData.FAVORITE;
import static com.playmusic.util.IContstants.serviceIntentData.NOTIFICATION_CODE;
import static com.playmusic.util.IContstants.serviceIntentData.PLAY_BACK_TIME;
import static com.playmusic.util.IContstants.serviceIntentData.PLAY_PAUSE;
import static com.playmusic.util.IContstants.serviceIntentData.SEEK_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.SONG_TIME;


public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener {


    private final String TAG = "MusicPlayActivity";
    private TextView mFromTime, mToTime, mSongTitle, mSongAlbumName;
    private ImageButton mPrevious, mNext, mPlayPause;
    private ImageView mSongImage;
    private AppCompatSeekBar mSeekbar;


    private ArrayList<SongsModel> mSongsList;
    private int mCurrentSong = -1;

    private boolean mIsShuffle, mIsPlaying;
    private ImageView mIvShuffle, mIvFavorite;


    private Intent mServicePlayBackIntent;
    private boolean mIsServiceSeek, mIsFavourite;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        mFromTime = findViewById(R.id.tv_from_time);
        mToTime = findViewById(R.id.tv_to_time);
        mSongTitle = findViewById(R.id.tv_song_name);
        mPrevious = findViewById(R.id.ib_previous);
        mPlayPause = findViewById(R.id.ib_play_pause);
        mNext = findViewById(R.id.ib_next);
        mSongImage = findViewById(R.id.iv_song_image);
        mSeekbar = findViewById(R.id.v_seek_bar);
        mIvShuffle = findViewById(R.id.iv_shuffle);
        mSongAlbumName = findViewById(R.id.tv_album_title);
        mIvFavorite = findViewById(R.id.iv_favorites);

        mIvFavorite.setOnClickListener(this);
        mIvShuffle.setOnClickListener(this);
        mPrevious.setOnClickListener(this);
        mPlayPause.setOnClickListener(this);
        mNext.setOnClickListener(this);


        init();

    }

    private void init() {
        Intent lIntent = getIntent();
        if (lIntent.getExtras() != null) {

            mSongsList = lIntent.getParcelableArrayListExtra(SONGS_LIST);
            mCurrentSong = lIntent.getIntExtra(CURRENT_SONG, -1);
            mIsShuffle = lIntent.getBooleanExtra(IS_SHUFFLED, false);
            mIvShuffle.setImageResource(mIsShuffle ? R.drawable.ic_shuffle_selected : R.drawable.ic_repeat_all);


        }


        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int lSeekTime = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!mIsServiceSeek)
                    lSeekTime = i;
                showMessage("seekTime==" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (!mIsServiceSeek) {

                    mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_SEEK);
                    mServicePlayBackIntent.putExtra(SEEK_DATA, lSeekTime);
                    sendCommandToService();

                }
            }


        });


        mServicePlayBackIntent = new Intent(this, BackgroundPlayService.class);
        mServicePlayBackIntent.putParcelableArrayListExtra(SONGS_LIST, mSongsList);
        mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_MUSIC_DATA);
        mServicePlayBackIntent.putExtra(CURRENT_SONG, mCurrentSong);
        mServicePlayBackIntent.putExtra(COMMAND_SHUFFLE, mIsShuffle);


        startService(mServicePlayBackIntent);
    }


    private boolean mOddEven;
    private Animation mAnimation;

    private void loadSongImage(Bitmap lBitmap) {

       /* Drawable lImageDrawable = Drawable.createFromPath(imagePath);
        mSongImage.setImageDrawable(lImageDrawable);*/

        // Glide.with(this).load(R.drawable.giphy_four).apply(new RequestOptions().circleCrop()).into(mSongImage);
        try {
            Glide.with(this).load(lBitmap).apply(new RequestOptions().circleCrop()
                    .placeholder(R.drawable.ic_music_player))
                    .transition(new DrawableTransitionOptions().crossFade(1000))
                    .into(mSongImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mOddEven = !mOddEven;
        mAnimation = AnimationUtils.loadAnimation(this, mOddEven ? R.anim.rotate_anim : R.anim.reverse_rotate);
        mSongImage.startAnimation(mAnimation);
    }


    private String getPlayTime(int duration) {
        CodeSnipet.showLog(TAG, "Duration==" + duration);
        return String.format("%02d.%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ib_previous) {
            playPrevious();

        } else if (i == R.id.ib_play_pause) {
            togglePlayPause();

        } else if (i == R.id.ib_next) {
            playNext();

        } else if (i == R.id.iv_shuffle) {
            shuffleAndPlayNext();
        } else if (i == R.id.iv_favorites) {
            mIsServiceSeek = true;
            mIsFavourite = !mIsFavourite;
            mIvFavorite.setImageDrawable(getResources().getDrawable(mIsFavourite ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite_unselected));
            CodeSnipet.showLog(TAG, "favInAct==" + mIsFavourite);
            mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_FAVORITE);
            mServicePlayBackIntent.putExtra(FAVORITE, mIsFavourite);
            sendCommandToService();
            mIsServiceSeek = false;
            CodeSnipet.showToast(MusicPlayActivity.this, mIsFavourite ? "Added to favorites" : "Removed from favorites");
        }

    }

    private void shuffleAndPlayNext() {

        mIsShuffle = !mIsShuffle;
        mIvShuffle.setImageResource(mIsShuffle ? R.drawable.ic_shuffle_selected : R.drawable.ic_repeat_all);
        mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_SHUFFLE);
        mServicePlayBackIntent.putExtra(COMMAND_SHUFFLE, mIsShuffle);
        sendCommandToService();
        CodeSnipet.showToast(this, mIsShuffle ? "Shuffle is on" : "Shuffle is off");
    }

    private void showMessage(String pMessage) {
        Log.d(TAG, pMessage);
    }

    private void playNext() {
        mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_NEXT);
        sendCommandToService();

    }

    private void togglePlayPause() {

        showMessage("Onclick is called");
        mIsPlaying = !mIsPlaying;
        mPlayPause.setImageDrawable(getResources().getDrawable(mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play));
        pausePlayAnimation();
        fromTimeBlinkAnim();
        mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_PLAY_PAUSE);
        sendCommandToService();
    }


    private Handler lHandler = new Handler();
    private Runnable mHandlerRunnable;

    private void fromTimeBlinkAnim() {
        if (mIsPlaying)
            mFromTime.setVisibility(View.VISIBLE);
        else {
            mHandlerRunnable = new Runnable() {
                @Override
                public void run() {
                    mFromTime.setVisibility(mFromTime.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    lHandler.postDelayed(this, 1000);
                }
            };
            lHandler.post(mHandlerRunnable);
        }
    }


    private void pausePlayAnimation() {
        if (mAnimation != null) {
            if (!mIsPlaying) {


                mAnimation.cancel();
                mAnimation.reset();
            } else {
                if (mHandlerRunnable != null) {
                    lHandler.removeCallbacks(mHandlerRunnable);
                }

                mAnimation = AnimationUtils.loadAnimation(this, mOddEven ? R.anim.rotate_anim : R.anim.reverse_rotate);
                mSongImage.setAnimation(mAnimation);
            }
        }

    }

    private void sendCommandToService() {
        startService(mServicePlayBackIntent);
    }


    private void playPrevious() {

        mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_PREVIOUS);
        sendCommandToService();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private BroadcastReceiver mBackgroundPlayBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String lCommand = intent.getStringExtra(COMMAND_DATA);
            // CodeSnipet.showLog(TAG, "Command==" + lCommand);
            switch (lCommand) {

                case COMMAND_SONG_TITLE_AND_DURATION:
                    String lTitle = intent.getStringExtra(TITLE_DATA);
                    String lAlbumData = intent.getStringExtra(ALBUM_DATA);
                    String lImagePath = intent.getStringExtra(IMAGE_DATA);
                    int lSongTime = intent.getIntExtra(SONG_TIME, 0);
                    mSongTitle.setText(lTitle);
                    mSongAlbumName.setText(lAlbumData);
                    mToTime.setText(getPlayTime(lSongTime));
                    mSeekbar.setMax(lSongTime);
                    mIsServiceSeek = true;
                    mSeekbar.setProgress(0);
                    mIsServiceSeek = false;
                    mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));


                    Bitmap alBumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);
                    Bitmap lTempBitmap = null;
                    try {
                        Uri lImageUri = getAlbumArtUri(Long.parseLong(lImagePath));
                        lTempBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), lImageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (lTempBitmap != null) {
                        alBumArt = lTempBitmap;
                    }

                    loadSongImage(alBumArt);
                    mIsPlaying = true;
                    break;

                case COMMAND_PLAYBACK_TIME:
                    int lPlayBackTime = intent.getIntExtra(PLAY_BACK_TIME, 0);
                    mFromTime.setText(getPlayTime(lPlayBackTime));
                    mIsServiceSeek = true;
                    mSeekbar.setProgress(lPlayBackTime);
                    mIsServiceSeek = false;
                    break;
                case COMMAND_PLAY_PAUSE:
                    mIsServiceSeek = true;
                    mIsPlaying = intent.getBooleanExtra(PLAY_PAUSE, false);
                    mPlayPause.setImageDrawable(getResources().getDrawable(mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play));
                    pausePlayAnimation();
                    fromTimeBlinkAnim();
                    mIsServiceSeek = false;
                    break;

                case COMMAND_STOP:
                    mIsPlaying = false;
                    mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    pausePlayAnimation();
                    finish();
                    break;
                case COMMAND_SHUFFLE:
                    mIsServiceSeek = true;
                    mIsShuffle = intent.getBooleanExtra(COMMAND_SHUFFLE, false);
                    mIvShuffle.setImageDrawable(getResources().getDrawable(mIsShuffle ? R.drawable.ic_shuffle_selected : R.drawable.ic_repeat_all));
                    CodeSnipet.showToast(MusicPlayActivity.this, mIsShuffle ? "Shuffle is on" : "Shuffle is off");
                    mIsServiceSeek = false;
                    break;

                case COMMAND_FAVORITE:
                    mIsServiceSeek = true;
                    mIsFavourite = intent.getBooleanExtra(FAVORITE, false);
                    mIvFavorite.setImageDrawable(getResources().getDrawable(mIsFavourite ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite_unselected));
                    CodeSnipet.showToast(MusicPlayActivity.this, mIsFavourite ? "Added to favorites" : "Removed from favorites");
                    mIsServiceSeek = false;
                    break;
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBackgroundPlayBroadCast, new IntentFilter("BackgroundPlayBroadcast"));

    }

    @Override
    protected void onStop() {
        super.onStop();
        CodeSnipet.showLog(TAG, "BroadCast is unregistered");
        try {
            // unregisterReceiver(mBackgroundPlayBroadCast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServiceAndNotification();
    }

    private void stopServiceAndNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_CODE);
            mServicePlayBackIntent.putExtra(COMMAND_DATA, COMMAND_STOP);
            sendCommandToService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }
}

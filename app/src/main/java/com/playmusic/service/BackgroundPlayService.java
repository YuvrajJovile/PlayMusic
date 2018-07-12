package com.playmusic.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import com.playmusic.R;
import com.playmusic.model.SongsModel;
import com.playmusic.receiver.MyPlayNotificationReceiver;
import com.playmusic.util.CodeSnipet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;
import static com.playmusic.util.IContstants.bundleDatas.ALBUM_DATA;
import static com.playmusic.util.IContstants.bundleDatas.CURRENT_SONG;
import static com.playmusic.util.IContstants.bundleDatas.IMAGE_DATA;
import static com.playmusic.util.IContstants.bundleDatas.SONGS_LIST;
import static com.playmusic.util.IContstants.bundleDatas.TITLE_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_FAVORITE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_MUSIC_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_NEXT;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PLAYBACK_TIME;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PLAY_PAUSE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PLAY_PAUSE_NOTIFICATION;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_PREVIOUS;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SEEK;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SHUFFLE;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_SONG_TITLE_AND_DURATION;
import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_STOP;
import static com.playmusic.util.IContstants.serviceIntentData.FAVORITE;
import static com.playmusic.util.IContstants.serviceIntentData.MY_NOTIFICATION_ID;
import static com.playmusic.util.IContstants.serviceIntentData.NOTIFICATION_CODE;
import static com.playmusic.util.IContstants.serviceIntentData.PLAY_BACK_TIME;
import static com.playmusic.util.IContstants.serviceIntentData.PLAY_PAUSE;
import static com.playmusic.util.IContstants.serviceIntentData.SEEK_DATA;
import static com.playmusic.util.IContstants.serviceIntentData.SHUFFLE;
import static com.playmusic.util.IContstants.serviceIntentData.SONG_TIME;

public class BackgroundPlayService extends Service {


    private final String TAG = "BackgroundPlayService";
    private MediaPlayer mMediaPlayer;
    private ArrayList<SongsModel> mSongsList;
    private int mCurrentPos;
    private boolean mIsPlaying;
    private boolean mIsShuffle;
    private boolean mIsFavorite;
    private int mCurrentSongDuration;
    private int mCurrentSongPlayBackTime;
    private Intent mBroadCastIntent = new Intent("BackgroundPlayBroadcast");


    private RemoteViews mRemoteViews;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private boolean mIsSongDataSent;


    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSessionCompat;
    private MediaControllerCompat.TransportControls mTransportControls;

    private SongsModel mCurrentSongModel;


    @Override
    public void onCreate() {
        CodeSnipet.showLog(TAG, "OnCreateOfService");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String lCommand = intent.getStringExtra(COMMAND_DATA);

        switch (lCommand) {

            case COMMAND_MUSIC_DATA:
                mIsShuffle = intent.getBooleanExtra(COMMAND_SHUFFLE, false);
                getMusicData(intent);
                break;
            case COMMAND_PREVIOUS:
                playPreviousSong();
                break;
            case COMMAND_PLAY_PAUSE:
                playOrPauseMedia();
                break;
            case COMMAND_PLAY_PAUSE_NOTIFICATION:
                playOrPauseMedia();
                playPauseToView();
                break;
            case COMMAND_NEXT:
                playNextSong();
                break;
            case COMMAND_SEEK:
                seekMediaPlayer(intent.getIntExtra(SEEK_DATA, 0));
                break;
            case COMMAND_SHUFFLE:
                mIsShuffle = intent.getBooleanExtra(COMMAND_SHUFFLE, false);
                buildNotification();
                break;
            case SHUFFLE:
                mIsShuffle = !mIsShuffle;
                buildNotification();
                mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_SHUFFLE);
                mBroadCastIntent.putExtra(COMMAND_SHUFFLE, mIsShuffle);
                sendBroadcastToView();
                break;
            case COMMAND_STOP:
                stopMediaPlayer();
                break;
            case COMMAND_FAVORITE:
                mIsSongDataSent = false;
                CodeSnipet.showLog(TAG,"isFav=="+mIsFavorite);
                mIsFavorite = !mIsFavorite;
                CodeSnipet.showLog(TAG,"isFav=="+mIsFavorite);
                mCurrentSongModel.setIsfavourite(mIsFavorite);
                buildNotification();
                mIsSongDataSent = true;
                break;

            case FAVORITE:
                toggleFavorite();
                break;
        }


        return START_NOT_STICKY;
    }

    private void toggleFavorite() {
        mIsSongDataSent = false;
        mIsFavorite = !mIsFavorite;
        mCurrentSongModel.setIsfavourite(mIsFavorite);
        mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_FAVORITE);
        mBroadCastIntent.putExtra(FAVORITE, mIsFavorite);
        sendBroadcastToView();
        buildNotification();
        mIsSongDataSent = true;
    }


    private void updateSeekbarInterval() {

        final Handler lHandler = new Handler();
        lHandler.post(new Runnable() {
            @Override
            public void run() {

                if (mIsSongDataSent && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    CodeSnipet.showLog(TAG, "sendingDATA");
                    mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_PLAYBACK_TIME);
                    mBroadCastIntent.putExtra(PLAY_BACK_TIME, mMediaPlayer.getCurrentPosition());
                    sendBroadcastToView();
                }
                lHandler.postDelayed(this, 1000);
            }
        });

    }


    private void sendBroadcastToView() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(mBroadCastIntent);
    }


    private void playOrPauseMedia() {
        if (mMediaPlayer != null) {

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }

            buildNotification();
        }
    }


    private void stopMediaPlayer() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_STOP);
            sendBroadcastToView();
            removeNotification();
            stopSelf();
        }
    }

    private void seekMediaPlayer(int pSeekData) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pSeekData);
        }
    }

    private void playPreviousSong() {
        if (mIsShuffle)
            playShuffledSong();
        else {
            if (mCurrentPos > 0) {
                mCurrentPos--;
                mCurrentSongModel = mSongsList.get(mCurrentPos);
                playSelection(mCurrentSongModel);
            }
        }
    }


    private void playSelection(SongsModel pSongsModel) {
        resetMediaPlayer();
        try {
            mMediaPlayer.setDataSource(pSongsModel.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            buildNotification();
            sendCurrentSongTitle(pSongsModel);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNextSong();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void playNextSong() {
        if (mIsShuffle)
            playShuffledSong();
        else {
            if (mCurrentPos < mSongsList.size() - 1) {
                mCurrentPos++;
                mCurrentSongModel = mSongsList.get(mCurrentPos);
                playSelection(mCurrentSongModel);
            }
        }

    }

    private void sendCurrentSongTitle(SongsModel pSongsModel) {

        mIsSongDataSent = false;
        mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_SONG_TITLE_AND_DURATION);
        mBroadCastIntent.putExtra(TITLE_DATA, pSongsModel.getTitle());
        mBroadCastIntent.putExtra(ALBUM_DATA, pSongsModel.getArtist());
        mBroadCastIntent.putExtra(IMAGE_DATA, pSongsModel.getImagePath());
        mBroadCastIntent.putExtra(SONG_TIME, mMediaPlayer.getDuration());
        LocalBroadcastManager.getInstance(this).sendBroadcast(mBroadCastIntent);
        mIsSongDataSent = true;

    }

    private void playShuffledSong() {
        int lTempPos = new Random().nextInt(mSongsList.size());
        mCurrentSongModel = mSongsList.get(lTempPos);
        playSelection(mSongsList.get(lTempPos));
    }

    private void resetMediaPlayer() {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();

    }

    private void getMusicData(Intent pIntent) {
        mNotificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        removeNotification();
        mSongsList = pIntent.getParcelableArrayListExtra(SONGS_LIST);
        mCurrentPos = pIntent.getIntExtra(CURRENT_SONG, 0);
        mCurrentSongModel = mSongsList.get(mCurrentPos);
        playSelection(mCurrentSongModel);
        updateSeekbarInterval();
    }


    /*private void showOrUpdateNotification() {




        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                initMediaSession();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_PLAY_PAUSE_NOTIFICATION).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent previousIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_PREVIOUS).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_NEXT).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent closeIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_STOP).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent logoIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(LOGO).setClass(this, MyPlayNotificationReceiver.class), 0);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        mRemoteViews.setOnClickPendingIntent(R.id.iv_play_or_pause, playPauseIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_previous, previousIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_next, nextIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_close, closeIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.iv_logo, logoIntent);
        mRemoteViews.setTextViewText(R.id.tv_song_title, mSongsList.get(mCurrentPos).getTitle());


        Bitmap lLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_headset);




        mNotificationBuilder =
                new NotificationCompat.Builder(this, MY_NOTIFICATION_ID)
                        .setContentText("Music")
                        .setContentTitle("Music")
                        .setSmallIcon(R.drawable.ic_headset)
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .setLargeIcon(lLargeIcon)
                        .setContent(mRemoteViews);

        Intent resultIntent = new Intent(this, ListOfSongsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ListOfSongsActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            channel = new NotificationChannel(MY_NOTIFICATION_ID, name, mNotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);


            mNotificationBuilder.setVibrate(new long[0]);

            mNotificationManager.createNotificationChannel(channel);

        }
        mNotificationManager.notify(NOTIFICATION_CODE, mNotificationBuilder.build());


    }
    */

    private void initMediaSession() {

        if (mMediaSessionManager == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
            }
            mMediaSessionCompat = new MediaSessionCompat(this, "MusicPlayer");
            mTransportControls = mMediaSessionCompat.getController().getTransportControls();
            mMediaSessionCompat.setActive(true);
            mMediaSessionCompat.setFlags(FLAG_HANDLES_MEDIA_BUTTONS | FLAG_HANDLES_TRANSPORT_CONTROLS);
            upDateMediaData();

        }


    }

    private void playPauseToView() {
        mBroadCastIntent.putExtra(COMMAND_DATA, COMMAND_PLAY_PAUSE);
        mBroadCastIntent.putExtra(PLAY_PAUSE, mMediaPlayer.isPlaying());
        sendBroadcastToView();
    }

    private void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_CODE);
    }

    private void buildNotification() {

        initMediaSession();
        upDateMediaData();


        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_PLAY_PAUSE_NOTIFICATION).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent previousIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_PREVIOUS).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_NEXT).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent closeIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(COMMAND_STOP).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent shuffle = PendingIntent.getBroadcast(this, 0, new Intent().setAction(SHUFFLE).setClass(this, MyPlayNotificationReceiver.class), 0);
        PendingIntent favorite = PendingIntent.getBroadcast(this, 0, new Intent().setAction(FAVORITE).setClass(this, MyPlayNotificationReceiver.class), 0);

        mIsPlaying = mMediaPlayer.isPlaying();


        Bitmap alBumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);
        Bitmap lTempBitmap = null;
        try {
            Uri lImageUri = getAlbumArtUri(Long.parseLong(mCurrentSongModel.getImagePath()));
            lTempBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), lImageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (lTempBitmap != null) {
            alBumArt = lTempBitmap;
        }

        NotificationCompat.Builder
                lNBuilder = null;

        lNBuilder = new NotificationCompat.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = null;

            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            channel = new NotificationChannel(MY_NOTIFICATION_ID, name, mNotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setLightColor(R.color.colorAccent);

            lNBuilder.setChannelId(MY_NOTIFICATION_ID);
            lNBuilder.setVibrate(new long[0]);
            lNBuilder.setTicker("ticker");
            lNBuilder.setOnlyAlertOnce(true);
            mNotificationManager.createNotificationChannel(channel);

        } else {
            lNBuilder = new NotificationCompat.Builder(this);
        }

        lNBuilder.setShowWhen(false);
        lNBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        lNBuilder.setColor(getResources().getColor(R.color.colorPrimary));
        lNBuilder.setSmallIcon(R.drawable.ic_shuffle);
        lNBuilder.addAction(mIsShuffle ? R.drawable.ic_shuffle_selected : R.drawable.ic_repeat_all, SHUFFLE, shuffle);
        lNBuilder.addAction(R.drawable.ic_skip_previous, COMMAND_PREVIOUS, previousIntent);
        lNBuilder.addAction(mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play, COMMAND_PLAY_PAUSE_NOTIFICATION, playPauseIntent);
        lNBuilder.addAction(R.drawable.ic_skip_next, COMMAND_NEXT, nextIntent);
        lNBuilder.addAction(mIsFavorite ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite_unselected, FAVORITE, favorite);
        lNBuilder.setContentTitle(mCurrentSongModel.getArtist());
        lNBuilder.setContentText(mCurrentSongModel.getTitle());
        lNBuilder.setContentInfo(mCurrentSongModel.getArtist() + " | " + mCurrentSongModel.getTitle());
        lNBuilder.setSubText(mCurrentSongModel.getArtist() + " | " + mCurrentSongModel.getTitle());
        lNBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mMediaSessionCompat.getSessionToken())
                .setShowActionsInCompactView(0, 2, 3)
                .setShowCancelButton(true)
                .setCancelButtonIntent(closeIntent));
        lNBuilder.setAutoCancel(mIsPlaying);
        lNBuilder.setDeleteIntent(closeIntent);
        lNBuilder.setCategory(NotificationCompat.CATEGORY_TRANSPORT);
        lNBuilder.setOngoing(mIsPlaying);
        lNBuilder.setColorized(true);
        lNBuilder.setLargeIcon(alBumArt);
        lNBuilder.setSound(null);
        mNotificationManager.notify(NOTIFICATION_CODE, lNBuilder.build());


    }


    private void upDateMediaData() {

        Bitmap alBumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);
        Bitmap lTempBitmap = null;
        try {
            Uri lImageUri = getAlbumArtUri(Long.parseLong(mCurrentSongModel.getImagePath()));
            lTempBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), lImageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (lTempBitmap != null) {
            alBumArt = lTempBitmap;
        }

        CodeSnipet.showLog(TAG, "is bitmap empty==" + (alBumArt == null ? "yes" : "no"));
        CodeSnipet.showLog(TAG, "Imagepath==" + mCurrentSongModel.getImagePath());
        CodeSnipet.showLog(TAG, "uri==" + getAlbumArtUri(Long.parseLong(mCurrentSongModel.getImagePath())));


        mMediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, alBumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mCurrentSongModel.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mCurrentSongModel.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mCurrentSongModel.getTitle())
                .build());

    }


    public Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMediaPlayer();
    }


}

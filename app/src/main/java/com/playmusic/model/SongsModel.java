package com.playmusic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SongsModel implements Parcelable {

    private String title;
    private String description;
    private String artist;
    private String path;
    private String imagePath;
    private String album;
    private boolean isfavourite;

    public SongsModel(String artist,String title, String path, String imagePath) {
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.imagePath = imagePath;
    }

    protected SongsModel(Parcel in) {
        title = in.readString();
        description = in.readString();
        artist = in.readString();
        path = in.readString();
        imagePath = in.readString();
        album = in.readString();
    }

    public static final Creator<SongsModel> CREATOR = new Creator<SongsModel>() {
        @Override
        public SongsModel createFromParcel(Parcel in) {
            return new SongsModel(in);
        }

        @Override
        public SongsModel[] newArray(int size) {
            return new SongsModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public boolean isIsfavourite() {
        return isfavourite;
    }

    public void setIsfavourite(boolean isfavourite) {
        this.isfavourite = isfavourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(artist);
        parcel.writeString(path);
        parcel.writeString(imagePath);
        parcel.writeString(album);
    }
}

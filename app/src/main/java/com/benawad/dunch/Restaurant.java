package com.benawad.dunch;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Restaurant implements Parcelable{

    private String name;
    private String mainUrl;
    private String picUrl;
    private List<String> pictures;
    private int currPic;
    private String costCat;
    private int page;
    private int iLast;

    public Restaurant(String name, String mainUrl) {
        this.name = name;
        setMainUrl(mainUrl);
        this.currPic = 0;
        this.iLast = 0;
        this.costCat = "";
        this.page = 30;
    }

    protected Restaurant(Parcel in) {
        name = in.readString();
        mainUrl = in.readString();
        picUrl = in.readString();
        pictures = in.createStringArrayList();
        currPic = in.readInt();
        costCat = in.readString();
        page = in.readInt();
        iLast = in.readInt();
    }

    public static final Creator<Restaurant> CREATOR = new Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainUrl() {
        return mainUrl;
    }

    public void setMainUrl(String mainUrl) {
        this.mainUrl = mainUrl;
        this.picUrl = mainUrl.replace("/biz/", "/biz_photos/");
        this.picUrl += "&tab=food";
    }

    public String getPicUrl() {
        return picUrl;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }

    public int getCurrPic() {
        return currPic;
    }

    public void incCurrPic() {
        this.currPic++;
    }

    public void decCurrPic() {
        this.currPic--;
    }

    public String getCostCat() {
        return costCat;
    }

    public void setCostCat(String costCat) {
        this.costCat = costCat;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getiLast() {
        return iLast;
    }

    public void setiLast(int iLast) {
        this.iLast = iLast;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(mainUrl);
        parcel.writeString(picUrl);
        parcel.writeStringList(pictures);
        parcel.writeInt(currPic);
        parcel.writeString(costCat);
        parcel.writeInt(page);
        parcel.writeInt(iLast);
    }
}

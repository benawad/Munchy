package com.benawad.dunch;

import java.util.List;

public class Restaurant {

    private String name;
    private String mainUrl;
    private String picUrl;
    private List<String> pictures;
    private String base = "https://www.yelp.com";
    private int currPic;
    private String costCat;
    private int page;
    private int iLast;

    public Restaurant(String name, String mainUrl) {
        this.name = name;
        setMainUrl(base + mainUrl);
        this.currPic = 0;
        this.iLast = 0;
        this.costCat = "";
        this.page = 30;
    }

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

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
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
}

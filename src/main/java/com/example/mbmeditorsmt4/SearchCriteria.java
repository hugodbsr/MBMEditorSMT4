package com.example.mbmeditorsmt4;

public class SearchCriteria {
    private String tag;
    private String text;

    public SearchCriteria(String tag, String text) {
        this.tag = tag;
        this.text = text;
    }

    public String getTagType() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSearchText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

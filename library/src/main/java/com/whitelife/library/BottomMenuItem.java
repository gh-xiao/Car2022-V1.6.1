package com.whitelife.library;

import android.graphics.drawable.Drawable;


public class BottomMenuItem {

    private int itemId;

    private String title;

    private Drawable icon;


    public BottomMenuItem(int itemId, String title, Drawable icon) {
        this.itemId = itemId;
        this.title = title;
        this.icon = icon;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}

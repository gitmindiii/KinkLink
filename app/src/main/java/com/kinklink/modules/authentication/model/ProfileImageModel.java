package com.kinklink.modules.authentication.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ProfileImageModel {
    public Bitmap profileBitmap;
    public String imageId;
    public String profileUrl;
    public Drawable img;
    public boolean isSelected = false;
    public String profileUrl_thumb;
}

package com.schainan.watchface.font;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * @author Samir Chainani (samir.chainani@gmail.com)
 * @date Sep 21, 2014
 */
public class TypefaceManager {

    private static TypefaceManager sInstance;
    private Typeface mLight, mRegular;


    public static TypefaceManager get() {
        if (sInstance == null) {
            throw new IllegalStateException("Call init() on TypefaceManager at application startup before use.");
        }
        return sInstance;
    }

    public static void init(Context context) {
        sInstance = new TypefaceManager(context);
    }

    private TypefaceManager(Context context) {
        mLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        mRegular = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
    }

    public Typeface getTypefaceRegular() {
        return mRegular;
    }

    private TypefaceManager setTypeface(Typeface typeface, TextView... textViews) {
        for (TextView each : textViews) {
            if (each != null) {
                each.setTypeface(typeface);
            }
        }
        return sInstance;
    }

    public TypefaceManager setTypefaceLight(TextView... textViews) {
        return setTypeface(mLight, textViews);
    }
}

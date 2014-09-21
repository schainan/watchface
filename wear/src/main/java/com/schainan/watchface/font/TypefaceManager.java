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
    private Typeface mLatoLight, mLatoRegular;


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
        mLatoLight = Typeface.createFromAsset(context.getAssets(), "Lato-Light.ttf");
        mLatoRegular = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
    }

    public Typeface getTypefaceLatoLight() {
        return mLatoLight;
    }

    public Typeface getTypefaceLatoRegular() {
        return mLatoRegular;
    }

    private TypefaceManager setTypeface(Typeface typeface, TextView... textViews) {
        for (TextView each : textViews) {
            if (each != null) {
                each.setTypeface(typeface);
            }
        }
        return sInstance;
    }

    public TypefaceManager setTypefaceLatoLight(TextView... textViews) {
        return setTypeface(mLatoLight, textViews);
    }
}

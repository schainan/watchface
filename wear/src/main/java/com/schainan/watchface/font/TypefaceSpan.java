package com.schainan.watchface.font;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

/**
 * @author Samir Chainani (samir.chainani@gmail.com)
 * @date Aug 28, 2014
 */
public class TypefaceSpan extends android.text.style.TypefaceSpan {

    private Typeface mTypeface;

    public TypefaceSpan(Typeface typeface) {
        super("sans-serif");
        mTypeface = typeface;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        applyCustomTypeFace(ds, mTypeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        applyCustomTypeFace(paint, mTypeface);
    }

    private static void applyCustomTypeFace(Paint paint, Typeface tf) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }
}

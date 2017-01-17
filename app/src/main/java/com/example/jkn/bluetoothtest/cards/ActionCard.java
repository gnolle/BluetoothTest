package com.example.jkn.bluetoothtest.cards;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.jkn.bluetoothtest.R;

/**
 * Created by jkn on 13.01.17.
 */

public abstract class ActionCard extends FrameLayout {

    protected String mTextTop;
    protected String mTextBottom;
    protected TextView mTextViewTop;
    protected TextView mTextViewBottom;

    public ActionCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ActionCard,
                0, 0);

        mTextTop = a.getString(R.styleable.ActionCard_textTop);
        mTextBottom = a.getString(R.styleable.ActionCard_textBottom);

        a.recycle();
    }

    public void setTextTop(String textTop) {
        mTextTop = textTop;
        mTextViewTop.setText(mTextTop);
    }

    public void setTextBottom(String textBottom) {
        mTextBottom = textBottom;
        mTextViewBottom.setText(mTextBottom);
    }

    protected void init() {
        setTextTop(mTextTop);
        setTextBottom(mTextBottom);
    }
}

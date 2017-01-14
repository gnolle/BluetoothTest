package com.example.jkn.bluetoothtest;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jkn on 13.01.17.
 */

public class TextActionCard extends ActionCard {

    private String mActionText;
    private TextView mActionTextView;

    public TextActionCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TextActionCard,
                0, 0);

        mActionText = a.getString(R.styleable.TextActionCard_textAction);
        a.recycle();

        inflateLayout();
        init();
    }

    private void inflateLayout() {
        View view = inflate(getContext(), R.layout.action_card_text, this);
        mActionTextView = (TextView) view.findViewById(R.id.action_text);
        mTextViewTop = (TextView) view.findViewById(R.id.text_top);
        mTextViewBottom = (TextView) view.findViewById(R.id.text_bottom);
    }

    @Override
    protected void init() {
        super.init();
        setActionText(mActionText);
    }

    public void setActionText(String actionText) {
        mActionTextView.setText(actionText);
    }
}

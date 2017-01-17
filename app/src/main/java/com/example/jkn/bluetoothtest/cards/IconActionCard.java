package com.example.jkn.bluetoothtest.cards;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jkn.bluetoothtest.R;

/**
 * Created by jkn on 13.01.17.
 */

public class IconActionCard extends ActionCard {

    private Drawable mIcon;
    private ImageView mIconView;

    public IconActionCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.IconActionCard,
                0, 0);

        mIcon = a.getDrawable(R.styleable.IconActionCard_icon);
        a.recycle();

        inflateLayout();
        init();
    }

    private void inflateLayout() {
        View view = inflate(getContext(), R.layout.action_card_icon, this);
        mIconView = (ImageView) view.findViewById(R.id.display_mode_icon);
        mTextViewTop = (TextView) view.findViewById(R.id.text_top);
        mTextViewBottom = (TextView) view.findViewById(R.id.text_bottom);
    }

    @Override
    protected void init() {
        super.init();
        setIcon(mIcon);
    }

    public void setIcon(Drawable icon) {
        mIconView.setImageDrawable(icon);
    }
}

package com.example.jkn.bluetoothtest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

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
    }

    @Override
    protected void inflateLayout() {
        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.action_card_icon, this, true);
        mIconView = (ImageView) view.findViewById(R.id.display_mode_icon);
    }

    @Override
    protected void init() {
        super.init();
        mIconView.setImageDrawable(mIcon);
    }
}

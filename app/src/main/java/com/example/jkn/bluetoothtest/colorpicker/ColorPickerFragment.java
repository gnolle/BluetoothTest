package com.example.jkn.bluetoothtest.colorpicker;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.jkn.bluetoothtest.HSVColor;
import com.example.jkn.bluetoothtest.R;

import java.util.List;

public class ColorPickerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {

    public final static String TAG = ColorPickerFragment.class.getSimpleName();
    final static String PREF_HUE = "PREF_HUE";
    final static String PREF_SATURATION = "PREF_SAT";
    final static String PREF_BRIGHTNESS = "PREF_BRI";

    ColorPickerListener colorPickerListener;

    SeekBar barHue;
    SeekBar barSaturation;
    SeekBar barBrightness;
    View colorField;
    TextView colorName;
    AlphaAnimation fadeIn;
    AlphaAnimation fadeOut;

    int currentColor;
    List<ColorName> colorNameList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorParser colorParser = new ColorParser(getActivity());
        colorNameList = colorParser.getColorsAsList();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            colorPickerListener = (ColorPickerListener) context;
        } catch (ClassCastException ex) {
            dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_color, container, false);

        setViewReferences(view);
        initListeners();
        initAnimations();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        expandDialog();
        initHsvBars();
        calculateCurrentColor();
        updateColorField();
        updateColorName();
    }

    private void expandDialog() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    private void initHsvBars() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        barHue.setProgress(sharedPref.getInt(PREF_HUE, 0));
        barSaturation.setProgress(sharedPref.getInt(PREF_SATURATION, 0));
        barBrightness.setProgress(sharedPref.getInt(PREF_BRIGHTNESS, 0));
    }

    private void initAnimations() {
        fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(100);
        fadeIn.setFillAfter(true);

        fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(100);
        fadeOut.setFillAfter(true);
    }

    private void setViewReferences(View view) {
        barHue = (SeekBar) view.findViewById(R.id.bar_hue);
        barSaturation = (SeekBar) view.findViewById(R.id.bar_saturation);
        barBrightness = (SeekBar) view.findViewById(R.id.bar_brightness);
        colorField = view.findViewById(R.id.color_field);
        colorName = (TextView) view.findViewById(R.id.color_name);
    }

    private void initListeners() {
        barHue.setOnSeekBarChangeListener(this);
        barSaturation.setOnSeekBarChangeListener(this);
        barBrightness.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        calculateCurrentColor();
        updateColorField();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        hideColorName();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateColorName();
        showColorName();
        notifyColorChanged();
    }

    private void notifyColorChanged() {

        int hueValue = barHue.getProgress() * 255 / 100;
        int saturationValue = barSaturation.getProgress() * 255 / 100;
        int brightnessValue = barBrightness.getProgress() * 255 / 100;

        colorPickerListener.onColorPicked(new HSVColor(hueValue, saturationValue, brightnessValue));
    }

    private void updateColorField() {
        colorField.setBackgroundColor(currentColor);
    }

    private void calculateCurrentColor() {
        float hueValue = (float) barHue.getProgress() * 3.6f;
        float saturationValue = (float) barSaturation.getProgress() / 100;
        float brightnessValue = (float) barBrightness.getProgress() / 100;

        currentColor = Color.HSVToColor(new float[] {hueValue, saturationValue, brightnessValue});
    }

    private void hideColorName(){
        colorName.startAnimation(fadeOut);
    }

    private void showColorName(){
        colorName.startAnimation(fadeIn);
    }

    private void updateColorName() {
        colorName.setText(ColorName.findClosestColorName(colorNameList, currentColor).getName());
        updateColorNameColor();
    }

    private void updateColorNameColor() {
        if (ColorName.calculateLuminosity(currentColor) > 0.179)
            colorName.setTextColor(Color.BLACK);
        else
            colorName.setTextColor(Color.WHITE);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_HUE, barHue.getProgress());
        editor.putInt(PREF_SATURATION, barSaturation.getProgress());
        editor.putInt(PREF_BRIGHTNESS, barBrightness.getProgress());
        editor.commit();
    }

    public interface ColorPickerListener {
        void onColorPicked(HSVColor pickedColor);
    }
}

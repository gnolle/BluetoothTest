package com.example.jkn.bluetoothtest.colorpicker;


import android.content.Context;
import android.graphics.Color;

import com.example.jkn.bluetoothtest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan Klostermann on 01.07.2016.
 */
public class ColorParser {

    private Context context;
    private JSONObject colorNames;

    public ColorParser(Context context) {
        this.context = context;
        parseJson();
    }

    private void parseJson() {
        InputStream inputStream = context.getResources().openRawResource(R.raw.colors_names);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder fileText = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                fileText.append(line);
            }
            colorNames = new JSONObject(fileText.toString());
        } catch (Exception e) {
        }
    }

    public List<ColorName> getColorsAsList() {
        List<ColorName> colorNameList = new ArrayList<>();

        try {
            JSONArray colorNamesArray = colorNames.getJSONArray("colorNames");

            for (int i = 0; i < colorNamesArray.length(); i++) {

                JSONArray colorNameArray = colorNamesArray.getJSONArray(i);
                ColorName colorName = new ColorName();
                colorName.setHexColor("#" + colorNameArray.getString(0));
                colorName.setName(colorNameArray.getString(1));
                colorName.setColor(Color.parseColor(colorName.getHexColor()));

                colorNameList.add(colorName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return colorNameList;
    }

}

package me.camdora.dorapanoimageviewer.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by camdora on 17-11-13.
 */

public class TextResourceReader {
    private static final String TAG = "TextResourceReader";

    public static String readTextFileFromResource(Context context,
                                                  int resourceId){
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);

            BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null){
                body.append(nextLine);
                body.append('\n');
            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return body.toString();
    }
}

package com.example.aplikasita.lib;

/**
 * Created by iisaev on 16/04/16.
 */

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public static void make(Context context, int text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void make(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

}
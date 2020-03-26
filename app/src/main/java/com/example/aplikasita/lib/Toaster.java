package com.example.aplikasita.lib;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public static void make(Context context, int text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void make(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}

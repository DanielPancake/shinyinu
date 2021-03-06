package net.danielpancake.shinyinu;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

/*
    I didn't figure out how to extend BaseTransientBottomBar class
        so I created this instead...

    Author: danielpancake
*/

public class CustomSnackbar {
    static Snackbar make(View view, String text, Drawable icon, int duration, String action, final View.OnClickListener listener) {
        // Make default snackbar
        final Snackbar snackbar = Snackbar.make(view, "", duration);
        // and make its layout invisible
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        // Inflate our custom layout
        View custom = LayoutInflater.from(view.getContext()).inflate(R.layout.custom_snackbar, null);
        ((TextView) custom.findViewById(R.id.snackbar_text)).setText(text);
        ((ImageView) custom.findViewById(R.id.snackbar_icon)).setImageDrawable(icon);
        Button actionButton = custom.findViewById(R.id.snackbar_action_button);

        if (action == null) {
            actionButton.setVisibility(View.GONE);
        } else {
            actionButton.setText(action);
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                    snackbar.dismiss();
                }
            });
        }

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);

        snackbarLayout.addView(custom, 0);

        return snackbar;
    }
}
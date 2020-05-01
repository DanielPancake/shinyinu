package net.danielpancake.shinyinu;

/*

    This class loads Shiba's image from shibe.online

*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;

public class ShibaLoader extends AsyncTask<Void, Void, Shiba> {

    private Context context;
    private View view;
    private ImageView imageView;
    private Button button;
    private ProgressBar progressBar;

    ShibaLoader(Context context, View view, ImageView imageView, Button button, ProgressBar progressBar) {

        // We import these to change UI in AsyncTask
        this.context = context;
        this.view = view;
        this.imageView = imageView;
        this.button = button;
        this.progressBar = progressBar;

        // Don't click me!
        // Please, just wait until image's loaded
        button.setEnabled(false);
        button.setText(context.getResources().getText(R.string.app_loading));

        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected Shiba doInBackground(Void[] nothing) {

        String JSON = "";
        Bitmap bitmap = null;
        String shibacode = null;

        // Collect information about Internet access
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            try {
                // We use https to prevent errors on android API 25+ (not sure)
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        new URL("https://shibe.online/api/shibes?count=1&urls=false").openStream()));

                // Get JSON from the link above
                String inputLine = "";

                while (inputLine != null) {
                    JSON += inputLine;
                    inputLine = bufferedReader.readLine();
                }

                // If app gets here (no error occurred), we'll get the image code
                shibacode = new JSONArray(JSON).getString(0);

                // Now use it to load the actual image and then pass the image on...
                bitmap = BitmapFactory.decodeStream(
                        new URL("https://cdn.shibe.online/shibes/" + shibacode + ".jpg").openStream());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        } else {
            // If there's no internet connection, load one of the saved images
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu");
            File[] imagesList = directory.listFiles();

            if (imagesList.length > 0) {
                Random random = new Random();
                File image = imagesList[random.nextInt(imagesList.length)];

                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                shibacode = image.getName();
            }
        }

        // ...there
        return new Shiba(shibacode, bitmap);
    }

    @Override
    protected void onPostExecute(Shiba result) {

        // Okay, we're through. Now give me a moment to catch my breath
        // And then you'll be able click me again!
        button.setText(context.getResources().getText(R.string.button_shiny));

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        Handler delay = new Handler();

        delay.postDelayed(new Runnable() {
            public void run() {
                button.setEnabled(true);
            }
        }, 500);

        // If all is right, set the image
        if (result.bitmap != null) {
            imageView.setImageBitmap(result.bitmap);

            CustomSnackbar.make(view, "Woof!", view.getResources().getDrawable(R.drawable.ic_shiba_status), Snackbar.LENGTH_LONG).show();
        }
    }
}




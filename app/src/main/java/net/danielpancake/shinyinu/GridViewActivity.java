package net.danielpancake.shinyinu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class GridViewActivity extends BasicActivity {

    private ImageAdapter gridAdapter;

    private PhotoView photoViewer;
    private View photoViewerContainer;

    private ActionBar actionBar;
    private View buttonBack;

    private boolean buttonBackIsShowm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        // Set up toolbars
        Toolbar toolbar = findViewById(R.id.toolbar).findViewById(R.id.actual_toolbar);
        toolbar.setSubtitle("> " + getString(R.string.title_bookmarked));
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        // Set up database
        DBHelper dbHelper = new DBHelper(this);

        // Set up home buttons
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        buttonBack = findViewById(R.id.button_back);

        // Get screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Create photo viewer
        photoViewerContainer = findViewById(R.id.photo_viewer_container);
        photoViewer = findViewById(R.id.photo_viewer);

        // Create grid view
        GridView gridView = findViewById(R.id.grid_view);
        gridAdapter = new ImageAdapter(this, dbHelper, metrics.widthPixels);
        gridView.setAdapter(gridAdapter);

        // If there's nothing to show, put an image of sleeping Shiba
        final View nothingToShow = findViewById(R.id.nothing_here);
        if (gridAdapter.getCount() > 0) {
            nothingToShow.setVisibility(View.INVISIBLE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    if (photoViewerContainer.getVisibility() == View.INVISIBLE) {
                        String code = gridAdapter.getItem(position);

                        if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                "/ShinyInu/" + code + ".jpg").exists()) {

                            ImageView imageView = view.findViewById(position);
                            ImageLoader imageLoader = new ImageLoader(view.getContext(), view, imageView, false);
                            imageLoader.execute("[\"" + code + "\"]");
                        } else {
                            PhotoViewerLoader photoViewerLoader = new PhotoViewerLoader(photoViewer);
                            photoViewerLoader.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                    "/ShinyInu/" + code + ".jpg");

                            setPhotoViewerVisible();
                        }
                    }
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                final String code = gridAdapter.getItem(position);

                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.gallery_menu, popupMenu.getMenu());

                int[] icons = {R.drawable.ic_share, R.drawable.ic_remove, R.drawable.ic_delete};

                for (int i = 0; i < icons.length; i++) {

                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableStringBuilder newMenuTitle = new SpannableStringBuilder("*   " + item.getTitle());
                    newMenuTitle.setSpan(new CenteredImageSpan(view.getContext(), icons[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    item.setTitle(newMenuTitle);

                }

                if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                        "/ShinyInu/" + code + ".jpg").exists()) {
                    popupMenu.getMenu().getItem(0).setEnabled(false);
                    popupMenu.getMenu().getItem(1).setEnabled(false);
                }

                MenuItem item = popupMenu.getMenu().getItem(2);
                SpannableStringBuilder newMenuTitle = new SpannableStringBuilder(item.getTitle());
                newMenuTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                newMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.design_default_color_error)), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                item.setTitle(newMenuTitle);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.option_share:
                                File shared_image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                        "/ShinyInu/" + code + ".jpg");

                                Uri contentUri = Uri.parse("file://" + shared_image);
                                Intent share = new Intent(Intent.ACTION_SEND);

                                share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                share.putExtra(Intent.EXTRA_STREAM, contentUri);
                                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
                                share.setType("image/jpg");

                                startActivity(Intent.createChooser(share, getString(R.string.share_title)));
                                break;

                            case R.id.option_remove:
                                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                                    gridAdapter.removeItem(position);
                                }
                                break;

                            case R.id.option_delete:
                                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                                    new AlertDialog.Builder(view.getContext())
                                            .setMessage(R.string.are_you_sure)
                                            .setPositiveButton(R.string.option_yes, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    gridAdapter.deleteItem(position);

                                                    if (gridAdapter.getCount() == 0) {
                                                        nothingToShow.setVisibility(View.VISIBLE);
                                                    }
                                                }

                                            }).setNegativeButton(R.string.option_no, null).show();
                                }
                                break;
                        }

                        return true;
                    }
                });

                popupMenu.show();
                return true;
            }
        });

        photoViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonBackIsShowm) {
                    buttonBackHide();
                } else {
                    buttonBackShow();
                }
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPhotoViewerInvisible();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            MainActivity.memoryCache.removeAllFromMemoryCache();
            gridAdapter.notifyDataSetChanged();
        }
    }

    void setPhotoViewerInvisible() {
        actionBar.show();
        photoViewerContainer.animate().alpha(0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                photoViewerContainer.setVisibility(View.INVISIBLE);
                buttonBackHide();
                photoViewer.setImageBitmap(null);
            }
        });
    }

    void setPhotoViewerVisible() {
        photoViewerContainer.setVisibility(View.VISIBLE);
        photoViewerContainer.animate().alpha(1f).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                photoViewerContainer.setVisibility(View.VISIBLE);
                buttonBackShow();
                actionBar.hide();
            }
        });
    }

    void buttonBackShow() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, -((View) buttonBack.getParent()).getHeight(), 0);
        animation.setDuration(500);
        animation.setFillAfter(true);

        if (actionBar.isShowing()) {
            animation.setStartOffset(1000);
        }

        buttonBack.startAnimation(animation);
        buttonBackIsShowm = true;
    }

    void buttonBackHide() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -((View) buttonBack.getParent()).getHeight());
        animation.setDuration(500);
        animation.setFillAfter(true);

        buttonBack.startAnimation(animation);
        buttonBackIsShowm = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (photoViewerContainer.getVisibility() == View.VISIBLE) {
            getMenuInflater().inflate(R.menu.menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (photoViewerContainer.getVisibility() == View.VISIBLE) {
            setPhotoViewerInvisible();
        } else {
            finish();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (photoViewerContainer.getVisibility() == View.VISIBLE) {
            setPhotoViewerInvisible();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
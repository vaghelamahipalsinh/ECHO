package com.echo.music;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;
import java.util.ArrayList;

public class MainActivity extends Activity {

    LinearLayout layout;
    ArrayList<Uri> songs = new ArrayList<>();
    MediaPlayer player;
    TextView nowPlaying;
    Button playButton;
    int currentSong = -1;

    int bg = Color.rgb(10, 10, 18);
    int blue = Color.rgb(0, 180, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(20, 25, 20, 20);
        layout.setBackgroundColor(bg);

        TextView logo = new TextView(this);
        logo.setText("ECHO 🎧");
        logo.setTextSize(32);
        logo.setTextColor(blue);
        logo.setTypeface(null, 1);
        layout.addView(logo);

        TextView heading = new TextView(this);
        heading.setText("YOUR MUSIC. YOUR WORLD.");
        heading.setTextColor(Color.LTGRAY);
        layout.addView(heading);

        nowPlaying = new TextView(this);
        nowPlaying.setText("No song playing");
        nowPlaying.setTextColor(Color.WHITE);
        nowPlaying.setTextSize(18);
        nowPlaying.setPadding(0, 30, 0, 15);
        layout.addView(nowPlaying);

        playButton = new Button(this);
        playButton.setText("PAUSE");
        playButton.setOnClickListener(v -> togglePlay());
        layout.addView(playButton);
Button previousButton = new Button(this);
previousButton.setText("⏮ PREVIOUS");
previousButton.setOnClickListener(v -> previousSong());
layout.addView(previousButton);

Button nextButton = new Button(this);
nextButton.setText("NEXT ⏭");
nextButton.setOnClickListener(v -> nextSong());
layout.addView(nextButton);
        TextView title = new TextView(this);
        title.setText("MY MUSIC");
        title.setTextColor(blue);
        title.setTextSize(23);
        title.setPadding(0, 30, 0, 15);
        layout.addView(title);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        setContentView(scroll);

        checkPermission();
    }

    void checkPermission() {
        String permission;

        if (Build.VERSION.SDK_INT >= 33) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            requestPermissions(new String[]{permission}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == 100 && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(this, "Music permission required",
                    Toast.LENGTH_LONG).show();
        }
    }

    void loadSongs() {
        songs.clear();

        String[] projection = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null,
            MediaStore.Audio.Media.TITLE + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String artist = cursor.getString(2);

                Uri uri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(id)
                );

                songs.add(uri);

                TextView song = new TextView(this);
                song.setText("🎵 " + name + "\n" + artist);
                song.setTextColor(Color.WHITE);
                song.setTextSize(17);
                song.setPadding(15, 18, 15, 18);
                song.setBackgroundColor(Color.rgb(24, 24, 36));

                final int index = songs.size() - 1;
                song.setOnClickListener(v -> playSong(index));

                LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-1, -2);
                params.setMargins(0, 5, 0, 5);

                layout.addView(song, params);
            }
            cursor.close();
        }

        if (songs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No music found on your phone.");
            empty.setTextColor(Color.WHITE);
            layout.addView(empty);
        }
    }

    void playSong(int index) {
        try {
            if (player != null) {
                player.release();
                player = null;
            }

            player = new MediaPlayer();
            player.setDataSource(this, songs.get(index));
            player.setOnCompletionListener(mp -> {
                int next = currentSong + 1;
                if (next < songs.size()) {
                    playSong(next);
                }
            });

            player.prepare();
            player.start();

            currentSong = index;
            nowPlaying.setText("🎵 Playing song " + (index + 1));
            playButton.setText("PAUSE");

        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this song",
                    Toast.LENGTH_SHORT).show();
        }
    }

void nextSong() {
    if (currentSong + 1 < songs.size()) {
        playSong(currentSong + 1);
    }
}

void previousSong() {
    if (currentSong > 0) {
        playSong(currentSong - 1);
    }
}
    void togglePlay() {
        if (player == null) return;

        if (player.isPlaying()) {
            player.pause();
            playButton.setText("PLAY");
        } else {
            player.start();
            playButton.setText("PAUSE");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (player != null) {
            player.release();
            player = null;
        }
    }
}

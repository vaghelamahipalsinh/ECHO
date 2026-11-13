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
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import java.util.ArrayList;
import android.text.TextWatcher;
import android.text.Editable;
public class MainActivity extends Activity {

    LinearLayout layout;
    ArrayList<Uri> songs = new ArrayList<>();
ArrayList<TextView> songViews = new ArrayList<>();
    MediaPlayer player;
    TextView nowPlaying;
    Button playButton;
    int currentSong = -1;
boolean isShuffle = false;
boolean isRepeat = false;
SeekBar seekBar;
TextView currentTime, totalTime;
android.os.Handler handler = new android.os.Handler();
Runnable progressUpdater;
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
        heading.setText("YOUR MUSIC. YOUR WORLD.")
;
        EditText searchBar = new EditText(this);
        searchBar.setHint("🔍 Search your music...");
        searchBar.setHintTextColor(Color.GRAY);
        searchBar.setTextColor(Color.WHITE);
        searchBar.setSingleLine(true);
        searchBar.setPadding(20, 15, 20, 15);

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.rgb(28, 28, 42));
        searchBg.setCornerRadius(30);
        searchBar.setBackground(searchBg);

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, -2);
        searchParams.setMargins(0, 20, 0, 20);
        layout.addView(searchBar, searchParams);
searchBar.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String query = s.toString().toLowerCase().trim();

        for (TextView song : songViews) {
            boolean match = song.getText().toString()
                    .toLowerCase().contains(query);

            song.setVisibility(match ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
});
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
Button shuffleButton = new Button(this);
shuffleButton.setText("SHUFFLE OFF");
shuffleButton.setOnClickListener(v -> {
    isShuffle = !isShuffle;
    shuffleButton.setText(isShuffle ? "SHUFFLE ON" : "SHUFFLE OFF");
});
layout.addView(shuffleButton);

Button repeatButton = new Button(this);
repeatButton.setText("REPEAT OFF");
repeatButton.setOnClickListener(v -> {
    isRepeat = !isRepeat;
    repeatButton.setText(isRepeat ? "REPEAT ON" : "REPEAT OFF");
});
layout.addView(repeatButton);
currentTime = new TextView(this);
currentTime.setText("0:00");
currentTime.setTextColor(Color.WHITE);
layout.addView(currentTime);

seekBar = new SeekBar(this);
layout.addView(seekBar);

totalTime = new TextView(this);
totalTime.setText("0:00");
totalTime.setTextColor(Color.WHITE);
layout.addView(totalTime);        TextView title = new TextView(this);
seekBar.setOnSeekBarChangeListener(
    new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(
                SeekBar bar, int progress, boolean fromUser) {
            if (fromUser && player != null) {
                player.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar bar) {}

        @Override
        public void onStopTrackingTouch(SeekBar bar) {}
    }
);        title.setText("MY MUSIC");
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
                GradientDrawable card = new GradientDrawable();
                card.setColor(Color.rgb(28, 28, 42));
                card.setCornerRadius(22);
                song.setBackground(card);
                song.setElevation(3);

                final int index = songs.size() - 1;
                song.setOnClickListener(v -> playSong(index));
songViews.add(song);

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
if (isRepeat) {
    playSong(currentSong);
} else if (isShuffle && songs.size() > 1) {
    int randomIndex;
    do {
        randomIndex = (int) (Math.random() * songs.size());
    } while (randomIndex == currentSong);
    playSong(randomIndex);
} else if (currentSong + 1 < songs.size()) {
    playSong(currentSong + 1);
}            });

            player.prepare();
            player.start();
seekBar.setMax(player.getDuration());
totalTime.setText(formatTime(player.getDuration()));

progressUpdater = new Runnable() {
    @Override
    public void run() {
        if (player != null && player.isPlaying()) {
            seekBar.setProgress(player.getCurrentPosition());
            currentTime.setText(formatTime(player.getCurrentPosition()));
        }
        handler.postDelayed(this, 500);
    }
};
handler.removeCallbacks(progressUpdater);
handler.post(progressUpdater);
            currentSong = index;
            nowPlaying.setText("🎵 Playing song " + (index + 1));
            playButton.setText("PAUSE");

        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this song",
                    Toast.LENGTH_SHORT).show();
        }
    }

String formatTime(int milliseconds) {
    int minutes = milliseconds / 60000;
    int seconds = (milliseconds / 1000) % 60;
    return String.format("%d:%02d", minutes, seconds);
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
    if (progressUpdater != null) {
    handler.removeCallbacks(progressUpdater);
}    super.onDestroy();

        if (player != null) {
            player.release();
            player = null;
        }
    }
}

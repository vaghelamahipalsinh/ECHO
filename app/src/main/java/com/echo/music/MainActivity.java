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
import java.util.HashSet;
import java.util.Set;
import android.text.TextWatcher;
import android.text.Editable;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
public class MainActivity extends Activity {
ArrayList<String> onlineUrls = new ArrayList<>();
ArrayList<String> onlineTitles = new ArrayList<>();
ArrayList<String> onlineArtists = new ArrayList<>();
Set<String> favoriteSongs = new HashSet<>();
LinearLayout onlineResultsLayout;
EditText onlineSearchInput;
    LinearLayout layout;
    ArrayList<Uri> songs = new ArrayList<>();
ArrayList<TextView> songViews = new ArrayList<>();
    MediaPlayer player;
    TextView nowPlaying;
TextView miniNowPlaying;
Button miniPlayButton;
TextView playerSongTitle;
TextView playerArtist;
ImageView albumArt;
android.app.Dialog nowPlayingDialog;
SeekBar dialogSeekBar;
TextView dialogCurrentTime;
TextView dialogTotalTime;
    Button playButton;
    int currentSong = -1;
boolean isShuffle = false;
boolean favoritesOnly = false;
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
Button favoritesFilter = new Button(this);
favoritesFilter.setText("❤️ SHOW FAVORITES");

favoritesFilter.setOnClickListener(v -> {
    favoritesOnly = !favoritesOnly;

    favoritesFilter.setText(
        favoritesOnly ? "🎵 SHOW ALL SONGS" : "❤️ SHOW FAVORITES"
    );

    String query = searchBar.getText().toString().toLowerCase().trim();

    for (int i = 0; i < songViews.size(); i++) {
        boolean match = songViews.get(i).getText()
            .toString().toLowerCase().contains(query);

        boolean isFavorite = favoriteSongs.contains(songs.get(i).toString());

        songViews.get(i).setVisibility(
            match && (!favoritesOnly || isFavorite)
                ? View.VISIBLE : View.GONE
        );
    }
});

layout.addView(favoritesFilter);
searchBar.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String query = s.toString().toLowerCase().trim();

        for (int i = 0; i < songViews.size(); i++) {
            TextView song = songViews.get(i);

            boolean match = song.getText().toString()
                .toLowerCase().contains(query);

            boolean isFavorite = favoriteSongs.contains(
                songs.get(i).toString()
            );

            song.setVisibility(
                match && (!favoritesOnly || isFavorite)
                    ? View.VISIBLE : View.GONE
            );
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}
});

TextView onlineHeading = new TextView(this);
onlineHeading.setText("🌐 ONLINE MUSIC");
onlineHeading.setTextColor(blue);
onlineHeading.setTextSize(22);
onlineHeading.setPadding(0, 25, 0, 15);
layout.addView(onlineHeading);

onlineSearchInput = new EditText(this);
onlineSearchInput.setHint("Search online songs...");
onlineSearchInput.setTextColor(Color.WHITE);
onlineSearchInput.setHintTextColor(Color.GRAY);
onlineSearchInput.setSingleLine(true);
layout.addView(onlineSearchInput);

Button onlineSearchButton = new Button(this);
onlineSearchButton.setText("SEARCH ONLINE");
onlineSearchButton.setOnClickListener(v -> searchOnlineMusic());
layout.addView(onlineSearchButton);

onlineResultsLayout = new LinearLayout(this);
onlineResultsLayout.setOrientation(LinearLayout.VERTICAL);
layout.addView(onlineResultsLayout);
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

LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setBackgroundColor(bg);

root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

LinearLayout miniPlayer = new LinearLayout(this);
miniPlayer.setOrientation(LinearLayout.HORIZONTAL);
miniPlayer.setGravity(Gravity.CENTER_VERTICAL);
miniPlayer.setPadding(16, 8, 16, 8);
miniPlayer.setBackgroundColor(Color.rgb(24, 24, 36));
miniPlayer.setOnClickListener(v -> showNowPlaying());
miniNowPlaying = new TextView(this);
miniNowPlaying.setText("No song playing");
miniNowPlaying.setTextColor(Color.WHITE);
miniNowPlaying.setTextSize(14);
miniNowPlaying.setSingleLine(true);
miniNowPlaying.setEllipsize(android.text.TextUtils.TruncateAt.END);

LinearLayout.LayoutParams miniTextParams =
        new LinearLayout.LayoutParams(0, -2, 1f);
miniPlayer.addView(miniNowPlaying, miniTextParams);

Button miniPrevious = new Button(this);
miniPrevious.setText("⏮");
miniPrevious.setOnClickListener(v -> previousSong());
miniPlayer.addView(miniPrevious);

miniPlayButton = new Button(this);
miniPlayButton.setText("▶");
miniPlayButton.setOnClickListener(v -> togglePlay());
miniPlayer.addView(miniPlayButton);

Button miniNext = new Button(this);
miniNext.setText("⏭");
miniNext.setOnClickListener(v -> nextSong());
miniPlayer.addView(miniNext);

root.addView(miniPlayer, new LinearLayout.LayoutParams(-1, 72));

setContentView(root);
loadFavorites();
checkPermission();    }
void loadFavorites() {
    android.content.SharedPreferences prefs =
        getSharedPreferences("ECHO_FAVORITES", MODE_PRIVATE);

    favoriteSongs.clear();
    favoriteSongs.addAll(
        prefs.getStringSet("favorites", new HashSet<>())
    );
}

void saveFavorites() {
    android.content.SharedPreferences prefs =
        getSharedPreferences("ECHO_FAVORITES", MODE_PRIVATE);

    prefs.edit()
        .putStringSet("favorites", new HashSet<>(favoriteSongs))
        .apply();
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

                Button favoriteButton = new Button(this);
                String songKey = uri.toString();

                favoriteButton.setText(
                    favoriteSongs.contains(songKey) ? "❤️ FAVORITED" : "♡ FAVORITE"
                );

                favoriteButton.setOnClickListener(v -> {
                    if (favoriteSongs.contains(songKey)) {
                        favoriteSongs.remove(songKey);
                        favoriteButton.setText("♡ FAVORITE");
                    } else {
                        favoriteSongs.add(songKey);
                        favoriteButton.setText("❤️ FAVORITED");
                    }
                    saveFavorites();
                });

                layout.addView(favoriteButton);
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
        }if (dialogSeekBar != null) {
    dialogSeekBar.setMax(player.getDuration());
    dialogSeekBar.setProgress(player.getCurrentPosition());
}
if (dialogCurrentTime != null) {
    dialogCurrentTime.setText(formatTime(player.getCurrentPosition()));
}
if (dialogTotalTime != null) {
    dialogTotalTime.setText(formatTime(player.getDuration()));
}
        handler.postDelayed(this, 500);
    }
};
handler.removeCallbacks(progressUpdater);
handler.post(progressUpdater);
            currentSong = index;
updateSongInfo(index);
            nowPlaying.setText("🎵 Playing song " + (index + 1));
            playButton.setText("PAUSE");
miniNowPlaying.setText("🎵 Playing song " + (index + 1));
miniPlayButton.setText("Ⅱ");
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play this song",
                    Toast.LENGTH_SHORT).show();
}        }
void updateSongInfo(int index) {
    String title = "Unknown title";
    String artist = "Unknown artist";
    long albumId = -1;

    try {
        String[] columns = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cursor = getContentResolver().query(
            songs.get(index),
            columns, null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                title = cursor.getString(0);
                artist = cursor.getString(1);
                albumId = cursor.getLong(2);
            }
            cursor.close();
        }
    } catch (Exception e) {
        Toast.makeText(this, "Song details unavailable",
                Toast.LENGTH_SHORT).show();
    }

    if (playerSongTitle != null) {
        playerSongTitle.setText(title);
    }

    if (playerArtist != null) {
        playerArtist.setText(artist);
    }

    if (albumArt != null) {
        try {
            Uri artUri = android.content.ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            );
            albumArt.setImageURI(artUri);
        } catch (Exception e) {
            albumArt.setImageResource(
                android.R.drawable.ic_media_play);
        }
    }

    nowPlaying.setText("🎵 " + title);
    miniNowPlaying.setText("🎵 " + title);
}
void showNowPlaying() {
    if (currentSong < 0 || songs.isEmpty()) {
        Toast.makeText(this, "Play a song first",
                Toast.LENGTH_SHORT).show();
        return;
    }

    android.app.Dialog dialog = new android.app.Dialog(this);

    LinearLayout screen = new LinearLayout(this);
    screen.setOrientation(LinearLayout.VERTICAL);
    screen.setGravity(Gravity.CENTER);
    screen.setPadding(25, 20, 25, 20);
    screen.setBackgroundColor(Color.rgb(10, 10, 18));

    Button close = new Button(this);
    close.setText("↓ CLOSE");
    close.setOnClickListener(v -> dialog.dismiss());
    screen.addView(close);

    albumArt = new ImageView(this);
    albumArt.setImageResource(android.R.drawable.ic_media_play);
    albumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);

    LinearLayout.LayoutParams artParams =
            new LinearLayout.LayoutParams(280, 280);
    artParams.setMargins(0, 25, 0, 25);
    screen.addView(albumArt, artParams);

    playerSongTitle = new TextView(this);
    playerSongTitle.setText("ECHO MUSIC");
    playerSongTitle.setTextColor(Color.WHITE);
    playerSongTitle.setTextSize(23);
    playerSongTitle.setGravity(Gravity.CENTER);
    screen.addView(playerSongTitle);

    playerArtist = new TextView(this);
    playerArtist.setText("Now Playing");
    playerArtist.setTextColor(blue);
    playerArtist.setTextSize(16);
    playerArtist.setGravity(Gravity.CENTER);
    screen.addView(playerArtist);

    dialogSeekBar = new SeekBar(this);
    screen.addView(dialogSeekBar);
dialogSeekBar.setOnSeekBarChangeListener(
    new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar bar,
                int progress, boolean fromUser) {
            if (fromUser && dialogCurrentTime != null) {
                dialogCurrentTime.setText(formatTime(progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar bar) {}

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            if (player != null) {
                player.seekTo(bar.getProgress());
            }
        }
    }
);
    LinearLayout times = new LinearLayout(this);
    times.setOrientation(LinearLayout.HORIZONTAL);

    dialogCurrentTime = new TextView(this);
    dialogCurrentTime.setText("0:00");
    dialogCurrentTime.setTextColor(Color.WHITE);

    dialogTotalTime = new TextView(this);
    dialogTotalTime.setText("0:00");
    dialogTotalTime.setTextColor(Color.WHITE);
    dialogTotalTime.setGravity(Gravity.RIGHT);

    times.addView(dialogCurrentTime,
            new LinearLayout.LayoutParams(0, -2, 1));
    times.addView(dialogTotalTime,
            new LinearLayout.LayoutParams(0, -2, 1));
    screen.addView(times);

    LinearLayout controls = new LinearLayout(this);
    controls.setGravity(Gravity.CENTER);

    Button previous = new Button(this);
    previous.setText("⏮");
    previous.setOnClickListener(v -> previousSong());

    Button play = new Button(this);
    play.setText("▶ / Ⅱ");
    play.setOnClickListener(v -> togglePlay());

    Button next = new Button(this);
    next.setText("⏭");
    next.setOnClickListener(v -> nextSong());

    controls.addView(previous);
    controls.addView(play);
    controls.addView(next);
    screen.addView(controls);
LinearLayout modes = new LinearLayout(this);
modes.setGravity(Gravity.CENTER);

Button shuffleButton = new Button(this);
shuffleButton.setText(isShuffle ? "SHUFFLE ON" : "SHUFFLE OFF");
shuffleButton.setOnClickListener(v -> {
    isShuffle = !isShuffle;
    shuffleButton.setText(isShuffle ? "SHUFFLE ON" : "SHUFFLE OFF");
});

Button repeatButton = new Button(this);
repeatButton.setText(isRepeat ? "REPEAT ON" : "REPEAT OFF");
repeatButton.setOnClickListener(v -> {
    isRepeat = !isRepeat;
    repeatButton.setText(isRepeat ? "REPEAT ON" : "REPEAT OFF");
});

modes.addView(shuffleButton);
modes.addView(repeatButton);
screen.addView(modes);
    dialog.setContentView(screen);
    dialog.show();
    dialog.getWindow().setLayout(-1, -1);

    nowPlayingDialog = dialog;
updateSongInfo(currentSong);
}
// ECHO 7.0 NOW PLAYING SCREEN
void searchOnlineMusic() {
    String query = onlineSearchInput.getText().toString().trim();

    if (query.isEmpty()) {
        Toast.makeText(this, "Enter song name", Toast.LENGTH_SHORT).show();
        return;
    }

    onlineResultsLayout.removeAllViews();

    TextView loading = new TextView(this);
    loading.setText("Searching online music...");
    loading.setTextColor(Color.WHITE);
    onlineResultsLayout.addView(loading);

    new Thread(() -> {
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> artists = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();

        try {
            String encoded = URLEncoder.encode(query, "UTF-8");

            URL url = new URL(
                "https://itunes.apple.com/search?term="
                + encoded + "&entity=song&limit=25"
            );

            HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            JSONObject json = new JSONObject(response.toString());
            JSONArray results = json.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject song = results.getJSONObject(i);

                if (!song.has("previewUrl")) continue;

                titles.add(song.getString("trackName"));
                artists.add(song.getString("artistName"));
                urls.add(song.getString("previewUrl"));
            }

        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(
                this, "Search failed. Check internet.",
                Toast.LENGTH_LONG
            ).show());
        }

        runOnUiThread(() -> {
            onlineResultsLayout.removeAllViews();

            onlineTitles.clear();
            onlineArtists.clear();
            onlineUrls.clear();

            onlineTitles.addAll(titles);
            onlineArtists.addAll(artists);
            onlineUrls.addAll(urls);

            for (int i = 0; i < onlineTitles.size(); i++) {
                final int index = i;

                TextView item = new TextView(this);
                item.setText("🎵 " + onlineTitles.get(i)
                    + "\n" + onlineArtists.get(i)
                    + "\n▶ PLAY PREVIEW");

                item.setTextColor(Color.WHITE);
                item.setTextSize(16);
                item.setPadding(18, 18, 18, 18);

                item.setOnClickListener(v ->
                    playOnlinePreview(index)
                );

                onlineResultsLayout.addView(item);
            }

            if (onlineTitles.isEmpty()) {
                TextView empty = new TextView(this);
                empty.setText("No preview songs found.");
                empty.setTextColor(Color.WHITE);
                onlineResultsLayout.addView(empty);
            }
        });
    }).start();
}

void playOnlinePreview(int index) {
    try {
        if (player != null) {
            player.release();
        }

        currentSong = -1;
        player = new MediaPlayer();

        player.setDataSource(onlineUrls.get(index));

        player.setOnPreparedListener(mp -> {
            mp.start();

            String title = onlineTitles.get(index);
            String artist = onlineArtists.get(index);

            nowPlaying.setText("🎵 " + title);
            miniNowPlaying.setText("🎵 " + title);

            playButton.setText("PAUSE");
            miniPlayButton.setText("Ⅱ");

            Toast.makeText(this, "Playing preview",
                Toast.LENGTH_SHORT).show();
        });

        player.prepareAsync();

    } catch (Exception e) {
        Toast.makeText(this, "Cannot play preview",
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
            miniPlayButton.setText("▶");
        } else {
            player.start();
            playButton.setText("PAUSE");
            miniPlayButton.setText("Ⅱ");
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

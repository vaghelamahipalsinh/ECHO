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
import java.util.HashMap;
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
ArrayList<String> audiusTrackIds = new ArrayList<>();
ArrayList<String> onlineTitles = new ArrayList<>();
ArrayList<String> onlineArtists = new ArrayList<>();
ArrayList<String> onlineArtwork = new ArrayList<>();
HashMap<String, String> savedTrackTitles = new HashMap<>();
HashMap<String, String> savedTrackArtists = new HashMap<>();
HashMap<String, String> savedTrackArtwork = new HashMap<>();
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
boolean isSeeking = false;
TextView dialogCurrentTime;
TextView dialogTotalTime;
    Button playButton;
    int currentSong = -1;
boolean isAudiusPlaying = false;
int currentAudiusIndex = -1;
boolean isShuffle = false;
boolean favoritesOnly = false;
ArrayList<String> playlistNames = new ArrayList<>();
ArrayList<ArrayList<String>> playlists = new ArrayList<>();
boolean isRepeat = false;
SeekBar seekBar;
TextView currentTime, totalTime;
android.os.Handler handler = new android.os.Handler();
Runnable progressUpdater;
    int bg = Color.rgb(18, 18, 18);
    int blue = Color.rgb(0, 180, 255);
    int green = Color.rgb(30, 215, 96);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTheme();

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(20, 25, 20, 20);
        layout.setBackgroundColor(bg);

        TextView logo = new TextView(this);
        logo.setText("ECHO 🎧");
        logo.setTextSize(34);
        logo.setTextColor(green);
        logo.setPadding(0, 10, 0, 8);
        logo.setTypeface(null, 1);
        layout.addView(logo);

        TextView heading = new TextView(this);
        heading.setText("YOUR MUSIC. YOUR WORLD.");
        heading.setTextSize(16);
        heading.setTextColor(Color.LTGRAY);
        heading.setTypeface(null, 1);
        heading.setPadding(0, 5, 0, 15);
        EditText searchBar = new EditText(this);
        searchBar.setHint("🔍 Search your music...");
        searchBar.setHintTextColor(Color.GRAY);
        searchBar.setTextColor(Color.WHITE);
        searchBar.setSingleLine(true);
        searchBar.setTextSize(16);
        searchBar.setMinHeight(55);
        searchBar.setPadding(20, 15, 20, 15);

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.rgb(40, 40, 40));
        searchBg.setCornerRadius(22);
        searchBar.setBackground(searchBg);

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, -2);
        searchParams.setMargins(0, 20, 0, 20);
        layout.addView(searchBar, searchParams);

Button themeButton = new Button(this);
themeButton.setText("🎨 PREMIUM STYLES");
themeButton.setTextColor(Color.WHITE);
themeButton.setBackgroundTintList(
    android.content.res.ColorStateList.valueOf(Color.rgb(45, 45, 45))
);
themeButton.setAllCaps(false);
themeButton.setOnClickListener(v -> showThemeDialog());
layout.addView(themeButton);

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

Button onlineFavoritesButton = new Button(this);
onlineFavoritesButton.setText("❤️ MY FAVORITES");
onlineFavoritesButton.setTextColor(Color.BLACK);
onlineFavoritesButton.setBackgroundTintList(
    android.content.res.ColorStateList.valueOf(green)
);
onlineFavoritesButton.setAllCaps(false);
onlineFavoritesButton.setOnClickListener(v -> showOnlineFavorites());
layout.addView(onlineFavoritesButton);

Button createPlaylistButton = new Button(this);
createPlaylistButton.setText("➕ CREATE PLAYLIST");
createPlaylistButton.setTextColor(Color.WHITE);
createPlaylistButton.setBackgroundTintList(
    android.content.res.ColorStateList.valueOf(Color.rgb(45, 45, 45))
);
createPlaylistButton.setAllCaps(false);
createPlaylistButton.setOnClickListener(v -> showCreatePlaylistDialog());
layout.addView(createPlaylistButton);

Button viewPlaylistsButton = new Button(this);
viewPlaylistsButton.setText("📂 MY PLAYLISTS");
viewPlaylistsButton.setTextColor(Color.WHITE);
viewPlaylistsButton.setBackgroundTintList(
    android.content.res.ColorStateList.valueOf(Color.rgb(45, 45, 45))
);
viewPlaylistsButton.setAllCaps(false);
viewPlaylistsButton.setOnClickListener(v -> showPlaylistsDialog());
layout.addView(viewPlaylistsButton);

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
onlineHeading.setTextColor(green);
onlineHeading.setTextSize(24);
onlineHeading.setTypeface(null, 1);
onlineHeading.setPadding(0, 25, 0, 15);
layout.addView(onlineHeading);

onlineSearchInput = new EditText(this);
onlineSearchInput.setHint("Search online songs...");
onlineSearchInput.setTextColor(Color.WHITE);
onlineSearchInput.setHintTextColor(Color.GRAY);
onlineSearchInput.setSingleLine(true);
layout.addView(onlineSearchInput);

Button onlineSearchButton = new Button(this);
onlineSearchButton.setText("SEARCH FULL SONGS");
onlineSearchButton.setTextColor(Color.BLACK);
onlineSearchButton.setBackgroundTintList(
    android.content.res.ColorStateList.valueOf(green)
);
onlineSearchButton.setAllCaps(false);
onlineSearchButton.setOnClickListener(v -> searchAudiusMusic());
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

LinearLayout bottomNav = new LinearLayout(this);
bottomNav.setOrientation(LinearLayout.HORIZONTAL);
bottomNav.setGravity(Gravity.CENTER);
bottomNav.setBackgroundColor(Color.rgb(24, 24, 24));

Button homeNav = new Button(this);
homeNav.setText("⌂ HOME");
homeNav.setTextColor(green);
homeNav.setAllCaps(false);

Button searchNav = new Button(this);
searchNav.setText("⌕ SEARCH");
searchNav.setTextColor(Color.WHITE);
searchNav.setAllCaps(false);

Button libraryNav = new Button(this);
libraryNav.setText("♫ LIBRARY");
libraryNav.setTextColor(Color.WHITE);
libraryNav.setAllCaps(false);

LinearLayout.LayoutParams navParams =
        new LinearLayout.LayoutParams(0, 58, 1f);

bottomNav.addView(homeNav, navParams);
bottomNav.addView(searchNav, new LinearLayout.LayoutParams(0, 58, 1f));
bottomNav.addView(libraryNav, new LinearLayout.LayoutParams(0, 58, 1f));

homeNav.setOnClickListener(v -> scroll.smoothScrollTo(0, 0));
searchNav.setOnClickListener(v -> scroll.smoothScrollTo(0, onlineHeading.getTop()));
libraryNav.setOnClickListener(v -> scroll.smoothScrollTo(0, onlineFavoritesButton.getTop()));

root.addView(bottomNav, new LinearLayout.LayoutParams(-1, 58));

setContentView(root);
loadFavorites();
loadSavedTrackMetadata();
loadPlaylists();
// Online-only music mode
    }
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


    void loadSavedTrackMetadata() {
        android.content.SharedPreferences prefs =
            getSharedPreferences("ECHO_TRACKS", MODE_PRIVATE);

        try {
            JSONObject data = new JSONObject(
                prefs.getString("track_metadata", "{}")
            );

            java.util.Iterator<String> keys = data.keys();

            while (keys.hasNext()) {
                String id = keys.next();
                JSONObject track = data.getJSONObject(id);

                savedTrackTitles.put(id, track.optString("title", "Unknown"));
                savedTrackArtists.put(id, track.optString("artist", "Unknown Artist"));
                savedTrackArtwork.put(id, track.optString("artwork", ""));
            }
        } catch (Exception e) {
            android.util.Log.e("ECHO", "Metadata load failed", e);
        }
    }

    void saveSavedTrackMetadata() {
        try {
            JSONObject data = new JSONObject();

            for (String id : savedTrackTitles.keySet()) {
                JSONObject track = new JSONObject();
                track.put("title", savedTrackTitles.get(id));
                track.put("artist", savedTrackArtists.get(id));
                track.put("artwork", savedTrackArtwork.get(id));
                data.put(id, track);
            }

            getSharedPreferences("ECHO_TRACKS", MODE_PRIVATE)
                .edit()
                .putString("track_metadata", data.toString())
                .apply();

        } catch (Exception e) {
            android.util.Log.e("ECHO", "Metadata save failed", e);
        }
    }

    void loadPlaylists() {
        android.content.SharedPreferences prefs =
            getSharedPreferences("ECHO_PLAYLISTS", MODE_PRIVATE);

        playlistNames.clear();
        playlists.clear();

        int count = prefs.getInt("count", 0);

        for (int i = 0; i < count; i++) {
            String name = prefs.getString("name_" + i, "Playlist");
            String data = prefs.getString("songs_" + i, "");

            playlistNames.add(name);

            ArrayList<String> items = new ArrayList<>();
            if (!data.isEmpty()) {
                items.addAll(java.util.Arrays.asList(data.split("\n")));
            }
            playlists.add(items);
        }
    }

    void savePlaylists() {
        android.content.SharedPreferences prefs =
            getSharedPreferences("ECHO_PLAYLISTS", MODE_PRIVATE);

        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putInt("count", playlistNames.size());

        for (int i = 0; i < playlistNames.size(); i++) {
            editor.putString("name_" + i, playlistNames.get(i));
            editor.putString("songs_" + i,
                android.text.TextUtils.join("\n", playlists.get(i)));
        }

        editor.apply();
    }


    void showCreatePlaylistDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter playlist name");
        input.setSingleLine(true);

        new android.app.AlertDialog.Builder(this)
            .setTitle("🎵 CREATE PLAYLIST")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("CREATE", (dialog, which) -> {
                String name = input.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Enter a playlist name",
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                playlistNames.add(name);
                playlists.add(new ArrayList<>());
                savePlaylists();

                Toast.makeText(this, "Playlist created: " + name,
                    Toast.LENGTH_SHORT).show();
            })
            .show();
    }


    void showPlaylistsDialog() {
        if (playlistNames.isEmpty()) {
            Toast.makeText(this, "No playlists created yet",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = playlistNames.toArray(new String[0]);

        new android.app.AlertDialog.Builder(this)
            .setTitle("📂 MY PLAYLISTS")
            .setItems(names, (dialog, which) -> {
                showPlaylistSongsDialog(which);
            })
            .setNegativeButton("CLOSE", null)
            .show();
    }


    void showPlaylistSongsDialog(int playlistIndex) {
        ArrayList<String> items = playlists.get(playlistIndex);

        String[] names = items.toArray(new String[0]);

        new android.app.AlertDialog.Builder(this)
            .setTitle("🎵 " + playlistNames.get(playlistIndex))
            .setItems(names, (dialog, which) -> {
                String selectedUri = items.get(which);

                int songIndex = -1;
                for (int i = 0; i < songs.size(); i++) {
                    if (songs.get(i).toString().equals(selectedUri)) {
                        songIndex = i;
                        break;
                    }
                }

                if (songIndex >= 0) {
                    playSong(songIndex);
                } else {
                    Toast.makeText(this, "Song not found on device",
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setPositiveButton("➕ ADD SONGS", (dialog, which) -> {
                showAddSongsDialog(playlistIndex);
            })
            .setNegativeButton("CLOSE", null)
            .show();
    }


    void showAddSongsDialog(int playlistIndex) {
        String[] names = new String[songViews.size()];
        boolean[] checked = new boolean[songViews.size()];

        for (int i = 0; i < songViews.size(); i++) {
            names[i] = songViews.get(i).getText().toString();
            checked[i] = playlists.get(playlistIndex)
                .contains(songs.get(i).toString());
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("➕ SELECT SONGS")
            .setMultiChoiceItems(names, checked,
                (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                })
            .setPositiveButton("SAVE", (dialog, which) -> {
                ArrayList<String> items = playlists.get(playlistIndex);
                items.clear();

                for (int i = 0; i < checked.length; i++) {
                    if (checked[i]) {
                        items.add(songs.get(i).toString());
                    }
                }

                savePlaylists();

                Toast.makeText(this, "Playlist updated!",
                    Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("CANCEL", null)
            .show();
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

startProgressUpdater();
            isAudiusPlaying = false;
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

void startProgressUpdater() {
    if (progressUpdater != null) {
        handler.removeCallbacks(progressUpdater);
    }

    progressUpdater = new Runnable() {
        @Override
        public void run() {
            MediaPlayer activePlayer = player;

            if (activePlayer != null) {
                try {
                    int duration = activePlayer.getDuration();
                    int position = activePlayer.getCurrentPosition();

                    if (duration > 0) {
                        if (seekBar != null) {
                            seekBar.setMax(duration);
                            seekBar.setProgress(position);
                        }

                        if (currentTime != null) {
                            currentTime.setText(formatTime(position));
                        }

                        if (dialogSeekBar != null) {
                            dialogSeekBar.setMax(duration);
                            if (!isSeeking) {
                                dialogSeekBar.setProgress(position);
                            }
                        }

                        if (dialogCurrentTime != null && !isSeeking) {
                            dialogCurrentTime.setText(formatTime(position));
                        }

                        if (dialogTotalTime != null) {
                            dialogTotalTime.setText(formatTime(duration));
                        }
                    }
                } catch (IllegalStateException ignored) {
                }
            }

            handler.postDelayed(this, 500);
        }
    };

    handler.post(progressUpdater);
}

void updateSongInfo(int index) {
    if (isAudiusPlaying && currentAudiusIndex >= 0
            && currentAudiusIndex < onlineTitles.size()) {
        String title = onlineTitles.get(currentAudiusIndex);
        String artist = onlineArtists.get(currentAudiusIndex);

        if (playerSongTitle != null) playerSongTitle.setText(title);
        if (playerArtist != null) playerArtist.setText(artist);
        if (albumArt != null) {
            albumArt.setImageResource(android.R.drawable.ic_media_play);

            final int artIndex = currentAudiusIndex;
            String imageUrl = artIndex < onlineArtwork.size()
                    ? onlineArtwork.get(artIndex) : "";

            if (!imageUrl.isEmpty()) {
                final String artworkUrl = imageUrl;

                new Thread(() -> {
                    java.net.HttpURLConnection connection = null;
                    try {
                        java.net.URL url = new java.net.URL(artworkUrl);
                        connection = (java.net.HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);

                        android.graphics.Bitmap bitmap;
                        try (java.io.InputStream stream = connection.getInputStream()) {
                            bitmap = android.graphics.BitmapFactory.decodeStream(stream);
                        }

                        if (bitmap != null) {
                            runOnUiThread(() -> {
                                if (albumArt != null && isAudiusPlaying
                                        && currentAudiusIndex == artIndex) {
                                    albumArt.setImageBitmap(bitmap);
                                }
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ECHO", "Artwork load failed", e);
                    } finally {
                        if (connection != null) connection.disconnect();
                    }
                }).start();
            }
        }
        nowPlaying.setText("🎵 " + title);
        miniNowPlaying.setText("🎵 " + title);
        return;
    }

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
    if (!isAudiusPlaying && (currentSong < 0 || songs.isEmpty())) {
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
        public void onStartTrackingTouch(SeekBar bar) {
            isSeeking = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            if (player != null) {
                player.seekTo(bar.getProgress());
            }
            isSeeking = false;
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


void searchAudiusMusic() {
    String query = onlineSearchInput.getText().toString().trim();

    if (query.isEmpty()) {
        Toast.makeText(this, "Enter song name", Toast.LENGTH_SHORT).show();
        return;
    }

    onlineResultsLayout.removeAllViews();

    TextView loading = new TextView(this);
    loading.setText("Searching full songs...");
    loading.setTextColor(Color.WHITE);
    onlineResultsLayout.addView(loading);

    new Thread(() -> {
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> artists = new ArrayList<>();
        ArrayList<String> artworks = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();
        HashSet<String> seenIds = new HashSet<>();

        ArrayList<String> queries = new ArrayList<>();
        queries.add(query);

        String lower = query.toLowerCase();

        if (lower.contains("gujarati") || query.contains("ગુજરાતી")) {
            String[] extraQueries = {
                "Gujarati songs",
                "ગુજરાતી ગીતો",
                "Gujarati love song",
                "Gujarati romantic songs",
                "Gujarati sad songs",
                "Gujarati folk",
                "Gujarati DJ",
                "Garba",
                "Gujarati bhajan",
                "Gujarati old songs",
                "Gujarati new songs"
            };

            for (String q : extraQueries) {
                queries.add(q);
            }
        }

        for (String searchQuery : queries) {
            HttpURLConnection connection = null;

            try {
                String encoded = URLEncoder.encode(searchQuery, "UTF-8");

                URL url = new URL(
                    "https://discoveryprovider.audius.co/v1/tracks/search?query="
                    + encoded + "&limit=25"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setRequestProperty("User-Agent", "ECHO-Music-App");

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONArray("data");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject track = results.getJSONObject(i);

                    if (!"true".equalsIgnoreCase(
                        track.optString("is_streamable", "false"))) {
                        continue;
                    }

                    String trackId = track.optString("track_id", "");

                    if (trackId.isEmpty() || !seenIds.add(trackId)) {
                        continue;
                    }

                    String title = android.net.Uri.decode(
                        track.optString("title", "Unknown")
                    );

                    String artist = track.optJSONObject("user") != null
                        ? track.getJSONObject("user").optString("name", "Audius Artist")
                        : "Audius Artist";

                    titles.add(title);
                    artists.add(artist);
                    JSONObject artwork = track.optJSONObject("artwork");
                    String artworkUrl = "";
                    if (artwork != null) {
                        artworkUrl = artwork.optString("1000x1000", artwork.optString("480x480", artwork.optString("150x150", "")));
                    }
                    artworks.add(artworkUrl);
                    ids.add(trackId);
                }

            } catch (Exception e) {
                android.util.Log.e("ECHO", "Search failed: " + searchQuery, e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        runOnUiThread(() -> {
            onlineResultsLayout.removeAllViews();

            onlineTitles.clear();
            onlineArtists.clear();
            onlineArtwork.clear();
            audiusTrackIds.clear();

            onlineTitles.addAll(titles);
            onlineArtists.addAll(artists);
            onlineArtwork.addAll(artworks);
            audiusTrackIds.addAll(ids);

            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                savedTrackTitles.put(id, titles.get(i));
                savedTrackArtists.put(id, artists.get(i));
                savedTrackArtwork.put(id, artworks.get(i));
            }
            saveSavedTrackMetadata();

            for (int i = 0; i < titles.size(); i++) {
                final int index = i;

                TextView item = new TextView(this);
                item.setText("🎵 " + titles.get(i)
                    + "\n" + artists.get(i)
                    + "\n▶ PLAY FULL TRACK");

                item.setTextColor(Color.WHITE);
                item.setTextSize(16);
                item.setPadding(18, 18, 18, 18);

                item.setOnClickListener(v -> playAudiusTrack(index));

                onlineResultsLayout.addView(item);
                onlineResultsLayout.addView(createFavoriteButton("audius:" + ids.get(i)));
            }

            if (titles.isEmpty()) {
                TextView empty = new TextView(this);
                empty.setText("No playable full songs found.");
                empty.setTextColor(Color.WHITE);
                onlineResultsLayout.addView(empty);
            }
        });

    }).start();
}


void playAudiusTrack(int index) {
    try {
        if (player != null) {
            player.release();
            player = null;
        }

        currentSong = -1;
        isAudiusPlaying = true;
        currentAudiusIndex = index;
        player = new MediaPlayer();

        String trackId = audiusTrackIds.get(index);

        String streamUrl =
            "https://discoveryprovider.audius.co/v1/tracks/"
            + URLEncoder.encode(trackId, "UTF-8")
            + "/stream";

        player.setDataSource(streamUrl);

        player.setOnPreparedListener(mp -> {
            mp.start();
            startProgressUpdater();

            String title = onlineTitles.get(index);
            nowPlaying.setText("🎵 " + title);
            miniNowPlaying.setText("🎵 " + title);

            playButton.setText("PAUSE");
            miniPlayButton.setText("Ⅱ");

            Toast.makeText(this, "Playing full Audius track",
                Toast.LENGTH_SHORT).show();
        });

        player.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Track unavailable. Try another.",
                Toast.LENGTH_LONG).show();
            return true;
        });

        player.prepareAsync();

    } catch (Exception e) {
        Toast.makeText(this, "Cannot play Audius track",
            Toast.LENGTH_SHORT).show();
    }
}

void loadTheme(){int t=getSharedPreferences("ECHO_SETTINGS",0).getInt("theme",0);int[] b={0xff0a0a12,0xff0f081c,0xff051423,0xff230c14,0xff081912};int[] a={0xff00b4ff,0xffbe50ff,0xff00dcdc,0xffff9141,0xff32dc82};bg=b[t>=0&&t<5?t:0];blue=a[t>=0&&t<5?t:0];}
void showThemeDialog(){String[] n={"🌌 Midnight Blue","💜 Neon Purple","🌊 Ocean","🌅 Sunset","💚 Emerald"};new android.app.AlertDialog.Builder(this).setTitle("🎨 ECHO PREMIUM STYLES").setItems(n,(d,w)->{getSharedPreferences("ECHO_SETTINGS",0).edit().putInt("theme",w).apply();recreate();}).setNegativeButton("CANCEL",null).show();}
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

    Button createFavoriteButton(String trackId) {
        Button button = new Button(this);

        Runnable updateText = () -> button.setText(
            favoriteSongs.contains(trackId)
                ? "♥ FAVORITED"
                : "♡ ADD TO FAVORITES"
        );

        updateText.run();
        button.setAllCaps(false);

        button.setOnClickListener(v -> {
            if (favoriteSongs.contains(trackId)) {
                favoriteSongs.remove(trackId);
            } else {
                favoriteSongs.add(trackId);
            }
            saveFavorites();
            updateText.run();
        });

        return button;
    }

    void showOnlineFavorites() {
        android.app.Dialog dialog = new android.app.Dialog(this);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(20, 20, 20, 20);
        list.setBackgroundColor(Color.rgb(15, 15, 25));

        TextView heading = new TextView(this);
        heading.setText("❤️ MY FAVORITES");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(22);
        list.addView(heading);

        int count = 0;

        for (String favoriteId : new HashSet<>(favoriteSongs)) {
            if (!favoriteId.startsWith("audius:")) continue;

            String id = favoriteId.substring(7);
            String title = savedTrackTitles.get(id);
            String artist = savedTrackArtists.get(id);

            if (title == null) continue;

            TextView item = new TextView(this);
            item.setText("🎵 " + title + "\n" + artist + "\n▶ PLAY");
            item.setTextColor(Color.WHITE);
            item.setTextSize(17);
            item.setPadding(15, 20, 15, 20);

            item.setOnClickListener(v -> {
                onlineTitles.clear();
                onlineArtists.clear();
                onlineArtwork.clear();
                audiusTrackIds.clear();

                onlineTitles.add(title);
                onlineArtists.add(artist == null ? "Unknown Artist" : artist);
                onlineArtwork.add(savedTrackArtwork.getOrDefault(id, ""));
                audiusTrackIds.add(id);

                playAudiusTrack(0);
                dialog.dismiss();
            });

            list.addView(item);
            count++;
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No online favorites saved yet.");
            empty.setTextColor(Color.WHITE);
            list.addView(empty);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(list);
        dialog.setContentView(scroll);
        dialog.show();
    }
}

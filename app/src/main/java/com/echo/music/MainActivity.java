package com.echo.music;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout mainLayout;

    int background = Color.rgb(10, 10, 18);
    int blue = Color.rgb(0, 180, 255);
    int white = Color.WHITE;
    int grey = Color.rgb(160, 160, 175);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);

        ScrollView scroll = new ScrollView(this);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 25, 20, 20);
        mainLayout.setBackgroundColor(background);

        scroll.addView(mainLayout);

        // HEADER
        TextView logo = new TextView(this);
        logo.setText("ECHO");
        logo.setTextSize(32);
        logo.setTextColor(blue);
        logo.setTypeface(null, 1);

        mainLayout.addView(logo);

        TextView greeting = new TextView(this);
        greeting.setText("YOUR MUSIC. YOUR WORLD.");
        greeting.setTextColor(grey);
        greeting.setTextSize(12);

        mainLayout.addView(greeting);

        // SEARCH BAR
        EditText search = new EditText(this);
        search.setHint("Search songs, artists...");
        search.setHintTextColor(grey);
        search.setTextColor(white);
        search.setSingleLine(true);
        search.setPadding(18, 5, 18, 5);
        search.setBackgroundColor(Color.rgb(30, 30, 42));

        LinearLayout.LayoutParams searchParams =
            new LinearLayout.LayoutParams(
                -1, 55
            );

        searchParams.setMargins(0, 25, 0, 25);
        mainLayout.addView(search, searchParams);

        // SECTION TITLE
        addTitle("Discover Music");

        // CATEGORIES
        addCategory("All");
        addCategory("Trending");
        addCategory("Pop");
        addCategory("Rock");
        addCategory("Chill");

        // FEATURED
        addTitle("Featured");

        TextView featured = new TextView(this);
        featured.setText(
            "🎧  YOUR DAILY MIX\n\n" +
            "Discover music made for you.\n\n" +
            "EXPLORE  →"
        );

        featured.setTextColor(white);
        featured.setTextSize(20);
        featured.setPadding(22, 25, 22, 25);
        featured.setBackgroundColor(Color.rgb(15, 65, 100));

        mainLayout.addView(featured);

        // SONGS
        addTitle("Popular Songs");

        addSong("🎵  Midnight Dreams", "ECHO Music");
        addSong("🎵  Lost In The Stars", "ECHO Music");
        addSong("🎵  Ocean Lights", "ECHO Music");
        addSong("🎵  Endless Memories", "ECHO Music");

        // BOTTOM NAVIGATION
        addTitle("HOME       SEARCH       LIBRARY");

        TextView footer = new TextView(this);
        footer.setText("Made with ♥ by ECHO");
        footer.setTextColor(grey);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 30, 0, 20);

        mainLayout.addView(footer);

        setContentView(scroll);
    }

    void addTitle(String text) {
        TextView title = new TextView(this);
        title.setText(text);
        title.setTextSize(21);
        title.setTextColor(white);
        title.setTypeface(null, 1);
        title.setPadding(0, 25, 0, 15);

        mainLayout.addView(title);
    }

    void addCategory(String name) {
        TextView category = new TextView(this);
        category.setText(name);
        category.setTextColor(blue);
        category.setTextSize(15);
        category.setPadding(12, 10, 12, 10);

        mainLayout.addView(category);
    }

    void addSong(String name, String artist) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(15, 15, 15, 15);
        row.setBackgroundColor(Color.rgb(24, 24, 36));

        TextView song = new TextView(this);
        song.setText(name);
        song.setTextColor(white);
        song.setTextSize(17);

        TextView singer = new TextView(this);
        singer.setText(artist);
        singer.setTextColor(grey);
        singer.setTextSize(13);

        row.addView(song);
        row.addView(singer);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-1, -2);

        params.setMargins(0, 5, 0, 5);

        mainLayout.addView(row, params);
    }
}

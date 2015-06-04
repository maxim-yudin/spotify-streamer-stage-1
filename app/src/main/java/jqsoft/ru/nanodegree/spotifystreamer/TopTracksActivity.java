package jqsoft.ru.nanodegree.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TopTracksActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.ARTIST_NAME) && extras.containsKey(Constants.ARTIST_ID)) {
                final String artistId = extras.getString(Constants.ARTIST_ID);
                final String artistName = extras.getString(Constants.ARTIST_NAME);
                if (savedInstanceState == null) {
                    TopTracksActivityFragment topTracksFragment =
                            TopTracksActivityFragment.newInstance(artistId, artistName);
                    getSupportFragmentManager().beginTransaction().add(android.R.id.content, topTracksFragment).commit();
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(artistName);
                }
            }
        }
    }
}

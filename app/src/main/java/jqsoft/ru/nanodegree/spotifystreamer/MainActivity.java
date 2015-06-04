package jqsoft.ru.nanodegree.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            MainActivityFragment mainFragment = MainActivityFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, mainFragment).commit();
        }
    }
}

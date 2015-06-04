package jqsoft.ru.nanodegree.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;

public class MainActivityFragment extends ListFragment {
    private EditText etArtistName;

    public static MainActivityFragment newInstance() {
        MainActivityFragment fragment = new MainActivityFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        getListView().setPadding(horizontalPadding, 0, horizontalPadding, 0);
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        setListShown(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseFragmentView = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup fragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        fragmentView.addView(baseFragmentView);

        etArtistName = (EditText) fragmentView.findViewById(R.id.etArtistName);
        etArtistName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
        return fragmentView;
    }

    private void performSearch() {
        String artistName = etArtistName.getText().toString().trim();
        if (artistName.length() == 0) {
            Toast.makeText(getActivity(), R.string.please_type_artist_name, Toast.LENGTH_SHORT).show();
            return;
        }

        // hide keyboard before beginning search
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(etArtistName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        new GetArtistsTask().execute(artistName);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        Map<String, String> artistInfo = (Map<String, String>) list.getItemAtPosition(position);
        Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
        topTracksIntent.putExtra(Constants.ARTIST_NAME, artistInfo.get(Constants.ARTIST_NAME));
        topTracksIntent.putExtra(Constants.ARTIST_ID, artistInfo.get(Constants.ARTIST_ID));
        startActivity(topTracksIntent);
    }

    private class GetArtistsTask extends AsyncTask<String, Void, ArtistsPager> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setEmptyText("");
            setListAdapter(null);
            setListShown(false);
        }

        protected ArtistsPager doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                return spotify.searchArtists(params[0]);
            } catch (Exception e) {
                // if some erros occurs, e.g. no internet
                return null;
            }
        }

        protected void onPostExecute(ArtistsPager result) {
            if (getActivity() == null) {
                return;
            }

            if (result == null) {
                setEmptyText(getString(R.string.some_error));
                Toast.makeText(getActivity(), R.string.some_error, Toast.LENGTH_SHORT).show();
                setListShown(true);
                return;
            }

            setEmptyText(getString(R.string.no_artists_are_found_try_search_again));

            ArrayList<Map<String, String>> artistList = new ArrayList<>(
                    result.artists.items.size());
            Map<String, String> currentArtistInfo;
            for (Artist artist : result.artists.items) {
                currentArtistInfo = new HashMap<>();
                currentArtistInfo.put(Constants.ARTIST_NAME, artist.name);
                currentArtistInfo.put(Constants.ARTIST_ID, artist.id);

                if (artist.images != null) {
                    String thumbImageUrl = null;
                    Image currentImage;
                    for (int i = artist.images.size() - 1; i >= 0; i--) {
                        currentImage = artist.images.get(i);
                        if (currentImage.width >= 200 || currentImage.height >= 200) {
                            thumbImageUrl = currentImage.url;
                            break;
                        }
                    }
                    if (thumbImageUrl != null) {
                        currentArtistInfo.put(Constants.ARTIST_THUMBNAIL_IMAGE_URL, thumbImageUrl);
                    }
                }

                artistList.add(currentArtistInfo);
            }

            SimpleAdapter artistAdapter = new SimpleAdapter(getActivity(), artistList, R.layout.row_artist,
                    new String[]{Constants.ARTIST_THUMBNAIL_IMAGE_URL, Constants.ARTIST_NAME},
                    new int[]{R.id.ivArtistPhoto, R.id.tvArtistName});
            artistAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view.getId() == R.id.ivArtistPhoto) {
                        Picasso.with(getActivity()).load((String) data).into((ImageView) view);
                        return true;
                    }
                    return false;
                }
            });

            setListAdapter(artistAdapter);
        }
    }
}

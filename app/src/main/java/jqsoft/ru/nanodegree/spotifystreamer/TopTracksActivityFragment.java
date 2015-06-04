package jqsoft.ru.nanodegree.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class TopTracksActivityFragment extends ListFragment {
    public static TopTracksActivityFragment newInstance(String artistId, String artistName) {
        TopTracksActivityFragment fragment = new TopTracksActivityFragment();

        Bundle args = new Bundle();
        args.putString(Constants.ARTIST_ID, artistId);
        args.putString(Constants.ARTIST_NAME, artistName);
        fragment.setArguments(args);

        return fragment;
    }

    private String getArtistName() {
        return getArguments().getString(Constants.ARTIST_NAME);
    }

    private String getArtistId() {
        return getArguments().getString(Constants.ARTIST_ID);
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
        if (savedInstanceState == null) {
            new GetTopTracksTask().execute(getArtistId());
        }
    }

    private class GetTopTracksTask extends AsyncTask<String, Void, Tracks> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setEmptyText("");
            setListAdapter(null);
            setListShown(false);
        }

        protected Tracks doInBackground(String... params) {
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> country = new HashMap<>();
                country.put(Constants.PARAM_COUNTRY, Constants.COUNTRY_DEFAULT);
                return spotify.getArtistTopTrack(params[0], country);
            } catch (Exception e) {
                // if some erros occurs, e.g. no internet
                return null;
            }
        }

        protected void onPostExecute(Tracks result) {
            if (getActivity() == null) {
                return;
            }

            if (result == null) {
                setEmptyText(getString(R.string.some_error));
                Toast.makeText(getActivity(), R.string.some_error, Toast.LENGTH_SHORT).show();
                setListShown(true);
                return;
            }

            setEmptyText(getString(R.string.no_tracks_are_found));

            ArrayList<Map<String, String>> trackList = new ArrayList<>(
                    result.tracks.size());
            Map<String, String> currentTrackInfo;
            for (Track track : result.tracks) {
                currentTrackInfo = new HashMap<>();
                currentTrackInfo.put(Constants.TRACK_NAME, track.name);
                currentTrackInfo.put(Constants.ALBUM_NAME, track.album.name);

                if (track.album.images != null) {
                    String smallThumbImageUrl = null;
                    Image currentImage;
                    for (int i = track.album.images.size() - 1; i >= 0; i--) {
                        currentImage = track.album.images.get(i);
                        if (currentImage.width >= 200 || currentImage.height >= 200) {
                            smallThumbImageUrl = currentImage.url;
                            break;
                        }
                    }

                    if (smallThumbImageUrl != null) {
                        currentTrackInfo.put(Constants.ALBUM_ART_SMALL, smallThumbImageUrl);
                    }

                    String largeThumbImageUrl = null;

                    for (int i = 0; i < track.album.images.size(); i++) {
                        currentImage = track.album.images.get(i);
                        if (currentImage.width <= 640 || currentImage.height <= 640) {
                            largeThumbImageUrl = currentImage.url;
                            break;
                        }
                    }

                    if (largeThumbImageUrl != null) {
                        currentTrackInfo.put(Constants.ALBUM_ART_LARGE, largeThumbImageUrl);
                    }
                }
                currentTrackInfo.put(Constants.TRACK_PREVIEW_URL, track.preview_url);
                currentTrackInfo.put(Constants.ARTIST_NAME, getArtistName());
                trackList.add(currentTrackInfo);
            }

            SimpleAdapter trackAdapter = new SimpleAdapter(getActivity(), trackList, R.layout.row_track,
                    new String[]{Constants.ALBUM_ART_SMALL, Constants.TRACK_NAME, Constants.ALBUM_NAME},
                    new int[]{R.id.ivAlbumPhoto, R.id.tvTrackName, R.id.tvAlbumName});
            trackAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view.getId() == R.id.ivAlbumPhoto) {
                        Picasso.with(getActivity()).load((String) data).into((ImageView) view);
                        return true;
                    }
                    return false;
                }
            });

            setListAdapter(trackAdapter);
        }
    }
}

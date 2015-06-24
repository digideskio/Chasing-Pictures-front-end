package de.fu_berlin.cdv.chasingpictures.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import de.fu_berlin.cdv.chasingpictures.Maps;
import de.fu_berlin.cdv.chasingpictures.PictureDownloader;
import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.api.Picture;
import de.fu_berlin.cdv.chasingpictures.api.PictureRequest;
import de.fu_berlin.cdv.chasingpictures.api.Place;

/**
 * Display a slideshow of all the pictures for a place.
 *
 * @author Simon Kalt
 */
public class Slideshow extends Activity {

    protected List<Picture> mPictures;
    private ProgressBar mProgressBar;
    private ViewGroup mContainerView;
    private RelativeLayout currentImageLayout;
    private Handler mHandler;
    private Place mPlace;

    /**
     * Creates an {@link Intent} for a slideshow using the given place.
     *
     * @param context  The current context
     * @param place A place for which to show the slideshow
     * @return An intent to be used with {@link #startActivity(Intent)}.
     */
    public static Intent createIntent(Context context, Place place) {
        Intent intent = new Intent(context, Slideshow.class);
        intent.putExtra(Maps.EXTRA_PLACE, place);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);
        mContainerView = (ViewGroup) findViewById(R.id.slideshowLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.slideshowProgressBar);
        mHandler = new Handler();
        mPlace = (Place) getIntent().getSerializableExtra(Maps.EXTRA_PLACE);

        if (mPlace == null) {
            Toast.makeText(
                    this,
                    "Did not receive a place",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        }

        PictureDownloader downloader = new SlideshowPictureDownloader(getCacheDir());
        PictureRequestTask pictureRequestTask = new PictureRequestTask(downloader);
        pictureRequestTask.execute(new PictureRequest(this, mPlace));
    }

    /**
     * Replace the currently displayed picture with the one with the given index.
     * @param index Index of the picture in the {@link #mPictures} list
     */
    private void setNewPicture(int index) {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(
                R.layout.slideshow_image,
                mContainerView,
                false
        );

        ImageView newImageView = (ImageView) relativeLayout.getChildAt(0);

        newImageView.setImageBitmap(getBitmapForIndex(index));

        if (currentImageLayout != null) {
            currentImageLayout.setVisibility(View.INVISIBLE);
            mContainerView.removeView(currentImageLayout);
        }

        currentImageLayout = relativeLayout;
        mContainerView.addView(relativeLayout, 0);
    }

    private Bitmap getBitmapForIndex(int idx) {
        String file = mPictures.get(idx).getCachedFile().getPath();
        return BitmapFactory.decodeFile(file);
    }

    /**
     * A background task for requesting all the available pictures for the current place.
     */
    private class PictureRequestTask extends AsyncTask<PictureRequest, Void, List<Picture>> {
        private final PictureDownloader downloader;

        public PictureRequestTask(PictureDownloader downloader) {
            this.downloader = downloader;
        }

        @Override
        protected List<Picture> doInBackground(PictureRequest... params) {
            // FIXME: Check for null, if yes, display error and exit
            return params[0].sendRequest().getBody().getPlaces().get(0).getPictures();
        }

        @Override
        protected void onPostExecute(List<Picture> pictures) {
            mPictures = pictures;
            mProgressBar.setMax(mPictures.size());
            downloader.execute(mPictures.toArray(new Picture[mPictures.size()]));
        }
    }

    /**
     * A background task for downloading the given pictures
     * and, when finished, starting the slideshow.
     */
    private class SlideshowPictureDownloader extends PictureDownloader {
        public SlideshowPictureDownloader(File targetDirectory) {
            super(targetDirectory);
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            if (values.length > 0)
                mProgressBar.setProgress(values[0].getCurrent());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.GONE);
            new SlideshowTask().executeOnExecutor(THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * A background task for animating the transition between images.
     */
    private class SlideshowTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for(int i = 0; i < mPictures.size(); i++) {
                final int finalI = i;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setNewPicture(finalI);
                    }
                });
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

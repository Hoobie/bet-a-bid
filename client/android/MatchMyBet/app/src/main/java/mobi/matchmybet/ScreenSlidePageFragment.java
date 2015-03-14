package mobi.matchmybet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class ScreenSlidePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            TextView textView = (TextView) rootView.findViewById(R.id.auctionTitleTextView);
            textView.setText(bundle.getString("title", ""));
            String imageURL = bundle.getString("imageUrl", "");
            ImageView imageView = (ImageView) rootView.findViewById(R.id.auctionImageView);
            new DownloadImageTask(imageView).execute(imageURL);
        }

        return rootView;
    }

    public static ScreenSlidePageFragment getInstance(String title, String imageUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("imageUrl", imageUrl);
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }
}
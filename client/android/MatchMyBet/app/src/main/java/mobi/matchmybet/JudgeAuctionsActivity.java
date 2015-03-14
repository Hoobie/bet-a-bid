package mobi.matchmybet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import mobi.matchmybet.model.Auction;


public class JudgeAuctionsActivity extends Activity {

    private ArrayList<Auction> auctions = new ArrayList<>();
    private ArrayList<ImageView> imageViews = new ArrayList<>();
    private String nick;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.58:6666");
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    private HorizontalScrollView scrollView;
    private int auctionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge_auctions);
        scrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // disable scrolling
            }
        });
        Intent i = getIntent();
        nick = i.getStringExtra(MainActivity.EXTRA_NICK);
        mSocket.connect();
        mSocket.on("paired", onPaired);
        mSocket.emit("join", nick);
    }

    private Emitter.Listener onPaired = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JudgeAuctionsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject auctionJSON;
                    String title, url;

                    try {
                        JSONArray jsonAuctions = (JSONArray) data.getJSONArray("auctions");
                        for (int i = 0; i < jsonAuctions.length(); i++) {
                            auctionJSON = jsonAuctions.getJSONObject(i);
                            title = auctionJSON.getString("title");
                            url = auctionJSON.getString("image");
                            auctions.add(new Auction(title, url));
                        }
                    } catch (JSONException e) {
                        return;
                    }

                    showAuctionsData(auctions);
                }
            });
        }
    };

    public void onYesClick(View view) {
        scrollRight();
    }

    public void onNoClick(View view) {
        scrollRight();
    }

    public void scrollRight() {
        scrollView.scrollTo(300 * ++auctionCounter, 0);
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
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).openStream());
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

    private void showAuctionsData(ArrayList<Auction> auctions) {
        LinearLayout auctionsLayout = (LinearLayout)findViewById(R.id.auctionsLayout);

        for (Auction auction : auctions) {
            LinearLayout insideLayout = new LinearLayout(getApplicationContext());
            insideLayout.setOrientation(LinearLayout.VERTICAL);
            insideLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView titleView = new TextView(getApplicationContext());
            titleView.setTextColor(Color.BLACK);
            titleView.setText(auction.getTitle());
            insideLayout.addView(titleView);
            ImageView imageView = new ImageView(getApplicationContext());
            new DownloadImageTask(imageView).execute(auction.getUrl());
            insideLayout.addView(imageView);
            imageViews.add(imageView);
            auctionsLayout.addView(insideLayout);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.off("paired", onPaired);
        mSocket.disconnect();
    }
}

package mobi.matchmybet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import mobi.matchmybet.model.Auction;


public class JudgeAuctionsActivity extends FragmentActivity {

    private ArrayList<Auction> auctions = new ArrayList<>();
    private String nick;
    private int preferencesMask;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.58:8000");
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge_auctions);
        Intent i = getIntent();
        nick = i.getStringExtra(MainActivity.EXTRA_NICK);
        mSocket.connect();
        mSocket.on("paired", onPaired);
        mSocket.emit("join", "{ \"name\" : \"" + nick + "\", \"image\" : \"http://www.digibuzzme.com/wp-content/uploads/2012/11/error-404-road-not-found.jpg\" }");
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Auction auction = auctions.get(position);
            return ScreenSlidePageFragment.getInstance(auction.getTitle(), auction.getUrl());
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private ZoomOutPageTransformer zoomOutPageTransformer;
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
                        JSONArray jsonAuctions = data.getJSONArray("auctions");
                        for (int i = 0; i < jsonAuctions.length(); i++) {
                            auctionJSON = jsonAuctions.getJSONObject(i);
                            title = auctionJSON.getString("title");
                            url = auctionJSON.getString("image");
                            auctions.add(new Auction(title, url));
                        }

                        // Instantiate a ViewPager and a PagerAdapter.
                        mPager = (ViewPager) findViewById(R.id.viewPager);
                        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                        mPager.setAdapter(mPagerAdapter);
                        zoomOutPageTransformer = new ZoomOutPageTransformer();
                        mPager.setPageTransformer(true, zoomOutPageTransformer);
                        mPager.setHorizontalScrollBarEnabled(false);
                        mPager.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true; // disable swipe
                            }
                        });
                    } catch (JSONException e) {
                    }
                }
            });
        }
    };

    public void onYesClick(View view) {
        preferencesMask += 1 << mPager.getCurrentItem();
        click();
    }

    public void onNoClick(View view) {
        scrollRight();
    }

    private void click() {
        if (mPager.getCurrentItem() == NUM_PAGES) {
            mSocket.emit("result", preferencesMask);
        } else {
            scrollRight();
        }
    }

    public void scrollRight() {
        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.off("paired", onPaired);
        //mSocket.emit("disconnect");
        mSocket.disconnect();
    }
}

package mobi.matchmybet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import mobi.matchmybet.model.Auction;


public class JudgeAuctionsActivity extends Activity {

    private ArrayList<Auction> auctions = new ArrayList<>();
    private String nick;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.58:6666");
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_judge_auctions);
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

    private void showAuctionsData(ArrayList<Auction> auctions) {
        TextView auctionsData = (TextView) findViewById(R.id.auctionsData);

        for (int i = 0; i < 3; i++) {
            auctionsData.append(auctions.get(i).getTitle() + auctions.get(i).getUrl());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("paired", onPaired);
    }
}

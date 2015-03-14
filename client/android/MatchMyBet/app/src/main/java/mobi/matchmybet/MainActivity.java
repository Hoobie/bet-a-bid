package mobi.matchmybet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import mobi.matchmybet.model.Auction;

public class MainActivity extends Activity {

    public static final String EXTRA_NICK = "mobi.matchmybet.NICK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartButtonClick(View view) {
        EditText nickInput = (EditText) findViewById(R.id.nick);
        String nick = nickInput.getText().toString();

        if (TextUtils.isEmpty(nick)) {
            return;
        }

        startJudgeAuctionsActivity(nick);
    }

    private void startJudgeAuctionsActivity(String nick) {
        Intent intent = new Intent(this, JudgeAuctionsActivity.class);
        intent.putExtra(EXTRA_NICK, nick);
        startActivity(intent);
    }
}

package mobi.matchmybet;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends Activity {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://localhost:6666");
        } catch (URISyntaxException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSocket.connect();
    }

    public void findPair(View view) {
        EditText nickInput = (EditText) findViewById(R.id.nick);
        String nick = nickInput.getText().toString();

        if (TextUtils.isEmpty(nick)) {
            return;
        }

        mSocket.emit("join", nick);
        mSocket.on("paired", onPaired);
    }

    private Emitter.Listener onPaired = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    addMessage(message);
                }
            });
        }
    };

    private void addMessage(String message) {
        EditText nickInput = (EditText) findViewById(R.id.nick);
        nickInput.setText(message);
        mSocket.off("paired");
    }
}

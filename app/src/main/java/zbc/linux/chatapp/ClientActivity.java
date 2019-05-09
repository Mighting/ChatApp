//TODO Codes of Danger:
// Red = Fix ASAP
// Yellow = Fix when you can
// Green = Minor improvements

package zbc.linux.chatapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


public class ClientActivity extends Activity {

    private ListView mList;
    private ArrayList<String> arrayList;
    private ClientListAdapter mAdapter;
    private TcpClient mTcpClient;

    //TODO (RED) Fix this shit, this is not okay
    // I want to make it so that network calls go on a different thread instead.
    public void enableStrictMode(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableStrictMode();

        arrayList = new ArrayList<String>();

        final EditText editText = (EditText) findViewById(R.id.edit_text);
        final Button create = (Button) findViewById(R.id.Create_button);
        final Button retrieve = (Button) findViewById(R.id.Retrieve_button);
        final Button update = (Button) findViewById(R.id.Update_button);
        final Button delete = (Button) findViewById(R.id.Delete_button);

        mList = (ListView) findViewById(R.id.list);
        mAdapter = new ClientListAdapter(this, arrayList);
        mList.setAdapter(mAdapter);


        //TODO (GREEN) Make is so you only have one listener instead of 4

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = create.getText().toString();
                String toCreate = editText.getText().toString();
                arrayList.add("client :" + message + " " + toCreate);


                //TODO (YELLOW) instead of just sending the param then
                // check if user presses on Enter to send it
                // Check for null
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                    mTcpClient.sendMessage(toCreate);
                }

                mAdapter.notifyDataSetChanged();
                editText.setText("");

            }

        });

        retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String message = retrieve.getText().toString();
                arrayList.add("client: " + message);

                if (mTcpClient != null){
                    mTcpClient.sendMessage(message);
                }

                mAdapter.notifyDataSetChanged();
            }
        });

        update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String message = update.getText().toString();

                //TODO (YELLOW) find a better way that splitting the sentence into 2
                String[] completeText = editText.getText().toString().split(" ");
                String toChange = completeText[0];
                String changedTo = completeText[1];

                arrayList.add("client: " + message + " " + toChange + " To " + changedTo);

                if (mTcpClient != null){
                    mTcpClient.sendMessage(message);
                    mTcpClient.sendMessage(toChange);
                    mTcpClient.sendMessage(changedTo);

                }

                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String message = delete.getText().toString();
                String toDelete = editText.getText().toString();

                arrayList.add("client: " + message + " " + toDelete + " From Database");

                if (mTcpClient != null){
                    mTcpClient.sendMessage(message);
                    mTcpClient.sendMessage(toDelete);
                }

                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        mTcpClient.stopClient();
        mTcpClient = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mTcpClient != null) {
            menu.getItem(1).setEnabled(true);
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(1).setEnabled(false);
            menu.getItem(0).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                new ConnectTask().execute("");
                return true;
            case R.id.disconnect:
                mTcpClient.stopClient();
                mTcpClient = null;
                arrayList.clear();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {
        @Override
        protected TcpClient doInBackground(String... message) {
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            });
            mTcpClient.run();
            return null;

        }

        @Override
        protected void onProgressUpdate(String... values){
            super.onProgressUpdate(values);
            arrayList.add(values[0]);
            mAdapter.notifyDataSetChanged();
        }

    }

}

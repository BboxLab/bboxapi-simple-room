package fr.bouyguestelecom.tv.simpleroom;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.Bbox;
import tv.bouyguestelecom.fr.bboxapilibrary.MyBbox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSendMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.MessageResource;

/**
 * Created by rmessara on 24/03/17.
 * simpleRoom
 */

public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    private MyBbox mBbox;
    private Button mCreate;
    private EditText mNameRoom;
    private TextView mText;
    private Button mSend;
    private EditText mMsg;
    private String mMessage = "";
    private String mMyRoom;
    //need handler if you want update ui in listener
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        mCreate = (Button) findViewById(R.id.button_create);
        mNameRoom = (EditText) findViewById(R.id.editText_name_room);
        mText = (TextView) findViewById(R.id.textview_text);
        mSend = (Button) findViewById(R.id.button_send);
        mMsg = (EditText) findViewById(R.id.editText_msg);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mMsg.getText().toString().isEmpty())
                    sendMsg(mMsg.getText().toString());
                else {
                    Log.i(TAG, "msg to send is empty");
                    Toast.makeText(getApplicationContext(), "msg to send is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mNameRoom.getText().toString().isEmpty()) {

                    mMyRoom = "Message/" + mNameRoom.getText();
                    System.out.println("room name : " + mMyRoom);
                    BboxHolder.getInstance().setCustomBbox("127.0.0.1");
                    try {
                        mBbox = BboxHolder.getInstance().getBbox();
                    } catch (MyBboxNotFoundException e) {
                        e.printStackTrace();
                    }

                    String appName = getString(R.string.app_name);
                    Bbox.getInstance().registerApp(mBbox.getIp(),
                            getString(R.string.APP_ID),
                            getString(R.string.APP_SECRET),
                            appName,
                            new IBboxRegisterApp() {
                                @Override
                                public void onResponse(final String registerApp) {
                                    System.out.println("register app : " + registerApp);
                                    if (registerApp != null && !registerApp.isEmpty()) {
                                        // Subscribe for create room
                                        Bbox.getInstance().subscribeNotification(mBbox.getIp(),
                                                getString(R.string.APP_ID),
                                                getString(R.string.APP_SECRET),
                                                registerApp,
                                                mMyRoom,
                                                new IBboxSubscribe() {
                                                    @Override
                                                    public void onSubscribe() {
                                                        //received message from room
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getApplicationContext(), "Subscribe success", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                        Bbox.getInstance().addListener(mBbox.getIp(),
                                                                registerApp,
                                                                new IBboxMessage() {
                                                                    @Override
                                                                    public void onNewMessage(MessageResource message) {
                                                                        Log.i("onNewMessage", "message : " + message.toString());
                                                                        mMessage = mMessage + "\n" + message.toString();
                                                                        handler.post(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                mText.setText(mMessage);
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    }
                                                    @Override
                                                    public void onFailure(Request request, int errorCode) {
                                                        Log.i("onSubscribe", "subscribe failed");
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getApplicationContext(), "subscribe failed", Toast.LENGTH_SHORT).show();

                                                            }
                                                        });
                                                    }
                                                });
                                        // end create room
                                    }
                                }
                                @Override
                                public void onFailure(Request request, int errorCode) {
                                    Log.i("IBboxRegisterApp", "register app failed");
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "register app failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });
                } else {
                    Log.i(TAG, "Room name is empty");
                    Toast.makeText(getApplicationContext(), "Room name is empty", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    void sendMsg(String msg)
    {
        Bbox.getInstance().sendMessage(mBbox.getIp(),
                getString(R.string.APP_ID),
                getString(R.string.APP_SECRET),
                mMyRoom, "test msg send", msg,
                new IBboxSendMessage() {

                    @Override
                    public void onResponse() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Send message success", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Request request, int errorCode) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Send message failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }
}

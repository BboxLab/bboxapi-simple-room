package fr.bouyguestelecom.tv.simpleroom;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Request;
import tv.bouyguestelecom.fr.bboxapilibrary.Bbox;
import tv.bouyguestelecom.fr.bboxapilibrary.MyBbox;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxRegisterApp;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSendMessage;
import tv.bouyguestelecom.fr.bboxapilibrary.callback.IBboxSubscribe;
import tv.bouyguestelecom.fr.bboxapilibrary.model.MessageResource;

/**
 * Created by rmessara on 15/05/17.
 * simple-room
 */

public class NotificationRoom {

    private final TextView mTextview;
    private String TAG = "NotificationRoom";
    private String mMyRoom;
    private MyBbox mBbox;
    private Context mContext;
    private Handler handler;
    private JSONObject mMessage;
    private String tmp = "";


    public NotificationRoom(MyBbox bbox, Context context, TextView textView) {
        mBbox = bbox;
        mContext = context;
        mTextview = textView;
        init();
    }

    public void init() {

        mMyRoom = "Message/Notification";
        handler = new Handler();
        BboxHolder.getInstance().setCustomBbox("127.0.0.1");
        try {
            mBbox = BboxHolder.getInstance().getBbox();
        } catch (MyBboxNotFoundException e) {
            e.printStackTrace();
        }

        String appName = mContext.getString(R.string.app_name);
        Bbox.getInstance().registerApp(mBbox.getIp(),
                mContext.getString(R.string.APP_ID),
                mContext.getString(R.string.APP_SECRET),
                appName,
                new IBboxRegisterApp() {
                    @Override
                    public void onResponse(final String registerApp) {
                        System.out.println("register app : " + registerApp);
                        if (registerApp != null && !registerApp.isEmpty()) {
                            // Subscribe for create room
                            Bbox.getInstance().subscribeNotification(mBbox.getIp(),
                                    mContext.getString(R.string.APP_ID),
                                    mContext.getString(R.string.APP_SECRET),
                                    registerApp,
                                    mMyRoom,
                                    new IBboxSubscribe() {
                                        @Override
                                        public void onSubscribe() {
                                            //received message from room
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(mContext, "Subscribe success", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                            Bbox.getInstance().addListener(mBbox.getIp(),
                                                    registerApp,
                                                    new IBboxMessage() {
                                                        @Override
                                                        public void onNewMessage(MessageResource message) {
                                                            Log.i("onNewMessage", "message : " + message.toString());

                                                            try {
                                                                mMessage = new JSONObject(message.toString());
                                                                if (!mMessage.get("message").equals(mContext.getPackageName()))
                                                                    createNotification();

                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            tmp = tmp + "\n" + message.toString();
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(mContext, mMessage.toString(), Toast.LENGTH_SHORT).show();
                                                                    mTextview.setText(tmp);


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
                                                    Toast.makeText(mContext, "subscribe failed", Toast.LENGTH_SHORT).show();

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
                                Toast.makeText(mContext, "register app failed", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
    }

    public void createNotification() {
        Bbox.getInstance().sendMessage(mBbox.getIp(),
                mContext.getString(R.string.APP_ID),
                mContext.getString(R.string.APP_SECRET),
                mMyRoom, "Create Notification", mContext.getPackageName(),
                new IBboxSendMessage() {

                    @Override
                    public void onResponse() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "createNotification message success", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Request request, int errorCode) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "createNotification message failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }
}

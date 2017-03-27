package fr.bouyguestelecom.tv.simpleroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import tv.bouyguestelecom.fr.bboxapilibrary.MyBbox;
import tv.bouyguestelecom.fr.bboxapilibrary.MyBboxManager;

/**
 * Created by rmessara on 24/03/17.
 * simpleRoom
 */

public class BboxHolder {

    private static final String TAG = BboxHolder.class.getCanonicalName();

    public static BboxHolder mInstance = new BboxHolder();
    private MyBbox mBbox;
    private MyBboxManager bboxManager = new MyBboxManager();
    private Handler handler = new Handler();

    /**
     * Singleton: private constructor. Instance must be retrieved with getInstance method
     */
    private BboxHolder() {}

    public MyBboxManager getBboxManager() {
        return bboxManager;
    }

    public void bboxSearch(final Context context){

        bboxManager.startLookingForBbox(context, new MyBboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final MyBbox bboxFound) {

                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();

                // We save our Bbox.
                mBbox = bboxFound;
                Log.i(TAG, "Bbox found: " + mBbox.getIp() + " macAdress: " + mBbox.getMacAddress());

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("bboxip", mBbox.getIp());
                editor.commit();
            }
        });

    }

    /**
     * set the current bbox
     *
     * @param ip bbox ip
     */
    public void setCustomBbox(String ip) {
        mBbox = new MyBbox(ip);
    }

    /**
     * Return the current bbox. null if not correctly initialized !
     *
     * @return the bbox.
     */
    public MyBbox getBbox() throws MyBboxNotFoundException {
        if (mBbox == null) {
            throw new MyBboxNotFoundException();
        }
        return mBbox;
    }

    public static BboxHolder getInstance() {
        return mInstance;
    }

}
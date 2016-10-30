package com.hawk.funday.support.analytics;

import android.content.Context;
import android.os.Bundle;

import com.tma.analytics.AnalyticsConfig;
import com.tma.analytics.ISessionAgent;
import com.tma.analytics.SPage;
import com.tma.analytics.TLogger;
import com.tma.analytics.Utils;

import java.util.Set;

/**
 * Created by wangdan on 16/9/20.
 */
public class FirebaseAgent implements ISessionAgent {

    private static final String TAG = FirebaseAgent.class.getSimpleName();

    String fr;

    SPage spage;

    long t;

    Context context;

    public FirebaseAgent(Context context) {
        this.context = context;
    }

    @Override
    public void onFrom(String fr) {
        this.fr = fr;
    }

    @Override
    public void onResume(Context context) {
        if (AnalyticsConfig.ACTIVITY_DURATION_OPEN) {
            onPageStart(context.getClass().getName());
        }

        if (spage != null) {
            spage.pst = Utils.time();;
        }

        checkSession();
    }

    @Override
    public void onPause(Context context) {
        t = Utils.time();

        if (AnalyticsConfig.ACTIVITY_DURATION_OPEN) {
            onPageEnd(context.getClass().getName());
        }

        if (spage != null) {
            spage.pet = Utils.time();;

            // 记录页面跳转事件
            Bundle args = new Bundle();
            args.putLong("duration", spage.pet - spage.pet);
            Stats.logEvent(spage.pn, args);

            if (spage.pst > 0) {
                TLogger.d(TAG, "add page[%s], duration[%s]", spage.pn, String.valueOf(spage.pet - spage.pst));
            }
        }
    }

    @Override
    public void onPageStart(String page) {
        spage = new SPage(page);
    }

    @Override
    public void onPageEnd(String page) {
        if (spage != null && spage.pn.equals(page) && spage.pst > 0) {
        }
        else {
            spage = null;
        }
    }

    @Override
    public void onEvent(String eventId, long d) {
        onEvent(eventId, d, null);
    }

    @Override
    public void onEvent(String eventId, long d, Bundle args) {
        if (args == null) {
            args = new Bundle();
        }
        if (d > 0) {
            args.putLong("duration", d);
        }
        Stats.logEvent(eventId, args);

        if (TLogger.DEBUG) {
            StringBuffer sb = new StringBuffer();
            if (args.size() > 0) {
                Set<String> keySet = args.keySet();
                for (String key : keySet) {
                    sb.append(args.get(key) + ", ");
                }
            }
            TLogger.v(TAG, "add event[%s], duration[%s], args[%s]", eventId, d + "", sb.toString());
        }
    }

    @Override
    public void checkSession() {
    }

}

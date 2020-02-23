package jn.mjz.aiot.jnuetc.kotlin.model.custom;

import android.os.Handler;

import androidx.annotation.NonNull;

/**
 * @author qq1962247851
 */
public class Timer {

    private int day = 0;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;

    private Handler handler = new Handler();
    private boolean pause = false;
    private static final int MAX_HOUR = 24;
    private static final int MAX_MINUTE = 60;
    private static final int MAX_SECOND = 60;
    private Runnable runnable;

    public Timer() {
    }

    @NonNull
    @Override
    public String toString() {
        return day + "day," + hour + "hour," + minute + "minute," + second + "second.";
    }

    public Timer(int day, int hour, int minute, int second) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    public void startTiming(IOnUpdateListener i) {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (!isPause()) {
                        addSecond();
                        i.onAdded(Timer.this);
                        handler.postDelayed(this, 1000);
                    }
                }
            };
        }
        runnable.run();
    }

    public Boolean isPause() {
        return pause;
    }

    public void pause() {
        if (!pause) {
            pause = true;
        }
    }

    public void resume() {
        if (pause) {
            pause = false;
        }
    }

    public void stopTiming() {
        if (runnable != null) {
            runnable = null;
        }
        pause = true;
    }

    public void endTiming() {
        pause = true;
    }

    private void addSecond() {
        if (++second >= MAX_SECOND) {
            second = 0;
            addMinute();
        }
    }

    private void addMinute() {
        if (++minute >= MAX_MINUTE) {
            minute = 0;
            addHour();
        }
    }

    private void addHour() {
        if (++hour >= MAX_HOUR) {
            hour = 0;
            addDay();
        }
    }

    private void addDay() {
        day++;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public interface IOnUpdateListener {
        /**
         * 增加
         *
         * @param timer Time
         */
        void onAdded(Timer timer);
    }

}

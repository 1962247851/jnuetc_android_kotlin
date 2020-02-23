package jn.mjz.aiot.jnuetc.kotlin.view.adapter.recyclerview;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.xframe.utils.log.XLog;

/**
 * @author qq1962247851
 * @date 2020/2/2 18:46
 */
public class MyOnScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = "MyOnScrollListener   ";
    private STATE currentState = STATE.NONE;
    private IStateChangeListener iStateChangeListener;

    public MyOnScrollListener(IStateChangeListener iStateChangeListener) {
        this.iStateChangeListener = iStateChangeListener;
    }

    public void resetState() {
        currentState = STATE.NONE;
    }

    public enum STATE {
        NONE,
        SCROLL_UP,
        SCROLL_DOWN,
        FULL_ON_SCREEN,
        ARRIVED_TOP,
        ARRIVED_BOTTOM,
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState != 0) {
            if (!recyclerView.canScrollVertically(-1) && !recyclerView.canScrollVertically(1)) {
                if (currentState != STATE.FULL_ON_SCREEN) {
                    currentState = STATE.FULL_ON_SCREEN;
                    iStateChangeListener.OnStateChange(currentState);
                    XLog.d(TAG + "FULL_ON_SCREEN");
                }
            }
        }
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        STATE newState = currentState;
        if (!recyclerView.canScrollVertically(1)
                && !recyclerView.canScrollVertically(-1)) {
            if (currentState != STATE.FULL_ON_SCREEN) {
                newState = STATE.FULL_ON_SCREEN;
                XLog.d(TAG + "FULL_ON_SCREEN");
            }
        } else if (!recyclerView.canScrollVertically(-1)) {
            if (currentState != STATE.ARRIVED_TOP) {
                //滑动到顶部
                newState = STATE.ARRIVED_TOP;
                XLog.d(TAG + "SCROLL_TO_TOP");
            }
        } else if (!recyclerView.canScrollVertically(1)) {
            if (currentState != STATE.ARRIVED_BOTTOM) {
                //滑动到底部
                newState = STATE.ARRIVED_BOTTOM;
                XLog.d(TAG + "SCROLL_TO_BOTTOM");
            }
        } else if (recyclerView.canScrollVertically(1)
                && recyclerView.canScrollVertically(-1)) {
            if (dy > 0) {
                if (currentState != STATE.SCROLL_DOWN) {
                    newState = STATE.SCROLL_DOWN;
                    XLog.d(TAG + "SCROLL_DOWN");
                }
            } else {
                if (currentState != STATE.SCROLL_UP) {
                    newState = STATE.SCROLL_UP;
                    XLog.d(TAG + "SCROLL_UP");
                }
            }
        }
        if (currentState != newState) {
            currentState = newState;
            iStateChangeListener.OnStateChange(currentState);
        }
        super.onScrolled(recyclerView, dx, dy);
    }

    public interface IStateChangeListener {
        void OnStateChange(STATE state);
    }
}

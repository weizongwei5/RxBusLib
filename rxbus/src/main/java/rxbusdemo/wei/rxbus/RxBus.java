package rxbusdemo.wei.rxbus;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observers.SafeSubscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wei on 16-9-10.
 *
 *
 */
public class RxBus {

    private static RxBus instance;

    //use SparseArray,because is high performance
    SparseArray<Action1> mSparseAction;
    CompositeSubscription mCompositeSubscription;

    private RxBus() {

        mSparseAction = new SparseArray<>();
        mCompositeSubscription=new CompositeSubscription();
    }


    public static RxBus getInstance() {

        if (instance == null) {
            instance = new RxBus();
        }
        return instance;
    }


    public void register(String filter, Action1 action1) {

        if (!TextUtils.isEmpty(filter) && action1 != null) {
            mSparseAction.put(filter.hashCode(), action1);
        }
    }

    /**
     * 解绑
     * @param filter
     */
    public void unRegister(String filter) {

        if (TextUtils.isEmpty(filter))
            return;

        if (mSparseAction.indexOfKey(filter.hashCode()) > -1) {
            mSparseAction.remove(filter.hashCode());
        }
    }

    /**
     * 解绑
     * @param action1
     */
    public void unRegister(Action1 action1) {

        if (action1 == null)
            return;

        int index=mSparseAction.indexOfValue(action1);
        if (index> -1) {
            mSparseAction.remove(index);
        }
    }


    /**
     * 不带线程切换 功能
     * action的触发会在发送的observable所在线程线程执行
     *
     * @param filter
     * @param observable
     */
    public void sendBroadCast(String filter, Observable observable) {

        if (TextUtils.isEmpty(filter) || observable == null)
            return;

        if (mSparseAction.indexOfKey(filter.hashCode()) > -1) {//设计的 就像  广播一样 发送出来 如果没有人接受 就丢弃了

            observable.subscribe(mSparseAction.get(filter.hashCode()));

            Log.d("on send ,isUnsubscribed",observable.subscribe().isUnsubscribed()+"");
        }
    }


    /**
     * 会在IO线程执行的 sendBroadCast方法
     *
     * @param filter
     * @param observable
     */
    public void sendBroadOnIO(String filter, Observable observable) {

        if (TextUtils.isEmpty(filter) || observable == null)
            return;

        sendBroadCast(filter, observable.observeOn(Schedulers.io()));
    }

    /**
     * 会在IO线程执行的 sendBroadCast方法
     *
     * @param filter
     * @param observable
     */
    public void sendBroadOnUI(String filter, Observable observable) {

        if (TextUtils.isEmpty(filter) || observable == null)
            return;

        sendBroadCast(filter, observable.observeOn(AndroidSchedulers.mainThread()));
    }


    /**
     * 会在新线程执行的 sendBroadCast方法
     *
     * @param filter
     * @param observable
     */
    public void sendBroadOnNewThread(String filter, Observable observable) {

        if (TextUtils.isEmpty(filter) || observable == null)
            return;

        sendBroadCast(filter, observable.observeOn(Schedulers.newThread()));
    }

}

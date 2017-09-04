package com.fran.test.go.module.manage.download.cache;

import com.dl7.downloaderlib.model.DownloadStatus;
import com.dl7.mvp.local.table.VideoInfo;
import com.dl7.mvp.local.table.VideoInfoDao;
import com.dl7.mvp.module.base.ILocalView;
import com.dl7.mvp.module.base.IRxBusPresenter;
import com.dl7.mvp.rxbus.RxBus;
import com.dl7.mvp.utils.ListUtils;
import com.orhanobut.logger.Logger;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by long on 2016/12/16.
 * video 缓存Presenter
 */
public class VideoCachePresenter implements IRxBusPresenter {

    private final ILocalView mView;
    private final VideoInfoDao mDbDao;
    private final RxBus mRxBus;

    public VideoCachePresenter(ILocalView view, VideoInfoDao dbDao, RxBus rxBus) {
        mView = view;
        mDbDao = dbDao;
        mRxBus = rxBus;
    }

    @Override
    public void getData(boolean isRefresh) {
        mDbDao.queryBuilder().rx()
                .oneByOne()
                .filter(new Func1<VideoInfo, Boolean>() {
                    @Override
                    public Boolean call(VideoInfo info) {
                        // 判断是否存于下载中
                        return (info.getDownloadStatus() != DownloadStatus.NORMAL &&
                                info.getDownloadStatus() != DownloadStatus.COMPLETE);
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<VideoInfo>>() {
                    @Override
                    public void call(List<VideoInfo> videoList) {
                        if (ListUtils.isEmpty(videoList)) {
                            mView.noData();
                        } else {
                            mView.loadData(videoList);
                        }
                    }
                });
    }

    @Override
    public void getMoreData() {
    }

    @Override
    public <T> void registerRxBus(Class<T> eventType, Action1<T> action) {
        Subscription subscription = mRxBus.doSubscribe(eventType, action, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Logger.e(throwable.toString());
            }
        });
        mRxBus.addSubscription(this, subscription);
    }

    @Override
    public void unregisterRxBus() {
        mRxBus.unSubscribe(this);
    }
}

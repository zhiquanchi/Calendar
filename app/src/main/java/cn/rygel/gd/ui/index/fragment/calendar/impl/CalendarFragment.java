package cn.rygel.gd.ui.index.fragment.calendar.impl;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.rygel.gd.R;
import cn.rygel.gd.bean.OnDateEventDeleteAllEvent;
import cn.rygel.gd.bean.OnDrawerStateChangeEvent;
import cn.rygel.gd.bean.OnEventAddedEvent;
import cn.rygel.gd.bean.OnWeekDayOffsetSelectEvent;
import cn.rygel.gd.setting.Settings;
import cn.rygel.gd.widget.timeline.TimeLineView;
import cn.rygel.gd.widget.timeline.bean.TimeLineItem;
import cn.rygel.gd.ui.event.impl.AddEventActivity;
import cn.rygel.gd.ui.index.fragment.calendar.ICalendarView;
import cn.rygel.gd.utils.calendar.CalendarUtils;
import cn.rygel.gd.utils.calendar.LunarUtils;
import cn.rygel.gd.widget.calendar.impl.CalendarView;
import cn.rygel.gd.widget.calendar.listener.OnDateSelectedListener;
import cn.rygel.gd.widget.calendar.listener.OnMonthChangedListener;
import rygel.cn.uilibrary.mvp.BaseFragment;
import rygel.cn.uilibrary.utils.UIUtils;

public class CalendarFragment extends BaseFragment<CalendarPresenter> implements ICalendarView {

    @BindView(R.id.tb_main)
    Toolbar mToolbar;

    @BindView(R.id.cv_calendar)
    CalendarView mCalendarView;

    @BindView(R.id.tl_calendar)
    TimeLineView mTimeLine;

    @Override
    protected CalendarPresenter createPresenter() {
        return new CalendarPresenter();
    }

    @Override
    protected void initView(View view) {
        EventBus.getDefault().register(this);
        ButterKnife.bind(this,view);
        mToolbar.setNavigationOnClickListener(l -> {
            EventBus.getDefault().post(new OnDrawerStateChangeEvent(true));
        });
        initCalendarView();
        initTimeLine();
    }

    private void initCalendarView(){
        mCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int year, int month) {
                CalendarFragment.this.onMonthChanged(year, month);
            }
        });
        mCalendarView.setSelect(CalendarUtils.today());
        mCalendarView.setCalendarOptions(
                mCalendarView
                        .getCalendarOptions()
                        .setDateOffset(
                                Settings
                                        .getInstance()
                                        .getWeekdayOffset()
                        )
        );
        mCalendarView.setOnDateSelectListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelect(LunarUtils.Solar date) {
                mTimeLine.setDate(date,true);
            }

            @Override
            public void onDateLongClick(LunarUtils.Solar date) {
                mTimeLine.setDate(date,true);
            }
        });
    }

    private void initTimeLine(){
        mTimeLine.setDateSelectListener(new TimeLineView.IDateSelectListener() {
            @Override
            public void onDateSelect(LunarUtils.Solar date) {
                mCalendarView.setSelect(date);
            }
        });
        mTimeLine.setLoadMoreListener(new TimeLineView.ILoadMoreListener() {
            @Override
            public void onLoadMore(LunarUtils.Solar start, int interval, boolean isStart) {
                getPresenter().loadEventItemsInRange(start, interval, isStart);
            }
        });
    }

    @OnClick(R.id.fab_add_event)
    protected void addEvent(){
        AddEventActivity.start(getContext(),null,null,null);
    }

    @Override
    protected void loadData() {
        LunarUtils.Solar start = CalendarUtils.today();
        start.solarDay = 1;
        getPresenter().loadEventItemsInRange(start,CalendarUtils.getMonthDay(start.solarYear,start.solarMonth) - 1,true);
    }

    private void reloadData() {
        if(mTimeLine.getData() != null && mTimeLine.getData().size() > 0){
            TimeLineItem start = mTimeLine.getData().get(0);
            TimeLineItem end = mTimeLine.getData().get(mTimeLine.getData().size() - 1);
            mTimeLine.clear();
            getPresenter().loadEventItemsInRange(start.getDate(),CalendarUtils.getIntervalDays(start.getDate(),end.getDate()),true);
        }
    }

    @Override
    public void showEvents(List<TimeLineItem> items, boolean isStart) {
        if(isStart) {
            mTimeLine.addEvents(0,items);
        } else {
            mTimeLine.addEvents(items);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calendar;
    }

    @Override
    public void refresh() {
        Logger.d("do not support refresh action!");
    }

    private void onMonthChanged(int year,int month){
        Logger.i("on month changed : year : " + year + " month : " + month);
        mToolbar.setTitle(UIUtils.getString(getContext(),R.string.yyyy_MM,year,month));
    }

    private void onDateSelect(LunarUtils.Solar solar){
        Logger.i("select date : " +  solar);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDrawerStateChanged(OnEventAddedEvent event) {
        // 重新加载数据
        reloadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDrawerStateChanged(OnWeekDayOffsetSelectEvent event) {
        mCalendarView.setCalendarOptions(
                mCalendarView
                        .getCalendarOptions()
                        .setDateOffset(
                                event.getOffset()
                        )
        );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDelete(OnDateEventDeleteAllEvent event) {
        reloadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

package cn.rygel.gd.widget.calendar.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.rygel.gd.utils.LunarUtils;
import cn.rygel.gd.widget.calendar.bean.CalendarData;
import cn.rygel.gd.widget.calendar.bean.ParamBean;
import cn.rygel.gd.widget.calendar.helper.CustomCalendarItem;
import cn.rygel.gd.widget.calendar.helper.CustomCalendarWeekItem;
import cn.rygel.gd.widget.calendar.impl.helper.DefaultCalendarItem;
import cn.rygel.gd.widget.calendar.impl.helper.DefaultCalendarWeekItem;
import cn.rygel.gd.widget.calendar.listener.OnDateSelectedListener;

public class RealCalendarView extends View {

    private static final long LONG_CLICK_DURATION = 3 * 1000;

    private Rect mBound = new Rect();
    private Rect mChildBound = new Rect();
    private Rect mWeekBarBound = new Rect();
    private int mWeekBarHeight = 0;
    private int mChildPaddingLeft = 0;
    private int mChildPaddingRight = 0;
    private int mChildPaddingTop = 0;
    private int mChildPaddingBottom = 0;

    private CalendarData mCalendarData = null;

    private int mSelectItemLeftOffset = 0;
    private int mSelectItemTopOffset = 0;

    private int mSelectIndex = -1;

    private int mDownX = 0;
    private int mDownY = 0;

    private long mDownTime = 0L;

    private CustomCalendarItem mCustomCalendarItem = new DefaultCalendarItem();
    private CustomCalendarWeekItem mCustomCalendarWeekItem = new DefaultCalendarWeekItem();

    private OnDateSelectedListener mOnDateSelectedListener = null;

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onActionUp(event);
                    break;
            }
            return true;
        }
    };

    /**
     * 构造函数
     * @param context
     */
    public RealCalendarView(Context context){
        this(context,null);
    }

    /**
     * 构造函数
     * @param context
     * @param attrs
     */
    public RealCalendarView(Context context, AttributeSet attrs){
        super(context, attrs);
        setClickable(true);
        setOnTouchListener(mTouchListener);
    }

    protected void onActionDown(MotionEvent event){
        mDownX = (int) event.getX();
        mDownY = (int) event.getY();
        mDownTime = System.currentTimeMillis();
    }

    protected void onActionUp(MotionEvent event){
        if(Math.abs(event.getX() - mDownX) > 20 || Math.abs(event.getY() - mDownY) > 20){
            return;
        }
        mSelectIndex = getSelectItem(event.getX(),event.getY());
        invalidate();
        if(mOnDateSelectedListener != null) {
            LunarUtils.Solar select = new LunarUtils.Solar(mCalendarData.year,mCalendarData.month,mSelectIndex + 1);
            if(System.currentTimeMillis() - mDownTime > LONG_CLICK_DURATION) {
                mOnDateSelectedListener.onDateLongClick(select);
            } else {
                mOnDateSelectedListener.onDateSelect(select);
            }
        }
    }

    protected int getSelectItem(float x, float y){
        if(x < mBound.left || x > mBound.right){
            return -1;
        }
        if(y < mBound.top || y > mBound.bottom){
            return -1;
        }
        final int column = (int) ((x - mBound.left) / (mChildBound.width() + mChildPaddingLeft + mChildPaddingRight));
        final int row = (int) ((y - mBound.top) / (mChildBound.height() + mChildPaddingTop + mChildPaddingBottom));
        int index = column + mCalendarData.mWeekDayInfo.length * row - mCalendarData.mStartIndex;
        if(index >= mCalendarData.mDays){
            return -1;
        }
        return index;
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        mWeekBarBound.left = paddingLeft;
        mWeekBarBound.top = paddingTop;
        mWeekBarBound.right = getMeasuredWidth() - paddingRight;
        mWeekBarBound.bottom = paddingTop + mWeekBarHeight;

        mBound.left = paddingLeft;
        mBound.top = mWeekBarBound.bottom;
        mBound.right = getMeasuredWidth() - paddingRight;
        mBound.bottom = getMeasuredHeight() - paddingBottom;
    }

    /**
     * 获取默认的宽高值
     */
    public static int getDefaultSize (int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mCalendarData != null){
            mCustomCalendarWeekItem.drawWeekDays(canvas,mWeekBarBound,mCalendarData.mWeekDayInfo);
            int maxWeek = (mCalendarData.mDays + mCalendarData.mStartIndex) / 7 +
                    ((mCalendarData.mDays + mCalendarData.mStartIndex) % 7 == 0 ? 0 : 1);
            for(int i = 0;i < mCalendarData.mDays;i++){
                if(mSelectIndex != i){
                    mCustomCalendarItem.drawDateItem(canvas,getChildBound(i + mCalendarData.mStartIndex,maxWeek),mCalendarData,i);
                }else {
                    Rect select = getChildBound(i + mCalendarData.mStartIndex,maxWeek);
                    select.offset(-mSelectItemLeftOffset,-mSelectItemTopOffset);
                    mCustomCalendarItem.drawSelectItem(canvas,select,mCalendarData,i);
                }
            }
            if(mCalendarData.mTodayIndex > 0 && mSelectIndex != mCalendarData.mTodayIndex){
                mCustomCalendarItem.drawTodayItem(canvas, getChildBound(mCalendarData.mTodayIndex + mCalendarData.mStartIndex,maxWeek),mCalendarData);
            }
        }
    }

    private Rect getChildBound(int index,int maxWeek){
        final int column = index % 7;
        final int row = index / 7;
        final int itemWidth = mBound.width() / 7;
        final int itemHeight = mBound.height() / maxWeek;
        mChildBound.left = mBound.left + column * itemWidth + mChildPaddingLeft;
        mChildBound.right = mBound.left + (column + 1) * itemWidth - mChildPaddingRight;
        mChildBound.top = mBound.top + row * itemHeight + mChildPaddingTop;
        mChildBound.bottom = mBound.top + (row + 1) * itemHeight - mChildPaddingBottom;
        return mChildBound;
    }

    public void setParam(ParamBean param){
        setWeekBarHeight(param.getWeekBarHeight());
        setWeekDayTextSize(param.getWeekDayTextSize());
        setWeekDayTextColor(param.getWeekDayTextColor());
        setSelectTextColor(param.getSelectTextColor());
        setDateTextSize(param.getDateTextSize());
        setLunarTextSize(param.getLunarTextSize());
        setHolidayTextSize(param.getHolidayTextSize());
        setPrimaryColor(param.getPrimaryColor());
        setAccentColor(param.getAccentColor());
        setTextColor(param.getTextColor());
        setChildPaddingLeft(param.getChildPaddingLeft());
        setChildPaddingRight(param.getChildPaddingRight());
        setChildPaddingTop(param.getChildPaddingTop());
        setChildPaddingBottom(param.getChildPaddingBottom());
    }

    public void setSelectIndex(int selectIndex) {
        if(selectIndex == mSelectIndex) {
            return;
        }
        mSelectIndex = selectIndex;
        postInvalidate();
    }

    public void setWeekBarHeight(int weekBarHeight) {
        if(weekBarHeight == mWeekBarHeight) {
            return;
        }
        mWeekBarHeight = weekBarHeight;
        postInvalidate();
    }

    public void setWeekDayTextSize(int textSize) {
        mCustomCalendarWeekItem.setWeekDayTextSize(textSize);
        postInvalidate();
    }

    public void setWeekDayTextColor(int textColor) {
        mCustomCalendarWeekItem.setWeekDayTextColor(textColor);
        postInvalidate();
    }

    public void setSelectTextColor(int selectTextColor) {
        mCustomCalendarItem.setSelectTextColor(selectTextColor);
        postInvalidate();
    }

    public void setDateTextSize(int dateTextSize) {
        mCustomCalendarItem.setDateTextSize(dateTextSize);
        postInvalidate();
    }

    public void setLunarTextSize(int lunarTextSize) {
        mCustomCalendarItem.setLunarTextSize(lunarTextSize);
        postInvalidate();
    }

    public void setHolidayTextSize(int holidayTextSize) {
        mCustomCalendarItem.setHolidayTextSize(holidayTextSize);
        postInvalidate();
    }

    public void setPrimaryColor(int primaryColor) {
        mCustomCalendarItem.setPrimaryColor(primaryColor);
        postInvalidate();
    }

    public void setAccentColor(int accentColor) {
        mCustomCalendarItem.setAccentColor(accentColor);
        postInvalidate();
    }

    public void setTextColor(int textColor) {
        mCustomCalendarItem.setTextColor(textColor);
        postInvalidate();
    }

    public int getChildPaddingLeft() {
        return mChildPaddingLeft;
    }

    public void setChildPaddingLeft(int childPaddingLeft) {
        mChildPaddingLeft = childPaddingLeft;
        postInvalidate();
    }

    public int getChildPaddingRight() {
        return mChildPaddingRight;
    }

    public void setChildPaddingRight(int childPaddingRight) {
        mChildPaddingRight = childPaddingRight;
        postInvalidate();
    }

    public int getChildPaddingTop() {
        return mChildPaddingTop;
    }

    public void setChildPaddingTop(int childPaddingTop) {
        mChildPaddingTop = childPaddingTop;
        postInvalidate();
    }

    public int getChildPaddingBottom() {
        return mChildPaddingBottom;
    }

    public void setChildPaddingBottom(int childPaddingBottom) {
        mChildPaddingBottom = childPaddingBottom;
        postInvalidate();
    }

    public CalendarData getCalendarData() {
        return mCalendarData;
    }

    public void setCalendarData(CalendarData calendarData) {
        mCalendarData = calendarData;
        postInvalidate();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        mOnDateSelectedListener = onDateSelectedListener;
    }

    protected int getSelectIndex() {
        return mSelectIndex;
    }
}
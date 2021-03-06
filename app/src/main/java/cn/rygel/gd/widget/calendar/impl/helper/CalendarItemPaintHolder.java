package cn.rygel.gd.widget.calendar.impl.helper;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

public class CalendarItemPaintHolder {

    private int mPrimaryColor = Color.BLACK;

    private int mTextColor = Color.BLACK;

    private int mSelectTextColor = Color.BLACK;

    private int mHolidayTextColor = Color.BLACK;

    private int mHolidayBreakTextColor = Color.BLACK;

    private int mSolarTermsTextColor = Color.BLACK;

    private TextPaint mDatePaint = new TextPaint();

    private TextPaint mLunarPaint = new TextPaint();

    private TextPaint mOfficialHolidayPaint = new TextPaint();

    private TextPaint mHolidayPaint = new TextPaint();

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void setSelectTextColor(int selectTextColor) {
        mSelectTextColor = selectTextColor;
    }

    public void setDateTextSize(int dateTextSize) {
        mDatePaint.setTextSize(dateTextSize);
    }

    public void setLunarTextSize(int lunarTextSize) {
        mLunarPaint.setTextSize(lunarTextSize);
        mHolidayPaint.setTextSize(lunarTextSize);
    }

    public void setHolidayTextSize(int holidayTextSize) {
        mOfficialHolidayPaint.setTextSize(holidayTextSize);
    }

    public void setPrimaryColor(int primaryColor) {
        mPrimaryColor = primaryColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        mDatePaint.setColor(textColor);
        mLunarPaint.setColor(textColor);
    }

    public void setHolidayTextColor(int holidayTextColor) {
        mHolidayTextColor = holidayTextColor;
    }

    public void setHolidayBreakTextColor(int holidayBreakTextColor) {
        mHolidayBreakTextColor = holidayBreakTextColor;
    }

    public void setSolarTermsTextColor(int solarTermsTextColor) {
        mSolarTermsTextColor = solarTermsTextColor;
    }

    public TextPaint getDatePaint(boolean mode) {
        if(mode){
            mDatePaint.setColor(mSelectTextColor);
        } else {
            mDatePaint.setColor(mTextColor);
        }
        mDatePaint.setTextAlign(Paint.Align.CENTER);
        return mDatePaint;
    }

    public TextPaint getLunarPaint(boolean mode) {
        if(mode){
            mLunarPaint.setColor(mSelectTextColor);
        } else {
            mLunarPaint.setColor(mTextColor);
        }
        mLunarPaint.setTextAlign(Paint.Align.CENTER);
        return mLunarPaint;
    }

    public TextPaint getOfficialHolidayPaint(boolean mode) {
        if(mode){
            mOfficialHolidayPaint.setColor(mSelectTextColor);
        } else {
            mOfficialHolidayPaint.setColor(mHolidayTextColor);
        }
        mOfficialHolidayPaint.setTextAlign(Paint.Align.RIGHT);
        return mOfficialHolidayPaint;
    }

    public TextPaint getOfficialBreakPaint(boolean mode) {
        if(mode){
            mOfficialHolidayPaint.setColor(mSelectTextColor);
        } else {
            mOfficialHolidayPaint.setColor(mHolidayBreakTextColor);
        }
        mOfficialHolidayPaint.setTextAlign(Paint.Align.RIGHT);
        return mOfficialHolidayPaint;
    }

    public TextPaint getHolidayPaint(boolean mode) {
        if(mode){
            mHolidayPaint.setColor(mSelectTextColor);
        } else {
            mHolidayPaint.setColor(mHolidayTextColor);
        }
        mHolidayPaint.setTextAlign(Paint.Align.CENTER);
        return mHolidayPaint;
    }

    public Paint getBackgroundPaint(boolean mode) {
        mBackgroundPaint.setColor(mPrimaryColor);
        if(mode){
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setShadowLayer(20,10,10,Color.LTGRAY);
        } else {
            mBackgroundPaint.clearShadowLayer();
            mBackgroundPaint.setStyle(Paint.Style.STROKE);
            mBackgroundPaint.setStrokeWidth(5);
        }
        return mBackgroundPaint;
    }
}

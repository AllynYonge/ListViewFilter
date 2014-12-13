// @author Bhavya Mehta
package com.example.listviewfilter.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.listviewfilter.IIndexBarFilter;
import com.example.listviewfilter.R;

// Represents right side index bar view with unique first latter of list view row text 
public class IndexBarView extends View {

    // index bar margin
    int mIndexbarMargin;

    // user touched Y axis coordinate value，用户触摸的Y轴坐标值
    float mSideIndexY;

    // flag used in touch events manipulations，当down时为true，up时为false
    boolean mIsIndexing = false;

    // holds current section position selected by user，保存用户当前选择的位置
    int mCurrentSectionPosition = -1;

    // array list to store section positions，存储的所有的首字母在mListItems集合中的索引值
    public ArrayList<Integer> mListSections;

    // array list to store listView data
    ArrayList<String> mListItems;

    // paint object
    Paint mIndexPaint;

    // context object
    Context mContext;

    // interface object used as bridge between list view and index bar view for
    // filtering list view content on touch event
    IIndexBarFilter mIndexBarFilter;

    
    public IndexBarView(Context context) {
        super(context);
        this.mContext = context;
    }

    
    public IndexBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }
    

    public IndexBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }
    

    public void setData(PinnedHeaderListView listView, ArrayList<String> listItems,ArrayList<Integer> listSections) {
        this.mListItems = listItems;
        this.mListSections = listSections;
        
        // list view implements mIndexBarFilter interface
        mIndexBarFilter = listView;

        // set index bar margin from resources
        mIndexbarMargin = (int) mContext.getResources().getDimension(R.dimen.index_bar_view_margin);

        // index bar item color and text size
        mIndexPaint = new Paint();
        mIndexPaint.setColor(mContext.getResources().getColor(R.color.color_green));
        mIndexPaint.setAntiAlias(true);
        mIndexPaint.setTextSize(mContext.getResources().getDimension(R.dimen.index_bar_view_text_size));
    }
    // draw view content on canvas using paint
    @Override
    protected void onDraw(Canvas canvas) {
        if (mListSections != null && mListSections.size() > 1) {
            float sectionHeight = (getMeasuredHeight() - 2 * mIndexbarMargin)/ mListSections.size();
            float paddingTop = (sectionHeight - (mIndexPaint.descent() - mIndexPaint.ascent())) / 2;

            for (int i = 0; i < mListSections.size(); i++) {
                float paddingLeft = (getMeasuredWidth() - mIndexPaint.measureText(getSectionText(mListSections.get(i)))) / 2;

                canvas.drawText(getSectionText(mListSections.get(i)),
                        paddingLeft,
                        mIndexbarMargin + (sectionHeight * i) + paddingTop + mIndexPaint.descent(),
                        mIndexPaint);
            }
        }
        super.onDraw(canvas);
    }

    
    public String getSectionText(int sectionPosition) {
        return mListItems.get(sectionPosition);
    }

    
    boolean contains(float x, float y) {
        //判断当前触摸的点是否属于IndexBarView这个控件的范围内(包括margin值)
        return (x >= getLeft() && y >= getTop() && y <= getTop() + getMeasuredHeight());
    }

    
    void filterListItem(float sideIndexY) {
        mSideIndexY = sideIndexY;

        // filter list items and get touched section position with in index bar
        //getMeasuredHeight()即包括了padding值不包括了margin值

        System.out.println("getTop():"+getTop());
        System.out.println("mSideIndexY:"+mSideIndexY);
        System.out.println("mIndexbarMargin:"+mIndexbarMargin);
        System.out.println("mIndexbarPadding:"+getPaddingBottom());
        System.out.println("getMeasuredHeight():"+getMeasuredHeight());
        System.out.println("getHeight():"+getHeight());


        mCurrentSectionPosition = (int) (((mSideIndexY) - mIndexbarMargin) /
                                    ((getMeasuredHeight() - (2 * mIndexbarMargin)) / mListSections.size()));

        if (mCurrentSectionPosition >= 0 && mCurrentSectionPosition < mListSections.size()) {
            int position = mListSections.get(mCurrentSectionPosition);
            String previewText = mListItems.get(position);
            mIndexBarFilter.filterList(mSideIndexY, position, previewText);
        }
    }

    public int getIndexBarMargin(){
        return mIndexbarMargin;
    }
    
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            
            case MotionEvent.ACTION_DOWN:
                // If down event occurs inside index bar region, start indexing
                if (contains(ev.getX(), ev.getY())) {
                    // It demonstrates that the motion event started from index
                    // bar
                    mIsIndexing = true;
                    // Determine which section the point is in, and move the
                    // list to
                    // that section
                    //ev.getY：当前触摸点距离IndexBarView自己顶部的距离
                    filterListItem(ev.getY());
                    return true;
                }
                else {
                    mCurrentSectionPosition = -1;
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                if (mIsIndexing) {
                    // If this event moves inside index bar
                    if (contains(ev.getX(), ev.getY())) {
                        // Determine which section the point is in, and move the
                        // list to that section
                        filterListItem(ev.getY());
                        return true;
                    }
                    else {
                        mCurrentSectionPosition = -1;
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsIndexing) {
                    mIsIndexing = false;
                    mCurrentSectionPosition = -1;
                }
                break;
        }
        return false;
    }
}

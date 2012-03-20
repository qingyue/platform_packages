/**
 * 
 */
package com.onyx.android.sdk.ui;

import java.util.ArrayList;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.onyx.android.sdk.ui.data.OnyxPagedAdapter;
import com.onyx.android.sdk.ui.util.IBoundaryItemLocator;
import com.onyx.android.sdk.ui.util.OnyxFocusFinder;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * base class for pagination grid view
 * 
 * tighten coupled with OnyxPagedAdapter
 * 
 * @author joy
 *
 */
public class OnyxGridView extends GridView implements IBoundaryItemLocator, GestureDetector.OnGestureListener
{
    private static final String TAG = "OnyxGridView";

    public interface OnAdapterChangedListener
    {
        void onAdapterChanged();
    }
    public interface OnSizeChangedListener
    {
        void onSizeChanged();
    }
    public interface OnLongPressListener
    {
        void onLongPress();
    }

    private ArrayList<OnAdapterChangedListener> mOnAdapterChangedListenerList = new ArrayList<OnAdapterChangedListener>();
    public void registerOnAdapterChangedListener(OnAdapterChangedListener l)
    {
        mOnAdapterChangedListenerList.add(l);
    }
    public void unregisterOnAdapterChangedListener(OnAdapterChangedListener l)
    {
        mOnAdapterChangedListenerList.remove(l);
    } 

    private ArrayList<OnSizeChangedListener> mOnSizeChangedListenerList = new ArrayList<OnSizeChangedListener>();
    public void registerOnSizeChangedListener(OnSizeChangedListener l)
    {
        mOnSizeChangedListenerList.add(l);
    }
    public void unregisterOnSizeChangedListener(OnSizeChangedListener l)
    {
        mOnSizeChangedListenerList.remove(l);
    }

    private ArrayList<OnLongPressListener> mOnLongPressListenerList = new ArrayList<OnLongPressListener>();
    public void registerOnLongPressListener(OnLongPressListener l)
    {
        mOnLongPressListenerList.add(l);
    }
    public void unregisterOnLongPressListener(OnLongPressListener l)
    {
        mOnLongPressListenerList.remove(l);
    }

    // TODO should use "dp" as unit
    private static final int sMinFlingLength = 10;

    private OnyxPagedAdapter mAdapter = null;

    private boolean mCrossVertical = false;
    private boolean mCrossHorizon = false;
    private int mSelectionInTouchMode = AdapterView.INVALID_POSITION;

    private GestureDetector mGestureDetector = null;

    private float mDownXLength = 0;
    private float mUpXLength = 0;
    private long mDownTime = 0;

    public OnyxGridView(Context context) {
        this(context, null);
    }

    public OnyxGridView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gridViewStyle);
    }

    public OnyxGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // default behavior as non-paged GridView
        this.setCrossHorizon(true);
        this.setCrossVertical(true);

        // by disabling ScrollBar, we can eliminate noisy onDraw when navigating items in GridView
        // same effect as android:scrollbars="none" style
        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);

        this.setupUIListeners();
    }

    /**
     * warning: ListAdapter is not accepted, use OnyxBasePagedAdapter instead
     */
    @Override
    public void setAdapter(ListAdapter adapter)
    {
        throw new IllegalArgumentException();
    }

    public void setAdapter(OnyxPagedAdapter adapter) 
    {
        if (mAdapter != adapter) {
            super.setAdapter(adapter); 

            mAdapter = adapter;
            adapter.getPageLayout().setupLayout(this);

            for (OnAdapterChangedListener l : mOnAdapterChangedListenerList) {
                l.onAdapterChanged();
            }
        }
    }

    /**
     * in convenience, use getOnyxAdapter() instead
     * override just for commentary
     */
    @Override
    public ListAdapter getAdapter()
    {
        return super.getAdapter();
    }
    // simple wrapper around getAdapter()
    public OnyxPagedAdapter getPagedAdapter()
    {
        return (OnyxPagedAdapter)this.getAdapter();
    }

    public void setCrossVertical(boolean value)
    {
        mCrossVertical = value;
    }

    public void setCrossHorizon(boolean value) 
    {
        mCrossHorizon = value;
    }

    public int getSelectionInTouchMode()
    {
        return mSelectionInTouchMode;
    }
    private void setSelectionInTouchMode(int value)
    {
        mSelectionInTouchMode = value;
    }

    @Override
    public void setSelection(int position)
    {
        Log.d(TAG, "setSelection: " + position);
        this.requestFocus();
        super.setSelection(position);
    }

    @Override
    public View getSelectedView()
    {
        if (this.isInTouchMode()) {
            if (super.getSelectedView() != null) {
                return super.getSelectedView();
            }
            else {
                if (mSelectionInTouchMode != AdapterView.INVALID_POSITION) {
                    return this.getChildAt(mSelectionInTouchMode);
                }
                else {
                    return null;
                }
            }
        }

        return super.getSelectedView();
    }

    // ========================= IBoundaryItemLocator ======================
    /**
     *  
     * @param srcRect
     * @param boundarySide
     */
    @Override
    public void selectBoundaryItemBySearch(Rect srcRect, BoundarySide boundarySide)
    {
        int item_count = this.getCount();

        if (item_count <= 0) {
            return;
        }
        else if (item_count == 1) {
            this.setSelection(0);
            return;
        }
        else {
            final int columns = Math.min(this.getCount(), mAdapter.getPageLayout().getLayoutColumnCount()); 
            if (columns <= 0) {
                return;
            }

            final int column_mod = item_count % columns; 
            final int rows = (this.getCount() / columns) + ((column_mod > 0) ? 1 : 0);

            if (srcRect == null) {
                switch (boundarySide) {
                case TOP:
                    this.setSelection(0);
                    break;
                case BOTTOM:
                    this.setSelection((rows -1 ) * columns);
                    break;
                case LEFT:
                    this.setSelection(0);
                    break;
                case RIGHT:
                    this.setSelection(columns - 1);
                    break;
                default:
                    assert(false);
                    throw new IndexOutOfBoundsException();
                }

                return;
            }

            if (boundarySide == BoundarySide.TOP) {
                if (srcRect.left < OnyxFocusFinder.getAbsoluteLeft(this.getChildAt(0))) {
                    this.setSelection(0);
                    return;
                }
                else if (srcRect.left > OnyxFocusFinder.getAbsoluteRight(this.getChildAt(columns - 1))) {
                    this.setSelection(columns - 1);
                    return;
                }
                else {
                    int min_distance = Integer.MAX_VALUE;
                    int best_item_index = 0;

                    for (int i = 0; i < columns; i++) {
                        int distance = Math.abs(OnyxFocusFinder.getAbsoluteLeft(this.getChildAt(i)) - srcRect.left);
                        if (distance < min_distance) {
                            min_distance = distance;
                            best_item_index = i;
                        }
                    }

                    this.setSelection(best_item_index);
                    return;
                }
            }
            else if (boundarySide == BoundarySide.BOTTOM) {
                int base_index = (rows - 1) * columns;

                if (srcRect.left < OnyxFocusFinder.getAbsoluteLeft(this.getChildAt(base_index))) {
                    this.setSelection(base_index);
                    return;
                }
                else if (srcRect.left > OnyxFocusFinder.getAbsoluteRight(this.getChildAt(item_count - 1))) {
                    this.setSelection(item_count - 1);
                    return;
                }
                else {
                    int min_distance = Integer.MAX_VALUE;
                    int best_item_index = 0;

                    for (int i = base_index; i < item_count; i++) {
                        int distance = Math.abs(OnyxFocusFinder.getAbsoluteLeft(this.getChildAt(i)) - srcRect.left);
                        if (distance < min_distance) {
                            min_distance = distance;
                            best_item_index = i;
                        }
                    }

                    this.setSelection(best_item_index);
                    return;
                }
            }
            else if (boundarySide == BoundarySide.LEFT) {
                final int first_column_of_last_row = (rows - 1) * columns;

                if (srcRect.top < OnyxFocusFinder.getAbsoluteTop(this.getChildAt(0))) {
                    this.setSelection(0);
                    return;
                }
                else if (srcRect.top > OnyxFocusFinder.getAbsoluteBottom(this.getChildAt(first_column_of_last_row))) {
                    this.setSelection(first_column_of_last_row);
                    return;
                }
                else {
                    int min_distance = Integer.MAX_VALUE;
                    int best_item_index = 0;

                    for (int i = 0; i < rows; i++) {
                        final int current_idx = i * columns;
                        int distance = Math.abs(OnyxFocusFinder.getAbsoluteTop(this.getChildAt(current_idx)) - srcRect.top);
                        if (distance < min_distance) {
                            min_distance = distance;
                            best_item_index = current_idx;
                        }
                    }

                    this.setSelection(best_item_index);
                    return;
                }
            }
            else if (boundarySide == BoundarySide.RIGHT) {
                final int last_row_of_right_column = ((column_mod == 0) ? rows : rows - 1) - 1;
                final int right_column_last_row = (last_row_of_right_column * columns) + columns - 1;

                if (srcRect.top < OnyxFocusFinder.getAbsoluteTop(this.getChildAt(columns - 1))) {
                    this.setSelection(columns - 1);
                    return;
                }
                else if (srcRect.top > OnyxFocusFinder.getAbsoluteBottom(this.getChildAt(right_column_last_row))) {
                    this.setSelection(right_column_last_row);
                    return;
                }
                else {
                    int min_distance = Integer.MAX_VALUE;
                    int best_item_index = 0;

                    for (int i = 0; i <= last_row_of_right_column; i++) {
                        int current_right_column = (i * columns) + columns - 1;
                        int distance = Math.abs(OnyxFocusFinder.getAbsoluteTop(this.getChildAt(current_right_column)) - srcRect.top);
                        if (distance < min_distance) {
                            min_distance = distance;
                            best_item_index = current_right_column;
                        }
                    }

                    this.setSelection(best_item_index);
                    return;
                }
            }
            else {
                assert(false);
                throw new IndexOutOfBoundsException();
            }
        }
    }

    // ========================= GestureDetector.OnGestureListener ======================
    @Override
    public boolean onDown(MotionEvent e)
    {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e)
    {
        Log.d(TAG, "show press");
        Rect r = new Rect();
        for (int i = 0; i < this.getChildCount(); i++) {
            View v = this.getChildAt(i);
            r.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            if (r.contains((int)e.getX(), (int)e.getY())) {
                this.setSelectionInTouchMode(i);
                return;
            }
        }
        
        this.setSelectionInTouchMode(AdapterView.INVALID_POSITION);
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY)
    {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public void onLongPress(MotionEvent e)
    {
        Log.d(TAG, "long press"); 
        for (OnLongPressListener l : mOnLongPressListenerList) {
            l.onLongPress();
        }
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY)
    {
        if (e1.getX() - e2.getX() > sMinFlingLength) {
            if (mAdapter.getPaginator().canNextPage()) {
                mAdapter.getPaginator().nextPage();
            }
            return true;
        }
        else if (e2.getX() - e1.getX() > sMinFlingLength) {
            if (mAdapter.getPaginator().canPrevPage()) {
                mAdapter.getPaginator().prevPage();
            }
            return true;
        }

        return false;
    }

    /**
     * special focusable search method to select GridView's corresponding item
     * 
     * @param direction
     * @param previouslyFocusedRect
     * @return
     */
    public boolean searchAndSelectNextFocusableChildItem(int direction, Rect previouslyFocusedRect)
    {
        if (this.getChildCount() > 0) {
            BoundarySide side = BoundarySide.valueOf(direction);
            if (side != BoundarySide.NONE) {
                this.selectBoundaryItemBySearch(previouslyFocusedRect, side);
                return true;
            }
        }

        return false;
    }

    private void setupUIListeners()
    {
        mGestureDetector = new GestureDetector(this.getContext(), this);

        // when dealing with long press, override onTouchEvent method of GridView could not work properly,
        // so set listener instead
        this.setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                {
                    mDownXLength = event.getX();
                    mDownTime = event.getEventTime();
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    mUpXLength = event.getX();
                    long currentTime = event.getEventTime();
                    long time = currentTime - mDownTime;

                    if (time >= 300 && mDownXLength != mUpXLength) {
                        return true;
                    }
                    break;
                }
                }

                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu)
    {
        Log.d(TAG, "create context menu");
        super.onCreateContextMenu(menu);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        Log.d(TAG, "onSizeChanged: from " + oldw + ", " + oldh + " to " + w + ", " + h);
        super.onSizeChanged(w, h, oldw, oldh);

        for (OnSizeChangedListener l : mOnSizeChangedListenerList) {
            l.onSizeChanged();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
//        Log.d(sTag, "onDraw");
        super.onDraw(canvas);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
            Rect previouslyFocusedRect)
    {
        if (gainFocus) {
            if (this.searchAndSelectNextFocusableChildItem(direction, previouslyFocusedRect)) {
                return;
            }
        }

        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
//            Log.d(sTag, "onKeyDown");
            
            if (mCrossVertical && ((keyCode == KeyEvent.KEYCODE_DPAD_UP) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_DOWN))) { 
                ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                return super.onKeyDown(keyCode, event);
            }

            if (mCrossHorizon && ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT))) { 
                ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                return super.onKeyDown(keyCode, event);
            }

            int item_count = this.getCount();

            if (item_count >= 1) {
                int columns = Math.min(this.getCount(), mAdapter.getPageLayout().getLayoutColumnCount());
                if (columns <= 0) {
                    return super.onKeyDown(keyCode, event);
                }

                int current_idx = this.getSelectedItemPosition();
                int last_idx = item_count - 1;

                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if ((current_idx % columns) == 0) {
                        if (current_idx != 0) {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(current_idx - 1);
                            return true;
                        }
                        else {
                            if (mAdapter.getPaginator().canPrevPage()) {
                                ScreenUpdateManager.invalidate(this, UpdateMode.GC);
                                mAdapter.getPaginator().prevPage();
                            }
                            return true;
                        }
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if ((current_idx == last_idx) || (((current_idx + 1) % columns) == 0)) {
                        if (current_idx == last_idx) {
                            if (mAdapter.getPaginator().canNextPage()) {
                                ScreenUpdateManager.invalidate(this, UpdateMode.GC);
                                mAdapter.getPaginator().nextPage();
                            }
                            return true;
                        }
                        else {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(current_idx + 1);
                            return true;
                        }
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (current_idx < columns) {
                        int mod = last_idx % columns;
                        if (current_idx > mod) {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(last_idx);
                            return true;
                        }
                        else {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(last_idx - mod + current_idx);
                            return true;
                        }
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (current_idx > (last_idx - columns)) {
                        int head_of_last_row = last_idx - (last_idx % columns);
                        if (current_idx < head_of_last_row) {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(last_idx);
                            return true;
                        }
                        else {
                            ScreenUpdateManager.invalidate(this, UpdateMode.DW);
                            this.setSelection(current_idx % columns);
                            return true;
                        }
                    }
                }
            }

            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP) || 
                    (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                    (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                ScreenUpdateManager.invalidate(this, UpdateMode.DW);
            }
            
            return super.onKeyDown(keyCode, event);
        }
        finally {
//            Log.d(sTag, "onKeydown finished");
        }
    }
}

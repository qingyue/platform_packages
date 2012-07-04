/**
 * 
 */
package com.onyx.android.launcher.dialog;

import java.util.ArrayList;
import java.util.List;

import com.onyx.android.launcher.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.onyx.android.launcher.view.OnyxDialogBase;
import com.onyx.android.sdk.ui.util.OnyxFocusFinder;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager;
import com.onyx.android.sdk.ui.util.ScreenUpdateManager.UpdateMode;

/**
 * @author dxwts
 *
 */
public class DialogTagCloud extends OnyxDialogBase
{
    private static final String[] TAGTEXT = {
        "Kids", "Magazines", "Newspaper", "Literature", "Car", "鲁迅", "朱自清", "嗯........", "郭德纲" ,"Favorite", "Magic","Engineering", "Foreign Languages", "China", "Art",
        "History", "Design", "HAHAHAHA!!!!", "?????", "Law", "Nursing", "Economics", "Education",  "Reasoning", "US", "Literature", "Reference", 
        "Visual Arts", "啊", "Humanities", "Software", "Sciences", "Philosophy", "杜甫", "李白", 
      "Kids", "Magazines", "Newspaper", "Literature", "Car", "鲁迅", "朱自清", "嗯........", "郭德纲" ,"Favorite", "Magic","Engineering", "Foreign Languages", "China", "Art",
      "History", "Design", "HAHAHAHA!!!!", "?????", "Law", "Nursing", "Economics", "Education",  "Reasoning", "US", "Literature", "Reference", 
      "Visual Arts", "啊", "Humanities", "Software", "Sciences", "Philosophy", "杜甫", "李白"
    };
    
    private static final int[] TAGSIZE = {
        30, 15, 20, 30, 20, 30, 20, 30, 30, 30, 20, 15, 20, 30, 20, 15, 15, 30, 20, 20 ,30, 15, 20, 20, 15, 20, 30, 20, 15, 15, 30, 30, 15, 20, 15, 
        30, 15, 20, 30, 20, 30, 20, 30, 30, 30, 20, 15, 20, 30, 20, 15, 15, 30, 20, 20 ,30, 15, 20, 20, 15, 20, 30, 20, 15, 15, 30, 30, 15, 20, 15
    };
    
    private static final int PAGESIZE = 60;
    private static int mPageIndex = 0;
    
    private List<List<Object[]>> mTagObj = null;
    private RelativeLayout mDialogLayout = null;
    private RelativeLayout mTagLayout = null;
    private LinearLayout mPageController = null;
    private TextView mPageIndexTextView = null;
    private TextView mSlashTextView = null;
    private TextView mPageCountTextView =null;

    public DialogTagCloud(final Context context)
    {
        super(context);
        
        List<Object[]> tagList = new ArrayList<Object[]>();

        for (int i = 0; i < TAGTEXT.length; i++) {
            tagList.add(new Object[] { TAGTEXT[i], TAGSIZE[i] });
        }

        mTagObj = splitAry(tagList, PAGESIZE);

        mDialogLayout = new RelativeLayout(context);

        mTagLayout = setTagLayout(context, mTagObj, mPageIndex);
        final RelativeLayout.LayoutParams tagParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        mTagLayout.setBackgroundColor(Color.WHITE);

        mPageController = new LinearLayout(context);

        mPageController.setBackgroundColor(Color.WHITE);
        mPageController.setOrientation(LinearLayout.HORIZONTAL);

        RelativeLayout.LayoutParams pageCtrParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        pageCtrParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pageCtrParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ImageButton prevButton = new ImageButton(context);
        prevButton.setId(1001);
        prevButton.setImageResource(R.drawable.prev_page);
        prevButton.setBackgroundResource(R.drawable.button_background_onyxpathindicator);
        prevButton.setNextFocusLeftId(1002);

        prevButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {

                if (mPageIndex > 0) {
                    mPageIndex = mPageIndex - 1;
                    mDialogLayout.removeView(mTagLayout);
                    mTagLayout = setTagLayout(context, mTagObj, mPageIndex);
                    mDialogLayout.addView(mTagLayout, tagParams);
                    mPageIndexTextView.setText(Integer.toString(mPageIndex + 1));
                }
            }
        });

        mPageController.addView(prevButton);

        mPageIndexTextView = new TextView(context);
        mPageIndexTextView.setText(Integer.toString(mPageIndex + 1));
        mPageIndexTextView.setTextSize(20);
        mPageIndexTextView.setGravity(Gravity.CENTER_VERTICAL);
        mPageIndexTextView.setLayoutParams(new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
        mPageController.addView(mPageIndexTextView);

        mSlashTextView = new TextView(context);
        mSlashTextView.setText("/");
        mSlashTextView.setTextSize(20);
        mSlashTextView.setGravity(Gravity.CENTER_VERTICAL);
        mSlashTextView.setLayoutParams(new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
        mPageController.addView(mSlashTextView);

        mPageCountTextView = new TextView(context);
        mPageCountTextView.setText(Integer.toString(mTagObj.size()));
        mPageCountTextView.setTextSize(20);
        mPageCountTextView.setGravity(Gravity.CENTER_VERTICAL);
        mPageCountTextView.setLayoutParams(new LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
        mPageController.addView(mPageCountTextView);

        ImageButton nextButton = new ImageButton(context);
        nextButton.setId(1002);
        nextButton.setImageResource(R.drawable.next_page);
        nextButton.setBackgroundResource(R.drawable.button_background_onyxpathindicator);
        nextButton.setNextFocusRightId(1001);

        nextButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mPageIndex < (mTagObj.size() - 1)) {
                    mPageIndex = mPageIndex + 1;
                    mDialogLayout.removeView(mTagLayout);
                    mTagLayout = setTagLayout(context, mTagObj, mPageIndex);
                    mDialogLayout.addView(mTagLayout, tagParams);
                    mPageIndexTextView.setText(Integer.toString(mPageIndex + 1));
                }
            }
        });

        mPageController.addView(nextButton);

        mDialogLayout.addView(mTagLayout, tagParams);
        mDialogLayout.addView(mPageController, pageCtrParams);

        this.setContentView(mDialogLayout);

    }
    
    public RelativeLayout setTagLayout(Context context, List<List<Object[]>> tagList, int index)
    {
        RelativeLayout tagLayout = new RelativeLayout(context);

        List<Object[]> item = tagList.get(index);

        for (int i = 0; i < item.size(); i++) {

            Button tag_button = new Button(context);

            tag_button.setId(i + 1);
            tag_button.setText((String) item.get(i)[0]);
            tag_button.setTextSize((Integer) item.get(i)[1]);
            tag_button.setBackgroundResource(R.drawable.button_background_onyxpathindicator);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tag_button.setNextFocusLeftId(i + 5);
            }
            else if (i % 5 == 0) {
                params.addRule(RelativeLayout.BELOW, i - 4);
                if (i + 5 < TAGTEXT.length) {
                    tag_button.setNextFocusLeftId(i + 5);
                }
                else {
                    tag_button.setNextFocusLeftId(TAGTEXT.length);
                }

            }
            else {
                params.addRule(RelativeLayout.RIGHT_OF, i);
                params.addRule(RelativeLayout.ALIGN_TOP, i);
            }
            if (i == 4 || ((i + 1) % 5 == 0)) {
                tag_button.setNextFocusRightId(i - 3);
            }
            else if (i == TAGTEXT.length - 1) {
                tag_button.setNextFocusRightId(i - (TAGTEXT.length % 5) + 2);
            }

            tagLayout.addView(tag_button, params);

        }
        return tagLayout;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) ||
                (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
            ScreenUpdateManager.invalidate(this.getWindow().getDecorView(), UpdateMode.DW);

            if (this.getCurrentFocus() != null) {
                int direction = 0;
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    direction = View.FOCUS_UP;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    direction = View.FOCUS_DOWN;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    direction = View.FOCUS_LEFT;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    direction = View.FOCUS_RIGHT;
                    break;
                default:
                    assert (false);
                    throw new IndexOutOfBoundsException();
                }

                View dst_view = this.getCurrentFocus().focusSearch(direction);
                if (dst_view == null) {
                    int reverse_direction = OnyxFocusFinder.getReverseDirection(direction);
                    dst_view = OnyxFocusFinder.findFartherestViewInDirection(this.getCurrentFocus(), reverse_direction);

                    Rect rect = OnyxFocusFinder.getAbsoluteFocusedRect(this.getCurrentFocus());

                    dst_view.requestFocus(direction, rect);

                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private static List<List<Object[]>> splitAry(List<Object[]> ary, int subSize)
    {
        int count = ary.size() % subSize == 0 ? ary.size() / subSize : ary.size() / subSize + 1;

        List<List<Object[]>> subAryList = new ArrayList<List<Object[]>>();

        for (int i = 0; i < count; i++) {
            int index = i * subSize;

            List<Object[]> list = new ArrayList<Object[]>();
            int j = 0;
            while (j < subSize && index < ary.size()) {
                list.add(ary.get(index++));
                j++;
            }

            subAryList.add(list);
        }

        Object[] subAry = new Object[subAryList.size()];

        for (int i = 0; i < subAryList.size(); i++) {
            List<Object[]> subList = subAryList.get(i);

            Object[] subAryItem = new Object[subList.size()];
            for (int j = 0; j < subList.size(); j++) {
                subAryItem[j] = subList.get(j);
            }

            subAry[i] = subAryItem;
        }

        return subAryList;
    }

}

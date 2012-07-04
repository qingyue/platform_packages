/**
 * 
 */
package com.onyx.android.sdk.ui.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author joy
 *
 */
public class OnyxFocusFinder
{
    public static View findTopMostViewAbove(View view)
    {
        return findFartherestViewInDirection(view, View.FOCUS_UP);
    }
    public static View findBottomMostViewBelow(View view)
    {
        return findFartherestViewInDirection(view, View.FOCUS_DOWN);
    }
    public static View findLeftMostViewBeside(View view)
    {
        return findFartherestViewInDirection(view, View.FOCUS_LEFT);
    }
    public static View findRightMostViewBeside(View view)
    {
        return findFartherestViewInDirection(view, View.FOCUS_RIGHT);
    }
    
    public static int getReverseDirection(int direction)
    {
        switch (direction) {
        case View.FOCUS_UP:
            return View.FOCUS_DOWN;
        case View.FOCUS_DOWN:
            return View.FOCUS_UP;
        case View.FOCUS_LEFT:
            return View.FOCUS_RIGHT;
        case View.FOCUS_RIGHT:
            return View.FOCUS_LEFT;
        default:
            break;
        }
        
        throw new IndexOutOfBoundsException();
    }
    
    /**
     * always return a view in the specified direction
     * @param view
     * @param direction
     * @return
     */
    public static View findFartherestViewInDirection(View view, int direction)
    {
        View find_view = view.focusSearch(direction);
        if (find_view == null) {
            return view;
        }
        
        // double-check whether find_view is what we want in direction
        Rect src_rect = getAbsoluteCoorinateRect(view);
        Rect dst_rect = getAbsoluteCoorinateRect(find_view);
        switch (direction) {
        case View.FOCUS_UP:
            if (((dst_rect.right <= src_rect.left) || (dst_rect.left >= src_rect.right)) &&
                    (dst_rect.top <= src_rect.top))
            {
                return view;
            }
            break;
        case View.FOCUS_DOWN:
            if (((dst_rect.right <= src_rect.left) || (dst_rect.left >= src_rect.right)) &&
                    (dst_rect.bottom >= src_rect.bottom))
            {
                return view;
            }
            break;
        case View.FOCUS_LEFT:
            if (((dst_rect.bottom <= src_rect.top) || (dst_rect.top >= dst_rect.bottom)) &&
                    (dst_rect.left >= src_rect.left))
            {
                return view;
            }
            break;
        case View.FOCUS_RIGHT:
            if (((dst_rect.bottom <= src_rect.top) || (dst_rect.top >= dst_rect.bottom)) &&
                    (dst_rect.right <= src_rect.right))
            {
                return view;
            }
            break;
        default:
            break;
        }
        
        View dst_view = find_view;
        while (find_view != null) {
            // when revert_view is null, meaning can't move down any more, so dst_view is bottom most
            dst_view = find_view;
            find_view = find_view.focusSearch(direction);
        }

        return dst_view;
    }
    
    public static Rect getAbsoluteCoorinateRect(View view, Rect rect) 
    {
        Rect ret = new Rect(rect);
        
        ViewGroup root = (ViewGroup)view.getRootView();
        
        root.offsetDescendantRectToMyCoords(view, ret);
        
        return ret;
    }
    
    public static Rect getAbsoluteCoorinateRect(View view)
    {
        Rect r = new Rect();
        view.getHitRect(r);
        
        return getAbsoluteCoorinateRect((View)view.getParent(), r);
    }
    
    /**
     * NOTICE: when view is GridView, view.getFocusedRect() will return it's child's focused rect
     * 
     * @param view
     * @return
     */
    public static Rect getAbsoluteFocusedRect(View view)
    {
        Rect r = new Rect();
        view.getFocusedRect(r);
        
        return getAbsoluteCoorinateRect(view, r);
    }
    
    public static int getAbsoluteTop(View view)
    {
        Rect r = new Rect();
        view.getFocusedRect(r);
        
        Rect r2 = getAbsoluteCoorinateRect(view, r);
        
        return r2.top;
    }
    public static int getAbsoluteBottom(View view)
    {
        Rect r = new Rect();
        view.getFocusedRect(r);
        
        Rect r2 = getAbsoluteCoorinateRect(view, r);
        
        return r2.bottom;
    }
    public static int getAbsoluteLeft(View view)
    {
        Rect r = new Rect();
        view.getFocusedRect(r);
        
        Rect r2 = getAbsoluteCoorinateRect(view, r);
        
        return r2.left;
    }
    public static int getAbsoluteRight(View view)
    {
        Rect r = new Rect();
        view.getFocusedRect(r);
        
        Rect r2 = getAbsoluteCoorinateRect(view, r);
        
        return r2.right;
    }
    
}

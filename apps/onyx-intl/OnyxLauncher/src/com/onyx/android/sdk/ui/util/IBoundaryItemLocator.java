/**
 * 
 */
package com.onyx.android.sdk.ui.util;

import android.graphics.Rect;
import android.view.View;

/**
 * @author joy
 *
 */
public interface IBoundaryItemLocator
{
    enum BoundarySide 
    {
        NONE, TOP, BOTTOM, LEFT, RIGHT, ;
        
        public static BoundarySide valueOf(int direction)
        {
            switch (direction) {
            case View.FOCUS_UP:
                return BOTTOM;
            case View.FOCUS_DOWN:
                return TOP;
            case View.FOCUS_LEFT:
                return RIGHT;
            case View.FOCUS_RIGHT:
                return LEFT;
            default:
                return NONE;
            }
        }
        
        public int toInt()
        {
            switch (this) {
            case TOP:
                return View.FOCUS_DOWN;
            case BOTTOM:
                return View.FOCUS_UP;
            case LEFT:
                return View.FOCUS_RIGHT;
            case RIGHT:
                return View.FOCUS_LEFT;
            default:
                assert(false);
                throw new IndexOutOfBoundsException();
            }
        }
    }
    
    void selectBoundaryItemBySearch(Rect srcRect, BoundarySide boundarySide);
}

/**
 * 
 */
package com.onyx.android.sdk.ui.data;

import java.util.Comparator;
import java.util.HashMap;

import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.util.FileUtil;

/**
 * @author joy
 *
 */
public class OnyxItemSorter
{
    private static final HashMap<SortOrder, Comparator<GridItemData>> ASC_COMPARATORS = new HashMap<SortOrder, Comparator<GridItemData>>();
    private static final HashMap<SortOrder, Comparator<GridItemData>> DESC_COMPARATORS = new HashMap<SortOrder, Comparator<GridItemData>>();
    
    static {
        final Comparator<GridItemData> comp_name = new Comparator<GridItemData>()
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                if ((o1 instanceof FileItemData) && !(o2 instanceof FileItemData)) {
                    return 1;
                }
                else if (!(o1 instanceof FileItemData) && (o2 instanceof FileItemData)) {
                    return -1;
                }
                else if ((o1 instanceof FileItemData) && (o2 instanceof FileItemData)) {
                    if (((FileItemData)o1).isDirectory() && !((FileItemData)o2).isDirectory()) {
                        return -1;
                    }
                    else if (!((FileItemData)o1).isDirectory() && ((FileItemData)o2).isDirectory()) {
                        return 1;
                    }
                }
                
                return o1.getURI().getName().compareToIgnoreCase(o2.getURI().getName());
            }
        };
        ASC_COMPARATORS.put(SortOrder.Name, comp_name);
        DESC_COMPARATORS.put(SortOrder.Name, new Comparator<GridItemData>()
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                return -comp_name.compare(o1, o2);
            }
        });
        
        final Comparator<GridItemData> comp_type = new Comparator<GridItemData>() 
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                int res = FileUtil.getFileExtension(o1.getURI().getName()).compareToIgnoreCase(
                        FileUtil.getFileExtension(o2.getURI().getName()));
                if (res != 0) {
                    return res;
                }
                
                return FileUtil.getFileNameWithoutExtension(o1.getURI().getName()).compareToIgnoreCase(
                        FileUtil.getFileNameWithoutExtension(o2.getURI().getName()));
            }
        };
        ASC_COMPARATORS.put(SortOrder.FileType, comp_type);
        DESC_COMPARATORS.put(SortOrder.FileType, new Comparator<GridItemData>() 
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                return -comp_type.compare(o1, o2);
            }
        });
    }
    
    public static Comparator<GridItemData> getComparator(SortOrder order, AscDescOrder ascOrder)
    {
        HashMap<SortOrder, Comparator<GridItemData>> dict = 
                (ascOrder == AscDescOrder.Asc) ? ASC_COMPARATORS : DESC_COMPARATORS;
        return dict.get(order);
    }
}

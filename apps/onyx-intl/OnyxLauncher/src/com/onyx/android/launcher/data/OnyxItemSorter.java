/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;

import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.util.FileUtil;
import com.onyx.android.sdk.ui.data.BookItemData;
import com.onyx.android.sdk.ui.data.FileItemData;
import com.onyx.android.sdk.ui.data.GridItemData;

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
                if ((o1 instanceof FileItemData)
                        && !(o2 instanceof FileItemData)) {
                    return 1;
                } else if (!(o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    return -1;
                } else if ((o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    if (((FileItemData) o1).isDirectory()
                            && !((FileItemData) o2).isDirectory()) {
                        return -1;
                    } else if (!((FileItemData) o1).isDirectory()
                            && ((FileItemData) o2).isDirectory()) {
                        return 1;
                    }else if(((FileItemData) o1).isDirectory()
                            && ((FileItemData) o2).isDirectory()){
                    	return 1;
                    }
                }

                return o1.getURI().getName()
                        .compareToIgnoreCase(o2.getURI().getName());
            }
        };
        
        
        final Comparator<GridItemData> desc_comp_name = new Comparator<GridItemData>()
        		{

					@Override
					public int compare(GridItemData o1, GridItemData o2) 
					{
						// TODO Auto-generated method stub
						if ((o1 instanceof FileItemData)
		                        && !(o2 instanceof FileItemData)) {
		                    return 1;
		                } else if (!(o1 instanceof FileItemData)
		                        && (o2 instanceof FileItemData)) {
		                    return -1;
		                } else if ((o1 instanceof FileItemData)
		                        && (o2 instanceof FileItemData)) {
		                    if (((FileItemData) o1).isDirectory()
		                            && !((FileItemData) o2).isDirectory()) {
		                        return -1;
		                    } else if (!((FileItemData) o1).isDirectory()
		                            && ((FileItemData) o2).isDirectory()) {
		                        return 1;
		                    }else if(((FileItemData) o1).isDirectory()
		                            && ((FileItemData) o2).isDirectory()){
		                    	return 1;
		                    }
		                }
						int i = -o1.getURI().getName().compareToIgnoreCase(o2.getURI().getName());

		                return i;
					}
        	
        		};
        
        ASC_COMPARATORS.put(SortOrder.Name, comp_name);
        DESC_COMPARATORS.put(SortOrder.Name, desc_comp_name);
//        DESC_COMPARATORS.put(SortOrder.Name, new Comparator<GridItemData>()
//        {
//
//            @Override
//            public int compare(GridItemData o1, GridItemData o2)
//            {
//                return -comp_name.compare(o1, o2);
//            }
//        });


        final Comparator<GridItemData> comp_type = new Comparator<GridItemData>()
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
            	 if ((o1 instanceof FileItemData)
	                        && (o2 instanceof FileItemData)) {
	                    if (((FileItemData) o1).isDirectory()
	                            && !((FileItemData) o2).isDirectory()) {
	                        return -1;
	                    } else if (!((FileItemData) o1).isDirectory()
	                            && ((FileItemData) o2).isDirectory()) {
	                        return 1;
	                    }else if(((FileItemData) o1).isDirectory()
	                            && ((FileItemData) o2).isDirectory()){
	                    	return 1;
	                    }
	                }
            	 
	                	int res = FileUtil
                        .getFileExtension(o1.getURI().getName())
                        .compareToIgnoreCase(
                                FileUtil.getFileExtension(o2.getURI().getName()));

                        if (res != 0) {
                             return res;
                            }
                return FileUtil.getFileNameWithoutExtension(
                        o1.getURI().getName()).compareToIgnoreCase(
                        FileUtil.getFileNameWithoutExtension(o2.getURI()
                                .getName()));
            
	        }
        };
        
        final Comparator<GridItemData> desc_comp_type = new Comparator<GridItemData>()
                {

                    @Override
                    public int compare(GridItemData o1, GridItemData o2)
                    {
                    	if ((o1 instanceof FileItemData)
    	                        && (o2 instanceof FileItemData)) {
    	                    if (((FileItemData) o1).isDirectory()
    	                            && !((FileItemData) o2).isDirectory()) {
    	                        return -1;
    	                    } else if (!((FileItemData) o1).isDirectory()
    	                            && ((FileItemData) o2).isDirectory()) {
    	                        return 1;
    	                    }else if(((FileItemData) o1).isDirectory()
    	                            && ((FileItemData) o2).isDirectory()){
    	                    	return 1;
    	                    }
    	                }
                    	
                        int res = FileUtil
                                .getFileExtension(o1.getURI().getName())
                                .compareToIgnoreCase(
                                        FileUtil.getFileExtension(o2.getURI().getName()));
                        if(res == 0) {
                        	 res = 1;
                        } else if(res > 0) {
                        	 res = -1;
                        } else if(res < 0) {
                        	 res = 1;
                        }
						return res;
                    }
                };
        
        
        ASC_COMPARATORS.put(SortOrder.FileType, comp_type);
        DESC_COMPARATORS.put(SortOrder.FileType, desc_comp_type);
//        DESC_COMPARATORS.put(SortOrder.FileType, new Comparator<GridItemData>()
//        {
//
//            @Override
//            public int compare(GridItemData o1, GridItemData o2)
//            {
//                return -comp_type.compare(o1, o2);
//            }
//        });

        final Comparator<GridItemData> comp_size = new Comparator<GridItemData>()
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                int res = 1;
                long diff = 0;
                if ((o1 instanceof FileItemData)
                        && !(o2 instanceof FileItemData)) {
                    return 1;
                } else if (!(o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    return -1;
                } else if ((o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    if (((FileItemData) o1).isDirectory()
                            && !((FileItemData) o2).isDirectory()) {
                        return -1;
                    } else if (!((FileItemData) o1).isDirectory()
                            && ((FileItemData) o2).isDirectory()) {
                        return 1;
                    }else if(((FileItemData) o1).isDirectory() 
                    		&& ((FileItemData) o2).isDirectory())
                    {
                    	return 0;
                    }

                    BookItemData b1 = (BookItemData) o1;
                    BookItemData b2 = (BookItemData) o2;
                    if((b1.getMetadata()!=null) 
                    		&& (b2.getMetadata()!=null)){
                    	 diff = b1.getMetadata().getSize()
                                - b2.getMetadata().getSize();
                    }else if((b1.getMetadata()==null) 
                    		&& (b2.getMetadata()!=null)){
                    	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                    	if(fb1!=null){
                    		 diff = fb1.length()
                                    - b2.getMetadata().getSize();
                    	}
                    }else if((b1.getMetadata()!=null) 
                    		&& (b2.getMetadata()==null)){
                    	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                    	if(fb2!=null){
                    		 diff = b1.getMetadata().getSize()
                                     - fb2.length();
                    	}
                    }else if((b1.getMetadata()==null) 
                    		&& (b2.getMetadata()==null)){
                    	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                    	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                    	if(fb2!=null && fb2!=null){
                    		 diff = fb1.length()
                                     - fb2.length();
                    	}
                    }
                    if (diff > 0)
                        return res = 1;
                    else if (diff == 0)
                        return res = 1;
                    else
                        return res = -1;
                }
                return res;
            }

        };
        
        final Comparator<GridItemData> desc_comp_size = new Comparator<GridItemData>()
                {

                    @Override
                    public int compare(GridItemData o1, GridItemData o2)
                    {
                        int res = 1;
                        long diff = 0;
                        if ((o1 instanceof FileItemData)
                                && !(o2 instanceof FileItemData)) {
                            return 1;
                        } else if (!(o1 instanceof FileItemData)
                                && (o2 instanceof FileItemData)) {
                            return -1;
                        } else if ((o1 instanceof FileItemData)
                                && (o2 instanceof FileItemData)) {
                            if (((FileItemData) o1).isDirectory()
                                    && !((FileItemData) o2).isDirectory()) {
                                return -1;
                            } else if (!((FileItemData) o1).isDirectory()
                                    && ((FileItemData) o2).isDirectory()) {
                                return 1;
                            }else if(((FileItemData) o1).isDirectory() 
                            		&& ((FileItemData) o2).isDirectory())
                            {
                            	return 0;
                            }

                            BookItemData b1 = (BookItemData) o1;
                            BookItemData b2 = (BookItemData) o2;
                            if((b1.getMetadata()!=null) 
                            		&& (b2.getMetadata()!=null)){
                            	 diff = b1.getMetadata().getSize()
                                        - b2.getMetadata().getSize();
                            }else if((b1.getMetadata()==null) 
                            		&& (b2.getMetadata()!=null)){
                            	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                            	if(fb1!=null){
                            		 diff = fb1.length()
                                            - b2.getMetadata().getSize();
                            	}
                            }else if((b1.getMetadata()!=null) 
                            		&& (b2.getMetadata()==null)){
                            	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                            	if(fb2!=null){
                            		 diff = b1.getMetadata().getSize()
                                             - fb2.length();
                            	}
                            }else if((b1.getMetadata()==null) 
                            		&& (b2.getMetadata()==null)){
                            	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                            	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                            	if(fb2!=null && fb2!=null){
                            		 diff = fb1.length()
                                             - fb2.length();
                            	}
                            }
                            if (diff > 0)
                                return res = -1;
                            else if (diff == 0)
                                return res = 1;
                            else
                                return res = 1;
                        }
                        return res;
                    }

                };
        ASC_COMPARATORS.put(SortOrder.Size, comp_size);
        DESC_COMPARATORS.put(SortOrder.Size,desc_comp_size);
//        DESC_COMPARATORS.put(SortOrder.Size, new Comparator<GridItemData>()
//        {
//
//            @Override
//            public int compare(GridItemData o1, GridItemData o2)
//            {
//                return -comp_size.compare(o1, o2);
//            }
//        });

        final Comparator<GridItemData> comp_accesstime = new Comparator<GridItemData>()
        {

            @Override
            public int compare(GridItemData o1, GridItemData o2)
            {
                int res = 1;
                long diff = 0;
                if ((o1 instanceof FileItemData)
                        && !(o2 instanceof FileItemData)) {
                    return 1;
                } else if (!(o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    return -1;
                } else if ((o1 instanceof FileItemData)
                        && (o2 instanceof FileItemData)) {
                    if (((FileItemData) o1).isDirectory()
                            && !((FileItemData) o2).isDirectory()) {
                        return -1;
                    } else if (!((FileItemData) o1).isDirectory()
                            && ((FileItemData) o2).isDirectory()) {
                        return 1;
                    }else if(((FileItemData) o1).isDirectory()
                            && ((FileItemData) o2).isDirectory()){
                    	return 0;
                    }
                    BookItemData b1 = (BookItemData) o1;
                    BookItemData b2 = (BookItemData) o2;
                    if((b1.getMetadata()!=null) 
                    		&& (b2.getMetadata()!=null)){
                    	 diff = b1.getMetadata().getLastModified()
                                - b2.getMetadata().getLastModified();
                    }else if((b1.getMetadata()==null) 
                    		&& (b2.getMetadata()!=null)){
                    	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                    	if(fb1!=null){
                    		 diff = fb1.lastModified()
                                    - b2.getMetadata().getLastModified();
                    	}
                    }else if((b1.getMetadata()!=null) 
                    		&& (b2.getMetadata()==null)){
                    	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                    	if(fb2!=null){
                    		 diff = b1.getMetadata().getLastModified()
                                     - fb2.lastModified();
                    	}
                    }else if((b1.getMetadata()==null) 
                    		&& (b2.getMetadata()==null)){
                    	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                    	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                    	if(fb2!=null && fb2!=null){
                    		 diff = fb1.lastModified()
                                     - fb2.lastModified();
                    	}
                    }
                    if (diff > 0)
                        return res = 1;
                    else if (diff == 0)
                        return res = 1;
                    else
                        return res = -1;
                }
                return res;
            }

        };

        final Comparator<GridItemData> desc_comp_accesstime = new Comparator<GridItemData>()
                {

                    @Override
                    public int compare(GridItemData o1, GridItemData o2)
                    {
                        int res = 1;
                        long diff = 0;
                        if ((o1 instanceof FileItemData)
                                && !(o2 instanceof FileItemData)) {
                            return 1;
                        } else if (!(o1 instanceof FileItemData)
                                && (o2 instanceof FileItemData)) {
                            return -1;
                        } else if ((o1 instanceof FileItemData)
                                && (o2 instanceof FileItemData)) {
                            if (((FileItemData) o1).isDirectory()
                                    && !((FileItemData) o2).isDirectory()) {
                                return -1;
                            } else if (!((FileItemData) o1).isDirectory()
                                    && ((FileItemData) o2).isDirectory()) {
                                return 1;
                            }else if(((FileItemData) o1).isDirectory()
                                    && ((FileItemData) o2).isDirectory()){
                            	return 0;
                            }
                            BookItemData b1 = (BookItemData) o1;
                            BookItemData b2 = (BookItemData) o2;
                            
                            if((b1.getMetadata()!=null) 
                            		&& (b2.getMetadata()!=null)){
                            	 diff = b1.getMetadata().getLastModified()
                                        - b2.getMetadata().getLastModified();
                            }else if((b1.getMetadata()==null) 
                            		&& (b2.getMetadata()!=null)){
                            	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                            	if(fb1!=null){
                            		 diff = fb1.lastModified()
                                            - b2.getMetadata().getLastModified();
                            	}
                            }else if((b1.getMetadata()!=null) 
                            		&& (b2.getMetadata()==null)){
                            	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                            	if(fb2!=null){
                            		 diff = b1.getMetadata().getLastModified()
                                             - fb2.lastModified();
                            	}
                            }else if((b1.getMetadata()==null) 
                            		&& (b2.getMetadata()==null)){
                            	File fb1 = GridItemManager.getFileFromURI(b1.getURI());
                            	File fb2 = GridItemManager.getFileFromURI(b2.getURI());
                            	if(fb2!=null && fb2!=null){
                            		 diff = fb1.lastModified()
                                             - fb2.lastModified();
                            	}
                            }
                            if (diff > 0)
                                return res = -1;
                            else if (diff == 0)
                                return res = 1;
                            else
                                return res = 1;
                        }
                        return res;
                    }

                };
        
        ASC_COMPARATORS.put(SortOrder.AccessTime, comp_accesstime);
        DESC_COMPARATORS.put(SortOrder.AccessTime, desc_comp_accesstime);
//        DESC_COMPARATORS.put(SortOrder.AccessTime,
//                new Comparator<GridItemData>()
//                {
//
//                    @Override
//                    public int compare(GridItemData o1, GridItemData o2)
//                    {
//                        return -comp_accesstime.compare(o1, o2);
//                    }
//                });
//
    }

    public static Comparator<GridItemData> getComparator(SortOrder order,
            AscDescOrder ascOrder)
    {
        HashMap<SortOrder, Comparator<GridItemData>> dict = (ascOrder == AscDescOrder.Asc) ? ASC_COMPARATORS
                : DESC_COMPARATORS;
        return dict.get(order);
    }

}

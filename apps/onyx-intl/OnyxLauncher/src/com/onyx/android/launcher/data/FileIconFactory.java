/**
 * 
 */
package com.onyx.android.launcher.data;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.onyx.android.launcher.R;

/**
 * @author joy
 * 
 */
public class FileIconFactory
{
    private static volatile boolean ourInited = false;
    private static Context ourContext = null;
    private static Bitmap ourDefaultBitmap = null;
    // file extension as key, upper case, such as ".PDF"
    private static HashMap<String, Bitmap> ourFileIcons = new HashMap<String, Bitmap>();
    // resource id as value, such as R.drawable.pdf
    private static HashMap<String, Integer> ourPredefinedIcons = createPredefinedIcons();

    /**
     * must be called when application starting up
     * 
     * @param context
     */
    public static synchronized void init(Context context)
    {
        if (ourInited) {
            return;
        }
        
        ourContext = context; 
        ourDefaultBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.unknown_document);
        
        ourInited = true;

        return;
    }

    /**
     * should init() first
     * 
     * @param fileName
     * @return
     */
    public static synchronized Bitmap getIconByFileName(String fileName)
    {
        if (!ourInited) {
            assert(false);
            throw new RuntimeException();
        }
        
        String ext = getFileExtension(fileName);

        if (ourFileIcons.containsKey(ext)) {
            return ourFileIcons.get(ext);
        }

        if (!ourPredefinedIcons.containsKey(ext)) {
            return ourDefaultBitmap;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(ourContext.getResources(),
                ourPredefinedIcons.get(ext));
        if (bitmap != null) {
            ourFileIcons.put(ext, bitmap);
            return bitmap;
        } else {
            return ourDefaultBitmap;
        }
    }

    private static HashMap<String, Integer> createPredefinedIcons()
    {
        HashMap<String, Integer> icons = new HashMap<String, Integer>();
        
        addPredefinedIcon(icons, "", R.drawable.unknown_document);
        addPredefinedIcon(icons, ".ABF", R.drawable.abf);
        addPredefinedIcon(icons, ".BMP", R.drawable.bmp);
        addPredefinedIcon(icons, ".CHM", R.drawable.chm);
        addPredefinedIcon(icons, ".DOC", R.drawable.doc);
        addPredefinedIcon(icons, ".DJVU", R.drawable.djvu);
        addPredefinedIcon(icons, ".EPUB", R.drawable.epub);
        addPredefinedIcon(icons, ".FB2", R.drawable.fb2);
        addPredefinedIcon(icons, ".GIF", R.drawable.gif);
        addPredefinedIcon(icons, ".HTM", R.drawable.htm);
        addPredefinedIcon(icons, ".HTML", R.drawable.html);
        addPredefinedIcon(icons, ".JPG", R.drawable.jpg);
        addPredefinedIcon(icons, ".JPEG", R.drawable.jpg);
        addPredefinedIcon(icons, ".MOBI", R.drawable.mobi);
        addPredefinedIcon(icons, ".MP3", R.drawable.mp3);
        addPredefinedIcon(icons, ".PDB", R.drawable.pdb);
        addPredefinedIcon(icons, ".PDF", R.drawable.pdf);
        addPredefinedIcon(icons, ".PNG", R.drawable.png);
        addPredefinedIcon(icons, ".PPT", R.drawable.ppt);
        addPredefinedIcon(icons, ".RTF", R.drawable.rtf);
        addPredefinedIcon(icons, ".TIF", R.drawable.tiff);
        addPredefinedIcon(icons, ".TIFF", R.drawable.tiff);
        addPredefinedIcon(icons, ".TXT", R.drawable.txt);
        addPredefinedIcon(icons, ".XLS", R.drawable.xls);
        addPredefinedIcon(icons, ".ZIP", R.drawable.zip);

        return icons;
    }

    private static void addPredefinedIcon(HashMap<String, Integer> icons,
            String fileExtension, int resourceId)
    {
        icons.put(fileExtension.toUpperCase(), resourceId);
    }

    /**
     * get upper case of file extension
     * 
     * @param fileName
     * @return
     */
    private static String getFileExtension(String fileName)
    {
        int idx = fileName.lastIndexOf('.');
        if (idx == -1) {
            return "";
        } else {
            return fileName.substring(idx).toUpperCase();
        }
    }
}

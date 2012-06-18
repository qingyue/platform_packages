/**
 * 
 */
package com.onyx.android.sdk.data.cms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.onyx.android.sdk.data.AscDescOrder;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.util.RefValue;

/**
 * @author joy
 * 
 */
public class OnyxCmsCenter
{
    private static final String TAG = "OnyxCMSCenter";

    public static final String PROVIDER_AUTHORITY = "com.onyx.android.launcher.OnyxCmsProvider";

    public static boolean getLibraryItems(Context activity,
            SortOrder sortOrder, AscDescOrder ascOrder, Collection<OnyxLibraryItem> result)
    {
        Cursor c = null;
        try {
            long time_start = System.currentTimeMillis();

            long time_point = time_start;
            
            String ascDescSort = null;
            if(ascOrder == AscDescOrder.Asc) {
            	ascDescSort = "ASC";
            } else {
            	ascDescSort = "DESC";
            }
            
            if (sortOrder == SortOrder.Name) {
                c = activity.getContentResolver().query(
                        OnyxLibraryItem.CONTENT_URI, null, null, null,
                        OnyxLibraryItem.Columns.NAME + " " + ascDescSort);
            } else if(sortOrder == SortOrder.Size) {
            	String sort = OnyxLibraryItem.Columns.SIZE + " " + ascDescSort + "," + OnyxLibraryItem.Columns.NAME + " ASC";
            	c = activity.getContentResolver().query(OnyxLibraryItem.CONTENT_URI, null, null, null, 
            			sort);
            } else if(sortOrder == SortOrder.FileType){
            	String sort = OnyxLibraryItem.Columns.TYPE + " " + ascDescSort + "," + OnyxLibraryItem.Columns.NAME + " ASC";
            	c = activity.getContentResolver().query(OnyxLibraryItem.CONTENT_URI, null, null, null, 
            			sort);
            } else if(sortOrder == SortOrder.AccessTime) {
            	c = activity.getContentResolver().query(OnyxLibraryItem.CONTENT_URI, null, null, null,
            			OnyxLibraryItem.Columns.ACCESS_TIME + " " + ascDescSort);
            } else {
                c = activity.getContentResolver().query(
                        OnyxLibraryItem.CONTENT_URI, null, null, null, null);
            }
            long time_db_load = System.currentTimeMillis() - time_point;

            if (c == null) {
                return false;
            }

            time_point = System.currentTimeMillis();
            readLibraryItemCursor(c, result);
            long time_db_read = System.currentTimeMillis() - time_point;

            long time_end = System.currentTimeMillis();

            Log.d(TAG, "items loaded, count: " + result.size());
            Log.d(TAG, "db load time: " + time_db_load + "ms\n");
            Log.d(TAG, "read time: " + time_db_read + "ms\n");
            Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");

            return true;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean getLibraryItems(Context context,
            Collection<OnyxLibraryItem> result)
    {
        return getLibraryItems(context, SortOrder.None, AscDescOrder.Asc, result);
    }

    public static boolean getMetadata(Context context, OnyxMetadata metadata)
    {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(OnyxMetadata.CONTENT_URI,
                    null,
                    OnyxMetadata.Columns.MD5 + "='" + metadata.getMD5() + "'",
                    null, null);
            if (c == null) {
                return false;
            }
            if (c.moveToFirst()) {
                OnyxMetadata.Columns.readColumnData(c, metadata);
                return true;
            }

            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean getMetadatas(Context context,
            Collection<OnyxMetadata> result)
    {
        Cursor c = null;
        try {
            long time_start = System.currentTimeMillis();

            long time_point = time_start;
            c = context.getContentResolver().query(OnyxMetadata.CONTENT_URI,
                    null, null, null, null);
            long time_db_load = System.currentTimeMillis() - time_point;

            if (c == null) {
                return false;
            }

            time_point = System.currentTimeMillis();
            readMetadataCursor(c, result);
            long time_db_read = System.currentTimeMillis() - time_point;

            long time_end = System.currentTimeMillis();

            Log.d(TAG, "items loaded, count: " + result.size());
            Log.d(TAG, "db load time: " + time_db_load + "ms\n");
            Log.d(TAG, "read time: " + time_db_read + "ms\n");
            Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");

            return true;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean insertMetadata(Context context, OnyxMetadata data)
    {
        Uri result = context.getContentResolver().insert(
                OnyxMetadata.CONTENT_URI,
                OnyxMetadata.Columns.createColumnData(data));
        if (result == null) {
            return false;
        }

        String id = result.getLastPathSegment();
        if (id == null) {
            return false;
        }

        data.setId(Long.parseLong(id));

        return true;
    }

    public static boolean updateMetadata(Context context, OnyxMetadata data)
    {
        Uri row = Uri.withAppendedPath(OnyxMetadata.CONTENT_URI,
                String.valueOf(data.getId()));
        int count = context.getContentResolver().update(row,
                OnyxMetadata.Columns.createColumnData(data), null, null);
        if (count <= 0) {
            return false;
        }

        assert (count == 1);
        return true;
    }

    public static boolean getRecentReadings(Context context,
            Collection<OnyxMetadata> result)
    {
        Cursor c = null;
        try {
            long time_start = System.currentTimeMillis();

            long time_point = time_start;
            c = context.getContentResolver().query(
                    OnyxMetadata.CONTENT_URI,
                    null,
                    "(" + OnyxMetadata.Columns.LAST_ACCESS
                            + " is not null) and ("
                            + OnyxMetadata.Columns.LAST_ACCESS + "!='')", null,
                    OnyxMetadata.Columns.LAST_ACCESS + " desc");
            long time_db_load = System.currentTimeMillis() - time_point;

            if (c == null) {
                return false;
            }

            time_point = System.currentTimeMillis();
            readMetadataCursor(c, result);
            long time_db_read = System.currentTimeMillis() - time_point;

            long time_end = System.currentTimeMillis();

            Log.d(TAG, "items loaded, count: " + result.size());
            Log.d(TAG, "db load time: " + time_db_load + "ms\n");
            Log.d(TAG, "read time: " + time_db_read + "ms\n");
            Log.d(TAG, "total time: " + (time_end - time_start) + "ms\n");

            return true;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean getThumbnail(Context context, OnyxMetadata metadata,
            RefValue<Bitmap> result)
    {
        if (metadata == null) {
            assert (false);
            return false;
        }

        Cursor c = null;
        try {
            c = context.getContentResolver().query(
                    OnyxThumbnail.CONTENT_URI,
                    null,
                    OnyxThumbnail.Columns.SOURCE_MD5 + "='" + metadata.getMD5()
                            + "'", null, null);
            if (c == null) {
                return false;
            }
            if (c.moveToFirst()) {
                OnyxThumbnail data = OnyxThumbnail.Columns.readColumnData(c);
                Uri row = Uri.withAppendedPath(OnyxThumbnail.CONTENT_URI,
                        String.valueOf(data.getId()));
                InputStream is = null;
                try {
                    is = context.getContentResolver().openInputStream(row);
                    if (is == null) {
                        Log.d(TAG, "openInputStream failed");
                        return false;
                    }
                    Bitmap b = BitmapFactory.decodeStream(is);
                    if (b == null) {
                        Log.d(TAG, "decodeStream failed");
                        return false;
                    }
                    result.setValue(b);
                    return true;
                } catch (FileNotFoundException e) {
                    Log.w(TAG, e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.w(TAG, e);
                        }
                    }
                }
            }

            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean insertThumbnail(Context context, OnyxMetadata data,
            Bitmap thumbnail)
    {
        if (data == null) {
            assert (false);
            return false;
        }

        Uri result = context.getContentResolver().insert(
                OnyxThumbnail.CONTENT_URI,
                OnyxThumbnail.Columns.createColumnData(data.getMD5()));
        if (result == null) {
            Log.d(TAG, "insertThumbnail db insert failed");
            return false;
        }

        OutputStream os = null;
        try {
            os = context.getContentResolver().openOutputStream(result);
            if (os == null) {
                Log.d(TAG, "openOutputStream failed");
                return false;
            }
            thumbnail.compress(CompressFormat.JPEG, 85, os);
            Log.d(TAG, "insertThumbnail success");
            return true;
        } catch (FileNotFoundException e) {
            Log.w(TAG, e);
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    private static void readLibraryItemCursor(Cursor c,
            Collection<OnyxLibraryItem> result)
    {
        if (c.moveToFirst()) {
            result.add(OnyxLibraryItem.Columns.readColumnData(c));

            while (c.moveToNext()) {
                if (Thread.interrupted()) {
                    return;
                }

                result.add(OnyxLibraryItem.Columns.readColumnData(c));
            }
        }
    }

    private static void readMetadataCursor(Cursor c,
            Collection<OnyxMetadata> result)
    {
        if (c.moveToFirst()) {
            result.add(OnyxMetadata.Columns.readColumnData(c));

            while (c.moveToNext()) {
                if (Thread.interrupted()) {
                    return;
                }

                result.add(OnyxMetadata.Columns.readColumnData(c));
            }
        }
    }
}

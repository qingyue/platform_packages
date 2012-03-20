/**
 * 
 */
package com.onyx.android.launcher.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.onyx.android.sdk.data.util.EnviromentUtil;
import com.onyx.android.sdk.data.util.RefValue;

/**
 * @author joy
 *
 */
public class SDFileFactory {
	static public boolean prepareSDFile()
	{
		File file_sd = EnviromentUtil.getExternalStorageDirectory();
		String path_sd_root = file_sd.getPath();
		try {
			String path_books = path_sd_root + "/" + "Books";
			File file_books = new File(path_books);
			if (!file_books.exists()) {
				if (!file_books.createNewFile()) {
					return false;
				}
			}

			String path_ntoes = path_sd_root + "/" + "Notes";
			File file_notes = new File(path_ntoes);
			if (!file_notes.exists()) {
				if (!file_notes.createNewFile()) {
					return false;
				}
			}
			
			String path_pics = path_sd_root + "/" + "Pics";
			File file_pics = new File(path_pics);
			if (!file_pics.exists()) {
				if (!file_pics.createNewFile()) {
					return false;
				}
			}
			
			String path_papers = path_sd_root + "/" + "Papers";
			File file_papers = new File(path_papers);
			if (!file_papers.exists()) {
				if (!file_papers.createNewFile()) {
					return false;
				}
			}

			return true;

		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}
	}
	
	static public boolean createFolder(String path)
	{
	    File file = new File(path);
	    if (file.mkdir()) {
	        return true;
	    }
        return false;
	}
	
	public static boolean createFolder(File file, RefValue<String> errMsg)
	{
	    if (file.exists()) {
            errMsg.setValue("file already exists: " + file.getAbsolutePath());
            return false; 
        }
	    
	    return file.mkdir();
	}
	
	public static boolean createFile(File file, RefValue<String> errMsg)
	{
	    if (file.exists()) {
	        errMsg.setValue("file already exists: " + file.getAbsolutePath());
	        return false; 
	    }
	    
	    try {
	        file.createNewFile();
	        return true;
	    }
	    catch (IOException e) {
	        errMsg.setValue("create file failed: " + e.getMessage());
            e.printStackTrace();
        }
	    
	    return false;
	}
	
	static public boolean createFile(String path)
    {
	    File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
                return true;
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }
	
	static public boolean reName(File file, File newFile)
	{
	    if (file.renameTo(newFile)) {
            return true;
        }
	    return false;
	}
	
	static public boolean delete(File file)
	{
	    if (file.exists()) {
	        if (file.isFile()) {
	            return file.delete();
	        }
	        else {
	            return SDFileFactory.deleteFile(file);
	        }
        }
	    
	    return true;
	}
	
    static private boolean deleteFile(File file)
    {
        if (file.exists()) {
            File currentFile = new File(file.toString());
            File[] files = currentFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (!files[i].delete()) {
                        return false;
                    }
                }
                else if (files[i].isDirectory()) {
                    deleteFile(files[i]);
                }
            }

            if (!file.delete()) {
                return false;
            }
        }
        return true;
    }
    
    static public int fileQuantity(String path)
    {
        int count = 1;
        
        count = count + countFile(path);
        sCount = 0;

        return count;
    }
    
    static int sCount = 0;
    static private int countFile(String path)
    {
        File file = new File(path);
        
        File[] files = file.listFiles();
        if (files != null && files.length != 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    sCount++;
                } else if (files[i].isDirectory()) {
                    sCount++;
                    countFile(files[i].getPath());
                }
            }
        }
        
        return sCount;
    }
    
    static public boolean cut(File sourceFile, File file)
    {
        File copyFile = new File(file.getPath(), sourceFile.getName());
        if (copyFile.exists()) {
            return false;
        }

        if (sourceFile.isFile()) {
            cutFile(sourceFile, copyFile);
        }
        else {
            cutDirectory(sourceFile, copyFile);
        }
        
        return true;
    }
    
    static public boolean cutFile(File sourceFile, File file)
    {
        try {
            if (!file.createNewFile()) {
                return false;
            }

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(file);
            int i = 0;
            byte[] b = new byte[6120];

            while ((i = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, i);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
        catch (IOException e) {
            return false;
        }
        
        return true;
    }
    
    static public boolean cutDirectory(File sourceFile, File file)
    {
        if (!file.mkdir()) {
            return false;
        }
        File[] files = sourceFile.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    cut(files[i], file);
                }
                else if (files[i].isDirectory()) {
                    cut(files[i], file);
                }
            }
        }
        
        return true;
    }
}

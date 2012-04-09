/**
 * 
 */
package com.onyx.android.sdk.data.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author joy
 *
 */
public class FileUtil
{
    public static String getFileExtension(String fileName)
    {
        int dot_pos = fileName.lastIndexOf('.');
        if (0 <= dot_pos) {
            return fileName.substring(dot_pos + 1);
        }
        
        return "";
    }
    public static String getFileExtension(File file)
    {
        return getFileExtension(file.getName());
    }
    
    public static String getFileNameWithoutExtension(String fileName)
    {
        int dot_pos = fileName.lastIndexOf('.');
        if (dot_pos < 0) {
            return fileName;
        }
        
        return fileName.substring(0, dot_pos);
    }
    
    public static String getFilePathFromUri(String uri)
    {
        final String PREFIX = "file://";
        return uri.substring(PREFIX.length());
    }
    
    public static String computeMD5(File file) throws IOException, NoSuchAlgorithmException
    {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        
        if (!file.isFile()) {
            throw new IllegalArgumentException();
        }
        
        byte[] digest_buffer = getDigestBuffer(file);
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(digest_buffer);
        byte[] out = md.digest();
        
        final char hex_digits[] = {
                '0', '1', '2', '3', 
                '4', '5', '6', '7',
                '8', '9', 'a', 'b', 
                'c', 'd', 'e', 'f' }; 
        
        char str[] = new char[out.length * 2];
        for (int i = 0; i < out.length; i++) {
            int j = i << 1;
            str[j] = hex_digits[(out[i] >> 4) & 0x0F];
            str[j + 1] = hex_digits[out[i] & 0x0F];
        }
        
        return String.valueOf(str);
    }
    
    /**
     * never return null
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private static byte[] getDigestBuffer(File file) throws IOException
    {
        final int digest_block_length = 512;

        byte[] digest_buffer = null;
        
        RandomAccessFile rf = null;
        
        try {
        rf = new RandomAccessFile(file, "r");
        
        long file_size = rf.length();
        
        // TODO: what about an empty file?
        if (file_size <= (digest_block_length * 3)) { 
            digest_buffer = new byte[(int)file_size];
            rf.read(digest_buffer);
        } 
        else {
            // 3 digest blocks, head, mid, end
            digest_buffer = new byte[3 * digest_block_length];
            rf.seek(0);
            rf.read(digest_buffer, 0, digest_block_length); 
            rf.seek((file_size / 2) - (digest_block_length / 2));
            rf.read(digest_buffer, digest_block_length, digest_block_length);
            rf.seek(file_size - digest_block_length);
            rf.read(digest_buffer, 2 * digest_block_length, digest_block_length);
        }
        }
        finally {
            if (rf != null) {
                rf.close();
            }
        }
        
        assert(digest_buffer != null);
        return digest_buffer;
    }
}

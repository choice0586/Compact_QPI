package com.example.compact_qpi.video_acticity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class Video_4_SaveImage {
    // public constants
    public final static String IpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    public final static String HostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    //******************************************************************************
    // 이미지 저장하는 Class ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    //******************************************************************************
    public static String DPC_SAVE(ContentResolver contentResolver, Bitmap source, String title, String description)
    {
        File snapshot;
        Uri url;
        try
        {
            // get/create the snapshots folder
            File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File rpi = new File(pictures, "DPC_SAVE");

            if (!rpi.exists())
            {
                rpi.mkdirs();
            }

            // 여기 stream에서 python 코드 돌려서
            // save the file within the snapshots folder
            snapshot = new File(rpi, title);
            OutputStream stream = new FileOutputStream(snapshot);
            source.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();



            // create the content values
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, title);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
            if (description != null)
            {
                values.put(MediaStore.Images.Media.DESCRIPTION, description);
            }
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, snapshot.toString().toLowerCase(Locale.US).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, snapshot.getName().toLowerCase(Locale.US));
            values.put("_data", snapshot.getAbsolutePath());

            // insert the image into the database
            url = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


        }
        catch (Exception ex)
        {
            return null;
        }

        // return the URL
        return (url != null) ? url.toString() : null;
    }
    public static String saveImage(ContentResolver contentResolver, Bitmap source, String title, String description)
    {
        File snapshot;
        Uri url;
        try
        {
            // get/create the snapshots folder
            File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File rpi = new File(pictures, "QPI");

            if (!rpi.exists())
            {
                rpi.mkdirs();
            }

            // save the file within the snapshots folder
            snapshot = new File(rpi, title);
            OutputStream stream = new FileOutputStream(snapshot);
            source.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();


            // create the content values
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, title);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
            if (description != null)
            {
                values.put(MediaStore.Images.Media.DESCRIPTION, description);
            }
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, snapshot.toString().toLowerCase(Locale.US).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, snapshot.getName().toLowerCase(Locale.US));
            values.put("_data", snapshot.getAbsolutePath());

            // insert the image into the database
            url = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        catch (Exception ex)
        {
            return null;
        }

        // return the URL
        return (url != null) ? url.toString() : null;
    }

}

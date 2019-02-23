package com.kinklink.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageRotationHelper {

    //check Image roation by angle
    public static int checkPictureRotatation(Uri uri) throws IOException {
        int angle = 0;

        ExifInterface exifInterface = new ExifInterface(uri.getPath());
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;

            case ExifInterface.ORIENTATION_NORMAL:
                angle = 0;
            default:
                angle = 0;
        }


        return angle;
    }

    // check Image flip vertical(4) , horizontal(2)
    public static int checkIslFlipped(Uri uri) throws IOException {
        // create new matrix for transformation
        int flippedType = 0;
        ExifInterface exifInterface = new ExifInterface(uri.getPath());
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                flippedType = 2;
                break;

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                flippedType = 4;
                break;

            default:
                Log.i("43647fd", "" + Build.MODEL + " " + Build.MANUFACTURER);
                   /* if(Build.MANUFACTURER.contains("HMD")) flippedType=2;
                    else*/
                flippedType = 0;
        }


        return flippedType;
    }

    // doing Image roation and flip vertical(4) , horizontal(2)
    public static Bitmap rotateAndFlippedImage(Bitmap source, float angle, int flipType) {
        Matrix matrix = new Matrix();
        if (angle == 0 && flipType == 0) return null;

        else if (angle != 0 && flipType != 0) {
            matrix.postRotate(angle);
            if (flipType == 2) matrix.preScale(-1.0f, 1.0f);
            else if (flipType == 4) matrix.preScale(1.0f, -1.0f);
        } else if (angle != 0 && flipType == 0) matrix.postRotate(angle);
        else if (angle == 0 && flipType != 0) {
            if (flipType == 2) matrix.preScale(-1.0f, 1.0f);
            else if (flipType == 4) matrix.preScale(1.0f, -1.0f);
        }

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static Bitmap imageCopressor(Bitmap original, Context mContext) throws IOException {
        Bitmap decoded = null;
        int imageQuality = 60;
        double imageSize = getfileSizeInMB(original);
        if (imageSize > 1 && imageSize <= 2) {
            imageQuality = 40;
        } else if (imageSize > 2 && imageSize <= 3) {
            imageQuality = 35;
        } else if (imageSize > 3 && imageSize <= 4) {
            imageQuality = 25;
        } else if (imageSize > 4 && imageSize <= 5) {
            imageQuality = 20;
        } else if (imageSize > 5 && imageSize <= 6) {
            imageQuality = 15;
        } else if (imageSize > 6) {
            imageQuality = 10;
        }


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        original.compress(Bitmap.CompressFormat.JPEG, imageQuality, byteArrayOutputStream);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        //getfileSizeInMB(decoded);
        byteArrayOutputStream.close();
        byteArrayOutputStream.flush();


        return decoded;

    }


    public static File persistImage(Context mContext, Bitmap bitmap, String name) {
        File filesDir = mContext.getFilesDir();
        File imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageFile;
    }


    public static double getfileSizeInMB(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        long fileSizeInBytes = imageInByte.length;

        // Get length of file in bytes
        //long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        double fileSizeInKB = fileSizeInBytes / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        double fileSizeInMB = fileSizeInKB / 1024;
        Log.i("size0983", "" + fileSizeInMB);
        return fileSizeInMB;
    }
}

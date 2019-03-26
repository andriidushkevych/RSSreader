/**
 * @file   PictureDownloader.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Asynchronous picture downloader
 */

package com.mad.cbcnewsreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Downloads and caches pictures on disk asynchronously
 */
public class PictureDownloader {

    /**
     * Directory to store cached pictures in
     */
    private File cacheDir;

    /**
     * Map of cached pictures
     */
    private final HashMap<String, Bitmap> pictureCache = new HashMap<>();

    /**
     * List of unique URLs to download in the next job
     */
    private HashSet<String> downloadUrls = new HashSet<>();

    /**
     * Interface to notify when pictures have finished downloading
     */
    private PicturesDownloadedInterface cb;

    /**
     * Singleton implementation
     */
    private static PictureDownloader instance;

    /**
     * Retrieve the singleton instance
     * @return The singleton object
     */
    public static PictureDownloader getInstance() {
        return instance;
    }

    /**
     * Initializes the downloader
     * @param ctx Android context
     * @param cb Interface to notify
     */
    public static void Initialize(Context ctx, PicturesDownloadedInterface cb) {
        instance = new PictureDownloader();
        instance.cacheDir = ctx.getCacheDir();
        instance.cb = cb;
        instance.LoadCachedPictures();
    }

    /**
     * Loads cached pictures from the disk
     */
    private void LoadCachedPictures() {
        File[] files = cacheDir.listFiles();
        for (File file: files) {
            if (file.isFile()) {
                Bitmap bmp = LoadBitmapFromFile(file.getAbsolutePath());
                if (bmp != null)
                    pictureCache.put(file.getName(), bmp);
            }
        }
    }

    /**
     * Converts picture URL to a unique hashmap key
     * @param url URL of the picture
     * @return Unique hashmap key
     */
    private String urlToKey(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(url.getBytes());
            byte[] digest = md.digest();
            StringBuffer result = new StringBuffer();
            for (byte byt : digest)
                result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Converts picture URL to its path on the disk
     * @param url URL of the picture
     * @return Path inside cache directory on disk
     */
    private String urlToFilename(String url) {
        return cacheDir + "/" + urlToKey(url);
    }

    /**
     * Adds URL to download list if it's not downloaded yet
     * @param url URL to retrieve
     */
    public void EnsureCached(String url) {
        String key = urlToKey(url);
        Boolean exists;
        synchronized (pictureCache) {
            exists = pictureCache.containsKey(key);
        }
        if (!exists) {
            downloadUrls.add(url);
        }
    }

    /**
     * Downloads all of the requested URLs in background
     */
    public void BatchDownload() {
        DownloadImageTask task = new DownloadImageTask(downloadUrls.toArray(new String[0]));
        task.execute();
        downloadUrls.clear();
    }

    /**
     * Retrieves a cached bitmap using its URL
     * @param url URL of the picture
     * @return Bitmap or NULL if not downloaded yet
     */
    public Bitmap GetBitmap(String url) {
        String key = urlToKey(url);
        Bitmap res = null;
        synchronized (pictureCache) {
            if (pictureCache.containsKey(key)) {
                res = pictureCache.get(key);
            }
        }
        return res;
    }

    /**
     * Loads bitmap from a file on the disk
     * @param path Path to the bitmap
     * @return Bitmap object or NULL of failed to load
     */
    private Bitmap LoadBitmapFromFile(String path) {
        try {
            InputStream input = new FileInputStream(path);
            return BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Downloads a bitmap from URL to a cached file
     * @param url URL to download
     * @return Downloaded bitmap object
     */
    private Bitmap DownloadToFile(String url) {
        try {
            InputStream input = new java.net.URL(url).openStream();
            OutputStream output = new FileOutputStream(urlToFilename(url));
            byte data[] = new byte[4096];

            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return LoadBitmapFromFile(urlToFilename(url));
    }

    /**
     * Task to download images in background.
     * Based on https://stackoverflow.com/a/9288544
     */
    private class DownloadImageTask extends AsyncTask<Void, Void, Void> {
        /**
         * List of URLs to download
         */
        private String[] urls;

        /**
         * Constructs the task
         * @param urls URLs to download
         */
        DownloadImageTask(String[] urls) {
            this.urls = urls;
        }

        /**
         * Runs task in background
         * @param voids Unused
         * @return Unused
         */
        @Override
        protected Void doInBackground(Void... voids) {
            for (String url: urls) {
                Bitmap bmp = DownloadToFile(url);

                if (bmp == null)
                    continue;

                String key = urlToKey(url);
                synchronized (pictureCache) {
                    pictureCache.put(key, bmp);
                }
            }

            return null;
        }

        /**
         * Calls the callback interface once all images are downloaded
         * @param v Unused
         */
        @Override
        protected void onPostExecute(Void v) {
            cb.onPicturesDownloaded();
        }
    }

}

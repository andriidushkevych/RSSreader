/**
 * @file   PicturesDownloadedInterface.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Interface to handle event when pictures are downloaded.
 */

package com.mad.cbcnewsreader;

/**
 * Interface used by PictureDownloader to notify when pictures have finished downloading
 */
public interface PicturesDownloadedInterface {
    /**
     * Called when pictures are downloaded
     */
    void onPicturesDownloaded();
}

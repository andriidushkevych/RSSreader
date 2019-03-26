/**
 * @file   NewsUpdatedInterface.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Interface to handle asynchronous news updates.
 */

package com.mad.cbcnewsreader;

import java.util.ArrayList;

/**
 * Interface used when news are updated
 */
public interface NewsUpdatedInterface {
    /**
     * Called after news are downloaded and parsed
     * @param news List of parsed news
     */
    void onNewsUpdated(ArrayList<NewsEntry> news);
}

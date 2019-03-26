/**
 * @file   NewsUpdater.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Updates news over the network
 */

package com.mad.cbcnewsreader;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Handles asynchronous updates over the network
 */
public class NewsUpdater extends AsyncTask<Void, Void, ArrayList<NewsEntry>> {

    /**
     * Interface to call when news have finished downloading
     */
    private NewsUpdatedInterface cb;

    /**
     * Asynchronous picture downloader
     */
    private PictureDownloader downloader;

    /**
     * Creates a NewsUpdater
     * @param cb Interface to use for notifications
     * @param downloader Picture downloader
     */
    NewsUpdater(NewsUpdatedInterface cb, PictureDownloader downloader) {
        this.cb = cb;
        this.downloader = downloader;
    }

    /**
     * The background task which downloads the news
     * @param voids Unused
     * @return List of news downloaded and parsed
     */
    @Override
    protected ArrayList<NewsEntry> doInBackground(Void... voids) {
        ArrayList<NewsEntry> news = new ArrayList<>();

        try {
            URL url = new URL("https://www.cbc.ca/cmlink/rss-topstories");

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

            NewsEntry entry = null;

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    String name = xpp.getName();
                    // Next news element
                    if (name.equals("item")) {
                        // Insert the previous entry
                        if (entry != null) {
                            news.add(entry);
                        }
                        entry = new NewsEntry();
                    }
                    if (entry != null) {
                        switch (name) {
                            case "title":
                                entry.setTitle(xpp.nextText());
                                break;
                            case "link":
                                entry.setLink(xpp.nextText());
                                break;
                            case "description":
                                entry.setDescription(xpp.nextText());
                                break;
                            case "guid":
                                entry.setGuid(xpp.nextText());
                                break;
                            case "pubDate":
                                entry.setPubDate(xpp.nextText());
                                break;
                            case "author":
                                entry.setAuthor(xpp.nextText());
                                break;
                            case "category":
                                entry.setCategory(xpp.nextText());
                                break;
                        }
                    }
                }

                eventType = xpp.next();
            }

            // Insert last news entry
            if (entry != null)
                news.add(entry);

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        return news;
    }

    /**
     * Called once news have finished downloading
     * @param news List of news
     */
    @Override
    protected void onPostExecute(ArrayList<NewsEntry> news) {
        // Prepare picture downloader to download pictures
        for (NewsEntry entry: news)
            downloader.EnsureCached(entry.getPictureUrl());
        // Create async download job
        downloader.BatchDownload();

        cb.onNewsUpdated(news);
    }

}

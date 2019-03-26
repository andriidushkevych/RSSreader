/**
 * @file   MainActivity.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Main application activity displays list of the news
 */

package com.mad.cbcnewsreader;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Activity which displays a list of news entries
 */
public class MainActivity extends AppCompatActivity implements NewsUpdatedInterface, PicturesDownloadedInterface {

    static String LOG_TAG = "newsapp";

    /**
     * Pull-to-refresh layout
     */
    private SwipeRefreshLayout swipeRefresh;

    /**
     * News list view
     */
    private ListView newsList;

    /**
     * Background picture downloader
     */
    private PictureDownloader downloader;

    /**
     * Database object
     */
    public static NewsEntryDB db;

    /**
     * Adapter to put news into a ListView
     */
    private NewsAdapter adapter;

    /**
     * Initializes the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefresh = findViewById(R.id.swiperefresh);
        newsList = findViewById(R.id.newslist);
        db = new NewsEntryDB(this);
        PictureDownloader.Initialize(this, this);
        downloader = PictureDownloader.getInstance();

        ArrayList<NewsEntry> newsEntries = db.getNewsEntries();
        loadNews(newsEntries);

        swipeRefresh.setOnRefreshListener(

            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                    UpdateNews();
                }
            }
        );

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewsEntry entry = (NewsEntry) newsList.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("guid", entry.getGuid());
                startActivity(intent);
            }
        });
    }

    /**
     * Loads news into the ListView
     * @param news List of news to display
     */
    public void loadNews(ArrayList<NewsEntry> news) {
        Collections.sort(news);
        adapter = new NewsAdapter(this, news, downloader);
        newsList.setAdapter(adapter);
    }

    /**
     * Updates news over the network
     */
    public void UpdateNews() {
        NewsUpdater updater = new NewsUpdater(this, downloader);
        updater.execute();
    }

    /**
     * Handles news update events
     * @param news List of news retrieved over the network
     */
    @Override
    public void onNewsUpdated(ArrayList<NewsEntry> news) {
        swipeRefresh.setRefreshing(false);
        for (NewsEntry ne: news) {
            if(db.getByGuid(ne.getGuid()) == null) {
                db.insertNewsEntry(ne);
            } else {
                db.updateNewsEntry(ne);
            }
        }

        // Reload news from db to get both new and old news
        ArrayList<NewsEntry> newsEntries = db.getNewsEntries();
        loadNews(newsEntries);
    }

    /**
     * Initializes the top right corner menu
     * @param menu Menu to create
     * @return True to display the menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Called when a menu item is selected
     * @param item Item that is selected
     * @return Whether the action was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Check if user triggered a refresh:
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "Refresh menu item selected");

                // Signal SwipeRefreshLayout to start the progress indicator
                swipeRefresh.setRefreshing(true);

                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                UpdateNews();

                return true;
        }

        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when all pictures have finished downloading
     */
    @Override
    public void onPicturesDownloaded() {
        adapter.notifyDataSetChanged();
    }
}

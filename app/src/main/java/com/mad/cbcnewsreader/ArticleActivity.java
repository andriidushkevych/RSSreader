/**
 * @file   ArticleActivity.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Article details activity
 */

package com.mad.cbcnewsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity which shows article details
 */
public class ArticleActivity extends AppCompatActivity implements PicturesDownloadedInterface {

    /**
     * The news entry to show, retrieved from the DB.
     */
    private NewsEntry entry;

    /**
     * Initializes the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Get GUID of article to display
        Intent intent = getIntent();
        String guid = intent.getStringExtra("guid");

        // Get the news entry
        entry =  MainActivity.db.getByGuid(guid);

        getSupportActionBar().setTitle("Story");

        // Article title
        TextView articleTitle = findViewById(R.id.articleTitle);
        articleTitle.setText(entry.getTitle());

        // Picture
        UpdatePicture();

        // Article description
        TextView articleDescription = findViewById(R.id.articleDescription);
        articleDescription.setText(entry.getDescriptionPlaintext());

        // Link to read more
        TextView articleReadMore = findViewById(R.id.articleReadMore);
        articleReadMore.setText(getString(R.string.read_more, entry.getLink()));
    }

    /**
     * Updates the article image
     */
    private void UpdatePicture() {
        ImageView articleImage = findViewById(R.id.articleImage);
        articleImage.setImageBitmap(PictureDownloader.getInstance().GetBitmap(entry.getPictureUrl()));
    }

    /**
     * Called when pictures have finished downloading in background
     */
    @Override
    public void onPicturesDownloaded() {
        // Update the picture in case it got dynamically downloaded
        UpdatePicture();
    }
}

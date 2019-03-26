/**
 * @file   NewsAdapter.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Adapter to show news in ListView.
 */

package com.mad.cbcnewsreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter that maps NewsEntry to ListView.
 */
public class NewsAdapter extends ArrayAdapter<NewsEntry> {

    /**
     * List of news to display
     */
    private ArrayList<NewsEntry> news;

    /**
     * Android context
     */
    private Context context;

    /**
     * Pictures downloader object storing cached pictures
     */
    private PictureDownloader pictures;

    /**
     * Creates the NewsAdapter
     * @param context Android context
     * @param news List of news to display
     * @param pictures A PictureDownloader instance associated with these news
     */
    public NewsAdapter(Context context, ArrayList<NewsEntry> news, PictureDownloader pictures) {
        super(context, 0, news);
        this.context = context;
        this.news = news;
        this.pictures = pictures;
    }

    /**
     * Creates a view for the list entry
     * @param position Position in the list
     * @param convertView View to reuse
     * @param parent Parent group
     * @return View to display
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);

        NewsEntry entry = news.get(position);

        TextView name = listItem.findViewById(R.id.articleName);
        name.setText(entry.getTitle());

        TextView category = listItem.findViewById(R.id.articleCategory);
        category.setText(entry.getCategory());

        ImageView image = listItem.findViewById(R.id.articleImage);
        image.setImageBitmap(pictures.GetBitmap(entry.getPictureUrl()));

        return listItem;
    }
}

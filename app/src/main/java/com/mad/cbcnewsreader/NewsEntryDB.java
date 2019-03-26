/**
 * @file   NewsEntryDB.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  Database access layer for storing news articles.
 */

package com.mad.cbcnewsreader;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Stores and retrieves news articles with SQLite.
 */
public class NewsEntryDB {
    /**
     * The underlying database object
     */
    private SQLiteDatabase db;

    /**
     * Database helper to handle new database creation
     */
    private DBHelper dbHelper;

    /**
     * Constructs the database object
     * @param context Android context
     */
    public NewsEntryDB(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * Opens database for read
     */
    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    /**
     * Opens database for write
     */
    private void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the database
     */
    private void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    // database constants
    public static final String DB_NAME = "mad_a02.db";
    public static final int    DB_VERSION = 1;

    // task table constants
    public static final String NEWS_ENTRY_TABLE = "newsentry";

    public static final String NEWS_ENTRY_ID = "_id";
    public static final int    NEWS_ENTRY_ID_COL = 0;

    public static final String NEWS_ENTRY_TITLE = "title";
    public static final int    NEWS_ENTRY_TITLE_COL = 1;

    public static final String NEWS_ENTRY_LINK = "link";
    public static final int    NEWS_ENTRY_LINK_COL = 2;

    public static final String NEWS_ENTRY_GUID = "guid";
    public static final int    NEWS_ENTRY_GUID_COL = 3;

    public static final String NEWS_ENTRY_PUB_DATE = "pubdate";
    public static final int    NEWS_ENTRY_PUB_DATE_COL = 4;

    public static final String NEWS_ENTRY_AUTHOR = "author";
    public static final int    NEWS_ENTRY_AUTHOR_COL = 5;

    public static final String NEWS_ENTRY_CATEGORY = "category";
    public static final int    NEWS_ENTRY_CATEGORY_COL = 6;

    public static final String NEWS_ENTRY_DESCRIPTION = "description";
    public static final int    NEWS_ENTRY_DESCRIPTION_COL = 7;

    public static final String CREATE_NEWS_ENTRY_TABLE =
            "CREATE TABLE " + NEWS_ENTRY_TABLE + " (" +
                    NEWS_ENTRY_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NEWS_ENTRY_TITLE          + " TEXT    NOT NULL, " +
                    NEWS_ENTRY_LINK           + " TEXT    NOT NULL, " +
                    NEWS_ENTRY_GUID           + " TEXT    UNIQUE, " +
                    NEWS_ENTRY_PUB_DATE       + " TEXT, " +
                    NEWS_ENTRY_AUTHOR         + " TEXT, " +
                    NEWS_ENTRY_CATEGORY       + " TEXT, " +
                    NEWS_ENTRY_DESCRIPTION    + " TEXT);";

    public static final String DROP_NEWS_ENTRY_TABLE =
            "DROP TABLE IF EXISTS " + NEWS_ENTRY_TABLE;


    /**
     * Database helper handling database creation and updates
     */
    private static class DBHelper extends SQLiteOpenHelper {

        /**
         * Creates the helper
         * @param context Android context
         */
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Called when a database needs to be created
         * @param db Database
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_NEWS_ENTRY_TABLE);
        }

        /**
         * Called when database needs to be upgraded
         * @param db Database
         * @param i Old version
         * @param i1 New version
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL(DROP_NEWS_ENTRY_TABLE);
            onCreate(db);
        }
    }

    /**
     * Inserts a new news entry
     * @param newsEntry Entry to insert
     * @return Primary key of the new row
     */
    public long insertNewsEntry(NewsEntry newsEntry) {
        ContentValues cv = new ContentValues();
        cv.put(NEWS_ENTRY_TITLE, newsEntry.getTitle());
        cv.put(NEWS_ENTRY_LINK, newsEntry.getLink());
        cv.put(NEWS_ENTRY_GUID, newsEntry.getGuid());
        cv.put(NEWS_ENTRY_PUB_DATE, newsEntry.getPubDate());
        cv.put(NEWS_ENTRY_AUTHOR, newsEntry.getAuthor());
        cv.put(NEWS_ENTRY_CATEGORY, newsEntry.getCategory());
        cv.put(NEWS_ENTRY_DESCRIPTION, newsEntry.getDescription());

        this.openWriteableDB();
        long rowID = db.insert(NEWS_ENTRY_TABLE, null, cv);
        this.closeDB();

        return rowID;
    }

    /**
     * Retrieves an article by its GUID
     * @param guid GUID to look up
     * @return News entry or NULL
     */
    public NewsEntry getByGuid(String guid) {
        NewsEntry newsEntry = null;
        String where = NEWS_ENTRY_GUID + "= ? ";
        this.openReadableDB();
        Cursor cursor = db.query(NEWS_ENTRY_TABLE, null, where, new String[] { guid }, null, null, null);
        if (cursor.moveToFirst()) {
            String title = cursor.getString(NEWS_ENTRY_TITLE_COL);
            String link = cursor.getString(NEWS_ENTRY_LINK_COL);
            String pubdate = cursor.getString(NEWS_ENTRY_PUB_DATE_COL);
            String author = cursor.getString(NEWS_ENTRY_AUTHOR_COL);
            String category = cursor.getString(NEWS_ENTRY_CATEGORY_COL);
            String description = cursor.getString(NEWS_ENTRY_DESCRIPTION_COL);
            newsEntry = new NewsEntry(title, link, guid, pubdate, author, category, description);
        }
        cursor.close();
        this.closeDB();
        return newsEntry;
    }

    /**
     * Updates a news entry
     * @param newsEntry Entry to update
     * @return Returns number of rows updated
     */
    public int updateNewsEntry(NewsEntry newsEntry) {
        ContentValues cv = new ContentValues();
        cv.put(NEWS_ENTRY_TITLE, newsEntry.getTitle());
        cv.put(NEWS_ENTRY_LINK, newsEntry.getLink());
        cv.put(NEWS_ENTRY_PUB_DATE, newsEntry.getPubDate());
        cv.put(NEWS_ENTRY_AUTHOR, newsEntry.getAuthor());
        cv.put(NEWS_ENTRY_CATEGORY, newsEntry.getCategory());
        cv.put(NEWS_ENTRY_DESCRIPTION, newsEntry.getDescription());

        String where = NEWS_ENTRY_GUID + "= ? ";
        this.openWriteableDB();
        int rowCount = db.update(NEWS_ENTRY_TABLE, cv, where,  new String[] { newsEntry.getGuid() });
        this.closeDB();
        return rowCount;
    }

    /**
     * Get all news entries
     * @return List of news entries in the database
     */
    public ArrayList<NewsEntry> getNewsEntries() {
        ArrayList<NewsEntry> newsEntries = new ArrayList<NewsEntry>();

        this.openReadableDB();
        Cursor cursor = db.query(NEWS_ENTRY_TABLE, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String title = cursor.getString(NEWS_ENTRY_TITLE_COL);
                String link = cursor.getString(NEWS_ENTRY_LINK_COL);
                String guid = cursor.getString(NEWS_ENTRY_GUID_COL);
                String pubdate = cursor.getString(NEWS_ENTRY_PUB_DATE_COL);
                String author = cursor.getString(NEWS_ENTRY_AUTHOR_COL);
                String category = cursor.getString(NEWS_ENTRY_CATEGORY_COL);
                String description = cursor.getString(NEWS_ENTRY_DESCRIPTION_COL);
                NewsEntry newsEntry = new NewsEntry(title, link, guid, pubdate, author, category, description);
                newsEntries.add(newsEntry);
                cursor.moveToNext();
            }
        }
        cursor.close();
        this.closeDB();
        return newsEntries;
    }
}

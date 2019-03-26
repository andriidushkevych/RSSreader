/**
 * @file   NewsEntry.java
 * @author Andrey Dushkevych, Ilia Zhuravlev
 * @date   2019-03-25
 * @brief  A single news entry
 */

package com.mad.cbcnewsreader;

import android.text.Html;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * News article class
 */
public class NewsEntry implements Comparable<NewsEntry> {
    /**
     * Article title
     */
    private String title;

    /**
     * URL to the full article
     */
    private String link;

    /**
     * Unique identifier of the article
     */
    private String guid;

    /**
     * Date of publication
     */
    private String pubdate;

    /**
     * Author
     */
    private String author;

    /**
     * Categories
     */
    private String category;

    /**
     * Article short description
     */
    private String description;

    /**
     * Date of publication as a Date object
     */
    private Date datePublished;

    /**
     * Creates an empty article object
     */
    public NewsEntry() {

    }

    /**
     * Creates an article object with provided data
     * @param title Title
     * @param link URL
     * @param guid Unique ID
     * @param pubdate Publication date as a string
     * @param author Author
     * @param category Category
     * @param description Article description
     */
    public NewsEntry(String title, String link, String guid, String pubdate,
                    String author, String category, String description) {
        this.title = title;
        this.link = link;
        this.guid = guid;
        this.pubdate = pubdate;
        this.author = author;
        this.category = category;
        this.description = description;
        parseDate();
    }

    /**
     * Title getter
     * @return Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Title setter
     * @param title Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * URL getter
     * @return URL of the article
     */
    public String getLink() {
        return link;
    }

    /**
     * URL setter
     * @param link URL of the article
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * GUID getter
     * @return GUID
     */
    public String getGuid() { return guid; }

    /**
     * GUID setter
     * @param guid GUID
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Publication date (string) getter
     * @return Publication date
     */
    public String getPubDate() {
        return pubdate;
    }

    /**
     * Publication date (string) setter
     * @param pubdate Publication date
     */
    public void setPubDate(String pubdate) {
        this.pubdate = pubdate;
        parseDate();
    }

    /**
     * Author getter
     * @return Author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Author setter
     * @param author Author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Category getter
     * @return Category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Category setter
     * @param category Category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Description getter
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Description setter
     * @param description Description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Date (as a Date object) getter
     * @return Date object
     */
    public Date getDatePublished() { return datePublished; }

    /**
     * Extracts and returns a URL to article image
     * @return URL of article image, or a fallback URL
     */
    public String getPictureUrl() {
        Pattern pattern = Pattern.compile("<img src='(.*?)'");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "https://www.cbc.ca/a/favicon.ico";
    }

    /**
     * Converts article description from HTML to plaintext
     * @return Article description with HTML tags stripped
     */
    public String getDescriptionPlaintext() {
        // https://stackoverflow.com/a/10581020
        return Html.fromHtml(description).toString().replace('\n', (char) 32)
                .replace((char) 160, (char) 32).replace((char) 65532, (char) 32).trim();
    }

    /**
     * Parses the date from string into a Date object
     */
    private void parseDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
        try {
            this.datePublished = formatter.parse(this.pubdate == null ? "" : this.pubdate);
        } catch (ParseException e) {
            // Generate a fallback date
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2000);
            calendar.set(Calendar.MONTH, 1);
            calendar.set(Calendar.DATE, 1);
            this.datePublished = calendar.getTime();
        }
    }

    /**
     * Compares article to another
     * @param o Other article
     * @return Comparable result
     */
    @Override
    public int compareTo(NewsEntry o) {
        if (getDatePublished() == null || o.getDatePublished() == null)
            return 0;
        return o.getDatePublished().compareTo(getDatePublished());
    }
}

package com.archiedavid.www.booklisting;

/**
 * Created by Archie David on 11/09/2016.
 */
public class Book {

    private String mBookTitle;
    private String mBookAuthor;

    public Book(String bookTitle, String bookAuthor) {
        mBookTitle = bookTitle;
        mBookAuthor = bookAuthor;
    }

    public String getMBookTitle() {
        return mBookTitle;
    }

    public String getMBookAuthor() {
        return mBookAuthor;
    }
}

package com.example.cucumber.book;

public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String isbn) {
        super("이미 등록된 ISBN입니다: " + isbn);
    }
}

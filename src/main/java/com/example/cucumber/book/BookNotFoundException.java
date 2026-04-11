package com.example.cucumber.book;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("도서를 찾을 수 없습니다. ID: " + id);
    }
}

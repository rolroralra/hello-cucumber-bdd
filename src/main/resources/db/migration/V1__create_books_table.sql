-- V1: 도서 테이블 생성
CREATE TABLE books
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(255)   NOT NULL,
    author     VARCHAR(255)   NOT NULL,
    isbn       VARCHAR(20)    NOT NULL UNIQUE,
    price      DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock      INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    created_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_books_isbn ON books (isbn);
CREATE INDEX idx_books_author ON books (author);

COMMENT ON TABLE books IS '도서 정보';
COMMENT ON COLUMN books.isbn IS 'ISBN-13 형식';
COMMENT ON COLUMN books.stock IS '재고 수량';

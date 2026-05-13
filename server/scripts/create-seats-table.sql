-- ============================================================
-- Tạo bảng seats cho hệ thống quản lý đặt vé máy bay
-- Chạy: mysql -u root -p airline_management < scripts/create-seats-table.sql
-- ============================================================

USE airline_management;

-- Tạo bảng seats nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS seats (
    seat_id      INT AUTO_INCREMENT PRIMARY KEY,
    airplane_id  INT          NOT NULL,
    seat_number  VARCHAR(10)  NOT NULL,
    class_type   ENUM('ECONOMY', 'BUSINESS', 'FIRST') NOT NULL DEFAULT 'ECONOMY',
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_seats_airplane
        FOREIGN KEY (airplane_id) REFERENCES airplanes(airplane_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_seat_per_airplane
        UNIQUE (airplane_id, seat_number)
);

-- Index để tăng tốc truy vấn theo airplane_id
CREATE INDEX IF NOT EXISTS idx_seats_airplane ON seats(airplane_id);

SELECT 'Bảng seats đã được tạo thành công.' AS result;

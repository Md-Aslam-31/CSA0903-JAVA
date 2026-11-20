CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;

CREATE TABLE IF NOT EXISTS registrations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(30) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    event_name VARCHAR(80) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_event UNIQUE (student_id, event_name)
);

CREATE INDEX idx_registrations_event_id ON registrations (event_name, id);

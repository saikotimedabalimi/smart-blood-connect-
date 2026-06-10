CREATE DATABASE IF NOT EXISTS smart_blood_connect;
USE smart_blood_connect;

CREATE TABLE IF NOT EXISTS blood_groups (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(5) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    location VARCHAR(150) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    notification_enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS donors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    blood_group VARCHAR(5) NOT NULL,
    location VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    last_donation_date DATE NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    requester_user_id INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    location VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    reason TEXT NOT NULL,
    units_required INT NOT NULL DEFAULT 1,
    emergency TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(40) NOT NULL DEFAULT 'Open for Matching',
    matched_donor_summary VARCHAR(255),
    assigned_donor_user_id INT NULL,
    contact_name VARCHAR(100) NULL,
    contact_phone VARCHAR(20) NULL,
    hospital_name VARCHAR(150) NULL,
    required_by DATETIME NULL,
    formality_notes TEXT NULL,
    procurement_status VARCHAR(40) NOT NULL DEFAULT 'Awaiting donor response',
    completed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS donor_request_targets (
    id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    donor_user_id INT NOT NULL,
    response_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    response_note VARCHAR(255) NULL,
    responded_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_request_donor (request_id, donor_user_id),
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(255) NOT NULL,
    category VARCHAR(40) NOT NULL,
    reference_request_id INT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reference_request_id) REFERENCES requests(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS certificates (
    id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL UNIQUE,
    donor_user_id INT NOT NULL,
    requester_user_id INT NOT NULL,
    certificate_number VARCHAR(60) NOT NULL UNIQUE,
    issuer_type VARCHAR(30) NOT NULL,
    issuer_name VARCHAR(150) NOT NULL,
    issued_on DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS admin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS global_broadcasts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message TEXT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT IGNORE INTO blood_groups (group_name) VALUES
('A+'), ('A-'), ('B+'), ('B-'), ('AB+'), ('AB-'), ('O+'), ('O-');

INSERT IGNORE INTO admin (username, password) VALUES
('admin@smartblood.com', 'admin123');

INSERT IGNORE INTO users (id, name, email, password, phone, city, location, account_type, role, notification_enabled) VALUES
(1, 'Aman Sharma', 'aman@example.com', '123456Aa!', '9876543210', 'Jalandhar', 'Boys Hostel 1', 'DONOR', 'USER', 1),
(2, 'Riya Verma', 'riya@example.com', '123456Aa!', '9876543211', 'Jalandhar', 'Girls Hostel 2', 'DONOR', 'USER', 1),
(3, 'City Hospital', 'hospital@example.com', '123456Aa!', '9876543222', 'Jalandhar', 'Civil Hospital', 'RECEIVER', 'USER', 1),
(4, 'Govind Singh', 'govind@example.com', '123456Aa!', '9876543233', 'Ludhiana', 'Sector 12', 'DONOR', 'USER', 1);

INSERT IGNORE INTO donors (user_id, blood_group, location, city, last_donation_date, available) VALUES
(1, 'A+', 'Boys Hostel 1', 'Jalandhar', '2026-01-15', 1),
(2, 'O+', 'Girls Hostel 2', 'Jalandhar', '2025-12-10', 1),
(4, 'B+', 'Sector 12', 'Ludhiana', '2026-02-10', 1);

INSERT IGNORE INTO requests (
    id, requester_user_id, patient_name, blood_group, location, city, reason, units_required, emergency, status,
    matched_donor_summary, assigned_donor_user_id, contact_name, contact_phone, hospital_name, required_by,
    formality_notes, procurement_status, completed_at
) VALUES
(1, 3, 'Rahul Kumar', 'A+', 'Civil Hospital', 'Jalandhar', 'Emergency surgery support', 2, 1,
 'Open for Matching', 'Broadcast sent to 2 compatible donor(s).', NULL, NULL, NULL, NULL, NULL, NULL,
 'Awaiting donor response', NULL),
(2, 3, 'Meena Arora', 'A+', 'Civil Hospital', 'Jalandhar', 'Post-operative transfusion support', 1, 0,
 'Donation Completed', 'Aman Sharma (9876543210)', 1, 'Dr. Singh', '9876500011', 'Civil Hospital',
 '2026-04-30 10:00:00', 'Bring donor ID and blood bank slip to counter 2.', 'Completed', CURRENT_TIMESTAMP);

INSERT IGNORE INTO donor_request_targets (request_id, donor_user_id, response_status, response_note, responded_at) VALUES
(1, 1, 'PENDING', NULL, NULL),
(1, 2, 'PENDING', NULL, NULL),
(2, 1, 'ACCEPTED', 'Confirmed and donated successfully.', CURRENT_TIMESTAMP);

INSERT IGNORE INTO notifications (user_id, title, message, category, reference_request_id, is_read) VALUES
(1, 'Blood request nearby', 'City Hospital needs A+ blood in Jalandhar.', 'REQUEST_CREATED', 1, 0),
(3, 'Certificate issued', 'Donation for Meena Arora is complete and certificate is available for the donor.', 'DONATION_COMPLETED', 2, 1);

INSERT IGNORE INTO certificates (request_id, donor_user_id, requester_user_id, certificate_number, issuer_type, issuer_name, issued_on) VALUES
(2, 1, 3, 'SBC-2-20260430', 'GOVERNMENT', 'Smart Blood Connect Authority', '2026-04-30');

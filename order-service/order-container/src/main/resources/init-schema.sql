DROP SCHEMA IF EXISTS `order`;

CREATE SCHEMA `order`;

-- MySQL에서는 UUID를 생성하기 위해 UUID() 함수를 사용합니다.
-- UUID 관련 확장을 생성할 필요는 없습니다.

DROP TYPE IF EXISTS order_status;
-- MySQL 8.0에서는 ENUM 타입을 직접 생성할 수 없으므로 테이블 내에서 생성합니다.

DROP TABLE IF EXISTS `order`.orders;

CREATE TABLE `order`.orders
(
    id CHAR(36) NOT NULL,
    customer_id CHAR(36) NOT NULL,
    restaurant_id CHAR(36) NOT NULL,
    tracking_id CHAR(36) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    order_status ENUM('PENDING', 'PAID', 'APPROVED', 'CANCELLED', 'CANCELLING') NOT NULL,
    failure_messages VARCHAR(255),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS `order`.order_items;

CREATE TABLE `order`.order_items
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    sub_total DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id, order_id),
    CONSTRAINT FK_ORDER_ID FOREIGN KEY (order_id)
        REFERENCES `order`.orders(id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

DROP TABLE IF EXISTS `order`.order_address;

CREATE TABLE `order`.order_address
(
    id CHAR(36) NOT NULL,
    order_id CHAR(36) UNIQUE NOT NULL,
    street VARCHAR(255) NOT NULL,
    postal_code VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    PRIMARY KEY (id, order_id),
    CONSTRAINT FK_ORDER_ADDRESS_ORDER_ID FOREIGN KEY (order_id)
        REFERENCES `order`.orders(id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

DROP TYPE IF EXISTS saga_status;
DROP TYPE IF EXISTS outbox_status;
-- MySQL 8.0에서는 ENUM 타입을 직접 생성할 수 없으므로 테이블 내에서 생성합니다.

DROP TABLE IF EXISTS `order`.payment_outbox;

CREATE TABLE `order`.payment_outbox
(
    id CHAR(36) NOT NULL,
    saga_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    type VARCHAR(255) NOT NULL,
    payload JSON NOT NULL,
    outbox_status ENUM('STARTED', 'COMPLETED', 'FAILED') NOT NULL,
    saga_status ENUM('STARTED', 'FAILED', 'SUCCEEDED', 'PROCESSING', 'COMPENSATING', 'COMPENSATED') NOT NULL,
    order_status ENUM('PENDING', 'PAID', 'APPROVED', 'CANCELLED', 'CANCELLING') NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX payment_outbox_saga_status
    ON `order`.payment_outbox(type, outbox_status, saga_status);

-- MySQL에서는 복합 인덱스에 대해 유일 인덱스를 추가할 수 있습니다.

DROP TABLE IF EXISTS `order`.restaurant_approval_outbox;

CREATE TABLE `order`.restaurant_approval_outbox
(
    id CHAR(36) NOT NULL,
    saga_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    type VARCHAR(255) NOT NULL,
    payload JSON NOT NULL,
    outbox_status ENUM('STARTED', 'COMPLETED', 'FAILED') NOT NULL,
    saga_status ENUM('STARTED', 'FAILED', 'SUCCEEDED', 'PROCESSING', 'COMPENSATING', 'COMPENSATED') NOT NULL,
    order_status ENUM('PENDING', 'PAID', 'APPROVED', 'CANCELLED', 'CANCELLING') NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX restaurant_approval_outbox_saga_status
    ON `order`.restaurant_approval_outbox(type, outbox_status, saga_status);

DROP TABLE IF EXISTS `order`.customers;

CREATE TABLE `order`.customers
(
    id CHAR(36) NOT NULL,
    username VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
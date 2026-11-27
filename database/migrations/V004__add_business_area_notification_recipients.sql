CREATE TABLE IF NOT EXISTS business_area_notification_recipients (
    business_area_id BIGINT NOT NULL,
    email VARCHAR(254) NOT NULL,

    CONSTRAINT uk_business_area_recipient_email
        UNIQUE (business_area_id, email),

    CONSTRAINT fk_business_area_notification_recipients
        FOREIGN KEY (business_area_id)
        REFERENCES business_areas (id)
        ON DELETE CASCADE
);

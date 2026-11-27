CREATE TABLE IF NOT EXISTS home_configurations (
    user_id BIGINT PRIMARY KEY,
    home_type VARCHAR(50) NOT NULL DEFAULT 'House',
    occupants INTEGER NOT NULL DEFAULT 1,
    location VARCHAR(255) NOT NULL DEFAULT '',
    tariff VARCHAR(100) NOT NULL DEFAULT '',

    CONSTRAINT ck_home_configuration_occupants
        CHECK (occupants >= 1),

    CONSTRAINT ck_home_configuration_type
        CHECK (home_type IN (
            'House',
            'Apartment',
            'Office/Premises'
        ))
);

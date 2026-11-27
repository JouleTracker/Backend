-- US-32: Associate IoT sensors with business areas.

ALTER TABLE sensors
    ADD COLUMN IF NOT EXISTS business_area_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_sensors_business_area_id
    ON sensors (business_area_id);

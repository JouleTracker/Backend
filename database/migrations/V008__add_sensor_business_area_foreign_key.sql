UPDATE sensors
SET business_area_id = NULL
WHERE business_area_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM business_areas
    WHERE business_areas.id = sensors.business_area_id
    AND business_areas.user_id = sensors.user_id
);

ALTER TABLE sensors
ADD CONSTRAINT fk_sensor_business_area
FOREIGN KEY (business_area_id)
REFERENCES business_areas (id)
ON DELETE SET NULL;

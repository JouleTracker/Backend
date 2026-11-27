CREATE TABLE IF NOT EXISTS monthly_consumption_thresholds (
    user_id BIGINT PRIMARY KEY,
    monthly_limit_kwh NUMERIC(12, 3) NOT NULL,

    CONSTRAINT ck_monthly_consumption_threshold_positive
        CHECK (monthly_limit_kwh > 0)
);


package com.jouletracker.platform.devices.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SensorBusinessAreaMigrationTest {

    private JdbcTemplate jdbcTemplate;

    private DriverManagerDataSource dataSource;

    @BeforeEach
    void setUp() {

        dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.h2.Driver");

        dataSource.setUrl(
                "jdbc:h2:mem:" + UUID.randomUUID()
                        + ";DB_CLOSE_DELAY=-1"
                        + ";MODE=PostgreSQL"
                        + ";DATABASE_TO_LOWER=TRUE"
        );

        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("""
                CREATE TABLE business_areas (
                    id BIGINT PRIMARY KEY,
                    user_id BIGINT NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE sensors (
                    id BIGINT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    business_area_id BIGINT
                )
                """);

        jdbcTemplate.update("""
                INSERT INTO business_areas (id, user_id)
                VALUES (10, 1)
                """);
    }

    @Test
    void shouldClearInvalidExistingAssociations() {

        insertSensor(100L, 1L, 10L);
        insertSensor(101L, 1L, 99L);
        insertSensor(102L, 2L, 10L);

        executeMigration();

        assertEquals(10L, findBusinessAreaId(100L));
        assertNull(findBusinessAreaId(101L));
        assertNull(findBusinessAreaId(102L));

        assertEquals(3, countSensors());
    }

    @Test
    void shouldPreserveSensorsWhenBusinessAreaIsDeleted() {

        insertSensor(100L, 1L, 10L);
        insertSensor(101L, 1L, 10L);

        executeMigration();

        jdbcTemplate.update(
                "DELETE FROM business_areas WHERE id = ?",
                10L
        );

        assertNull(findBusinessAreaId(100L));
        assertNull(findBusinessAreaId(101L));

        assertEquals(2, countSensors());
    }

    @Test
    void shouldRejectAssociationWithNonexistentArea() {

        executeMigration();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertSensor(100L, 1L, 99L)
        );

        assertEquals(0, countSensors());
    }

    @Test
    void shouldAllowSensorsWithoutBusinessArea() {

        executeMigration();

        insertSensor(100L, 1L, null);

        assertNull(findBusinessAreaId(100L));

        assertEquals(1, countSensors());
    }

    private void executeMigration() {

        var populator = new ResourceDatabasePopulator(
                new ClassPathResource(
                        "db/migration/V008__add_sensor_business_area_foreign_key.sql"
                )
        );

        populator.execute(dataSource);
    }

    private void insertSensor(
            Long sensorId,
            Long userId,
            Long businessAreaId) {

        jdbcTemplate.update("""
                INSERT INTO sensors (
                    id,
                    user_id,
                    business_area_id
                )
                VALUES (?, ?, ?)
                """,
                sensorId,
                userId,
                businessAreaId
        );
    }

    private Long findBusinessAreaId(Long sensorId) {

        return jdbcTemplate.queryForObject(
                """
                SELECT business_area_id
                FROM sensors
                WHERE id = ?
                """,
                Long.class,
                sensorId
        );
    }

    private Integer countSensors() {

        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sensors",
                Integer.class
        );
    }
}

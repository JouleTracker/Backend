package com.jouletracker.platform.shared.infrastructure.seeding;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;
import com.jouletracker.platform.devices.domain.repositories.ApplianceProfileRepository;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Seeds verified demo users (Homeowner, MYPE Business, Admin) and realistic sample telemetry
 * for local development when the database has no user/device data yet.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final ApplianceProfileRepository applianceProfileRepository;
    private final BusinessAreaRepository businessAreaRepository;
    private final EnergyMeasurementRepository energyMeasurementRepository;
    private final AlertRepository alertRepository;

    public DemoDataSeeder(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService,
            DeviceRepository deviceRepository,
            SensorRepository sensorRepository,
            ApplianceProfileRepository applianceProfileRepository,
            BusinessAreaRepository businessAreaRepository,
            EnergyMeasurementRepository energyMeasurementRepository,
            AlertRepository alertRepository) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.applianceProfileRepository = applianceProfileRepository;
        this.businessAreaRepository = businessAreaRepository;
        this.energyMeasurementRepository = energyMeasurementRepository;
        this.alertRepository = alertRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Transactional
    public void seedDemoData() {
        User homeowner = ensureUser(
                "juan@gmail.com",
                "Juan20",
                "Juan",
                "Pérez",
                Set.of(Role.ROLE_USER)
        );
        User business = ensureUser(
                "mype@jouletracker.com",
                "Mype2026",
                "Cafetería",
                "Andina",
                Set.of(Role.ROLE_BUSINESS)
        );
        ensureUser(
                "admin@jouletracker.com",
                "Admin2026",
                "Admin",
                "JouleTracker",
                Set.of(Role.ROLE_ADMIN)
        );

        Long homeUserId = homeowner.getId().value();
        Long businessUserId = business.getId().value();

        if (homeUserId != null && deviceRepository.findAllByUserId(homeUserId).isEmpty()) {
            seedHomeownerSampleData(homeUserId);
        }

        if (businessUserId != null
                && businessAreaRepository.findAllByUserId(businessUserId).isEmpty()
                && deviceRepository.findAllByUserId(businessUserId).isEmpty()) {
            seedBusinessSampleData(businessUserId);
        }
    }

    private User ensureUser(String email, String rawPassword, String firstName, String lastName, Set<Role> roles) {
        EmailAddress address = EmailAddress.of(email);
        return userRepository.findByEmail(address).orElseGet(() -> {
            User newUser = new User(
                    UserId.empty(),
                    address,
                    passwordHashingService.hash(rawPassword),
                    firstName,
                    lastName,
                    UserStatus.ACTIVE,
                    true,
                    roles
            );
            return userRepository.save(newUser);
        });
    }

    private void seedHomeownerSampleData(Long userId) {
        List<ApplianceProfile> profiles = applianceProfileRepository.findAll();

        Device fridge = deviceRepository.save(new Device(
                null, userId, "Refrigeradora Inverter", "Cocina", "Electrodomésticos",
                DeviceStatus.ONLINE, 0.18, 0.35, "2026-12-15", 2.15, "Hoy, 17:15"
        ));
        Device ac = deviceRepository.save(new Device(
                null, userId, "Aire Acondicionado Sala", "Sala", "Climatización",
                DeviceStatus.ONLINE, 1.35, 1.20, "2026-11-30", 5.40, "En línea"
        ));
        Device waterHeater = deviceRepository.save(new Device(
                null, userId, "Terma Eléctrica", "Baño", "Calefacción",
                DeviceStatus.ONLINE, 1.45, 1.60, "2027-01-20", 3.85, "Hace 10 min"
        ));
        Device tv = deviceRepository.save(new Device(
                null, userId, "Televisor Smart TV 55\"", "Dormitorio", "Entretenimiento",
                DeviceStatus.STANDBY, 0.03, 0.25, "2027-03-01", 0.55, "En espera"
        ));

        Sensor fridgeSensor = createSensorIfAbsent(
                userId, "Sensor Cocina", "JoulePlug-Pro", "JT-HOME-001",
                0.18, 2.15, fridge, matchProfile(profiles, "Refrigeradora"), 1.80
        );
        Sensor acSensor = createSensorIfAbsent(
                userId, "Sensor Sala A/C", "JouleClamp-20A", "JT-HOME-002",
                1.35, 5.40, ac, matchProfile(profiles, "Aire Acondicionado"), 4.50
        );
        Sensor heaterSensor = createSensorIfAbsent(
                userId, "Sensor Baño Terma", "JouleRelay-16A", "JT-HOME-003",
                1.45, 3.85, waterHeater, matchProfile(profiles, "Terma"), 3.20
        );
        createSensorIfAbsent(
                userId, "Sensor Dormitorio TV", "JoulePlug-Mini", "JT-HOME-004",
                0.03, 0.55, tv, matchProfile(profiles, "Televisor"), 0.70
        );

        seedMeasurementsForDevices(userId, List.of(
                new DeviceSeedInfo(fridge, fridgeSensor, 0.18, 0.52),
                new DeviceSeedInfo(ac, acSensor, 1.35, 1.45),
                new DeviceSeedInfo(waterHeater, heaterSensor, 1.45, 0.98),
                new DeviceSeedInfo(tv, null, 0.03, 0.18)
        ));

        if (alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            alertRepository.save(new Alert(
                    null, userId, acSensor != null ? acSensor.getId() : null, ac.getId(),
                    "Límite diario superado: Sensor Sala A/C",
                    "Aire Acondicionado Sala acumuló 5.40 kWh hoy (límite recomendado: 4.50 kWh/día).",
                    AlertSeverity.WARNING, "/dispositivos", false, AlertStatus.ACTIVE, now.minusMinutes(25)
            ));
            alertRepository.save(new Alert(
                    null, userId, acSensor != null ? acSensor.getId() : null, ac.getId(),
                    "Potencia instantánea elevada",
                    "Aire Acondicionado Sala registra 1.35 kW, superando su umbral configurado de 1.20 kW.",
                    AlertSeverity.CRITICAL, "/dispositivos", false, AlertStatus.ACTIVE, now.minusMinutes(8)
            ));
            alertRepository.save(new Alert(
                    null, userId, fridgeSensor != null ? fridgeSensor.getId() : null, fridge.getId(),
                    "Resumen semanal disponible",
                    "Tu hogar redujo el consumo base nocturno en 6.4% respecto a la semana pasada.",
                    AlertSeverity.INFO, "/reportes", true, AlertStatus.READ, now.minusDays(1)
            ));
        }
    }

    private void seedBusinessSampleData(Long userId) {
        List<ApplianceProfile> profiles = applianceProfileRepository.findAll();

        businessAreaRepository.save(new BusinessArea(
                null, userId, "Cocina Industrial", "Piso 1",
                "Equipos de refrigeración comercial, hornos y preparación.", "kitchen"
        ));
        businessAreaRepository.save(new BusinessArea(
                null, userId, "Salón Principal", "Piso 1",
                "Iluminación de atención al cliente, climatización y vitrinas.", "storefront"
        ));
        businessAreaRepository.save(new BusinessArea(
                null, userId, "Caja y Oficina", "Piso 2",
                "Punto de venta POS, servidor local y equipos administrativos.", "point_of_sale"
        ));

        Device freezer = deviceRepository.save(new Device(
                null, userId, "Congeladora Comercial", "Cocina Industrial", "Refrigeración",
                DeviceStatus.ONLINE, 0.62, 0.80, "2026-12-20", 8.90, "En línea"
        ));
        Device displayCooler = deviceRepository.save(new Device(
                null, userId, "Vitrina Exhibidora", "Salón Principal", "Refrigeración",
                DeviceStatus.ONLINE, 0.48, 0.60, "2027-01-10", 6.40, "En línea"
        ));
        Device shopAc = deviceRepository.save(new Device(
                null, userId, "Aire Acondicionado Local", "Salón Principal", "Climatización",
                DeviceStatus.ONLINE, 1.85, 1.60, "2026-11-28", 11.20, "En línea"
        ));
        Device posTerminal = deviceRepository.save(new Device(
                null, userId, "Terminal POS y Servidor", "Caja y Oficina", "Cómputo",
                DeviceStatus.ONLINE, 0.24, 0.50, "2027-02-15", 2.85, "Hoy, 17:10"
        ));

        Sensor freezerSensor = createSensorIfAbsent(
                userId, "Sensor Cocina Industrial", "JouleClamp-30A", "JT-MYPE-001",
                0.62, 8.90, freezer, matchProfile(profiles, "Refrigeradora"), 8.50
        );
        Sensor coolerSensor = createSensorIfAbsent(
                userId, "Sensor Vitrina Salón", "JoulePlug-Pro", "JT-MYPE-002",
                0.48, 6.40, displayCooler, matchProfile(profiles, "Refrigeradora"), 6.50
        );
        Sensor acSensor = createSensorIfAbsent(
                userId, "Sensor Climatización Salón", "JouleClamp-30A", "JT-MYPE-003",
                1.85, 11.20, shopAc, matchProfile(profiles, "Aire Acondicionado"), 9.00
        );
        Sensor posSensor = createSensorIfAbsent(
                userId, "Sensor Caja POS", "JoulePlug-Pro", "JT-MYPE-004",
                0.24, 2.85, posTerminal, matchProfile(profiles, "Computadora"), 3.20
        );

        seedMeasurementsForDevices(userId, List.of(
                new DeviceSeedInfo(freezer, freezerSensor, 0.62, 2.20),
                new DeviceSeedInfo(displayCooler, coolerSensor, 0.48, 1.60),
                new DeviceSeedInfo(shopAc, acSensor, 1.85, 2.80),
                new DeviceSeedInfo(posTerminal, posSensor, 0.24, 0.72)
        ));

        if (alertRepository.findAllByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            alertRepository.save(new Alert(
                    null, userId, acSensor != null ? acSensor.getId() : null, shopAc.getId(),
                    "Consumo elevado en Salón Principal",
                    "Aire Acondicionado Local registra 11.20 kWh hoy (umbral sugerido: 9.00 kWh/día).",
                    AlertSeverity.CRITICAL, "/areas", false, AlertStatus.ACTIVE, now.minusMinutes(18)
            ));
            alertRepository.save(new Alert(
                    null, userId, freezerSensor != null ? freezerSensor.getId() : null, freezer.getId(),
                    "Mantenimiento preventivo próximo",
                    "Revisión recomendada de empaques de Congeladora Comercial programada este mes.",
                    AlertSeverity.WARNING, "/dispositivos", false, AlertStatus.ACTIVE, now.minusHours(3)
            ));
        }
    }

    private Sensor createSensorIfAbsent(
            Long userId,
            String name,
            String model,
            String serial,
            Double powerKw,
            Double todayKwh,
            Device device,
            ApplianceProfile profile,
            Double recommendedDailyKwh) {
        SerialNumber sn = new SerialNumber(serial);
        if (sensorRepository.existsBySerialNumber(sn)) {
            return sensorRepository.findBySerialNumber(sn).orElse(null);
        }
        Sensor sensor = new Sensor(
                null,
                userId,
                name,
                model,
                sn,
                SensorNetworkStatus.ONLINE,
                powerKw,
                todayKwh,
                device.getId(),
                device.getName(),
                device.getCategory(),
                profile != null ? profile.getId() : null,
                profile != null ? profile.getName() : null,
                recommendedDailyKwh,
                "Hace 1 min"
        );
        return sensorRepository.save(sensor);
    }

    private void seedMeasurementsForDevices(Long userId, List<DeviceSeedInfo> devices) {
        LocalDateTime now = LocalDateTime.now();

        for (DeviceSeedInfo info : devices) {
            Long sensorId = info.sensor != null ? info.sensor.getId() : null;

            // Today hourly intervals (every 3 hours up to current time)
            for (int hoursAgo = 9; hoursAgo >= 0; hoursAgo -= 3) {
                LocalDateTime ts = now.minusHours(hoursAgo).withMinute(0).withSecond(0).withNano(0);
                double factor = 0.85 + (hoursAgo % 4) * 0.08;
                saveMeasurement(userId, sensorId, info.device, info.basePowerKw * factor, info.slotKwh * factor, ts);
            }

            // Past 6 days (weekly & monthly windows)
            for (int daysAgo = 1; daysAgo <= 6; daysAgo++) {
                LocalDateTime ts = now.minusDays(daysAgo).withHour(14).withMinute(0).withSecond(0).withNano(0);
                double factor = 0.90 + (daysAgo % 3) * 0.12;
                saveMeasurement(userId, sensorId, info.device, info.basePowerKw * factor, info.slotKwh * 3.8 * factor, ts);
            }

            // Past 3 weeks (monthly comparative window)
            for (int daysAgo = 10; daysAgo <= 28; daysAgo += 6) {
                LocalDateTime ts = now.minusDays(daysAgo).withHour(12).withMinute(0).withSecond(0).withNano(0);
                double factor = 1.05 + (daysAgo % 2) * 0.10;
                saveMeasurement(userId, sensorId, info.device, info.basePowerKw * factor, info.slotKwh * 4.2 * factor, ts);
            }

            // Past 4 months (yearly comparative window)
            for (int monthsAgo = 1; monthsAgo <= 4; monthsAgo++) {
                LocalDateTime ts = now.minusMonths(monthsAgo).withDayOfMonth(15).withHour(12).withMinute(0).withSecond(0).withNano(0);
                double factor = 0.95 + monthsAgo * 0.05;
                saveMeasurement(userId, sensorId, info.device, info.basePowerKw * factor, info.slotKwh * 12.0 * factor, ts);
            }
        }
    }

    private void saveMeasurement(
            Long userId,
            Long sensorId,
            Device device,
            double powerKw,
            double energyKwh,
            LocalDateTime measuredAt) {
        double roundedPower = Math.round(powerKw * 100.0) / 100.0;
        double roundedKwh = Math.round(energyKwh * 100.0) / 100.0;
        EnergyMeasurement measurement = new EnergyMeasurement(
                null,
                userId,
                sensorId,
                device.getId(),
                device.getName(),
                device.getCategory(),
                roundedPower,
                roundedKwh,
                220.0,
                null,
                null,
                measuredAt
        );
        energyMeasurementRepository.save(measurement);
    }

    private ApplianceProfile matchProfile(List<ApplianceProfile> profiles, String keyword) {
        String needle = keyword.toLowerCase();
        return profiles.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(needle))
                .findFirst()
                .orElse(profiles.isEmpty() ? null : profiles.get(0));
    }

    private record DeviceSeedInfo(Device device, Sensor sensor, double basePowerKw, double slotKwh) {}
}

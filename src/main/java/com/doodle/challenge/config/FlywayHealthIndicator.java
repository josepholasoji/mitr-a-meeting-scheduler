package com.metr.challenge.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// flyway.info() opens a JDBC connection under the hood - failures are caught and turned into Health.down(ex)
// here, since an uncaught exception from one indicator fails the entire aggregated /actuator/health response
@Component
public class FlywayHealthIndicator implements HealthIndicator {

    private final Flyway flyway;

    public FlywayHealthIndicator(Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public Health health() {
        try {
            MigrationInfoService infoService = flyway.info();
            MigrationInfo current = infoService.current();
            long failedCount = Arrays.stream(infoService.all())
                    .filter(info -> info.getState().isFailed())
                    .count();

            Health.Builder builder = failedCount > 0 ? Health.down() : Health.up();
            return builder.withDetail("currentVersion", current != null ? current.getVersion().toString() : "none")
                    .withDetail("pendingMigrations", infoService.pending().length)
                    .withDetail("failedMigrations", failedCount)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}

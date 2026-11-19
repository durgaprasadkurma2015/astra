
package com.astra.backend.inventory;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservationScheduler {

    private final JdbcTemplate jdbc;
    private final InventoryManagementService inventory;

    public InventoryReservationScheduler(
            JdbcTemplate jdbc,
            InventoryManagementService inventory) {
        this.jdbc = jdbc;
        this.inventory = inventory;
    }

    @Scheduled(fixedDelay = 60000)
    public void expireReservations() {

        List<String> expiredIds = jdbc.queryForList(
            """
            SELECT id
            FROM inventory_reservations
            WHERE status = 'ACTIVE'
              AND expires_at <= UTC_TIMESTAMP()
            ORDER BY expires_at
            LIMIT 100
            """,
            String.class
        );

        for (String id : expiredIds) {
            try {
                inventory.release(id, true);
            } catch (Exception ex) {
                // One failed reservation must not stop the batch.
                // The next scheduler execution retries it.
            }
        }
    }
}

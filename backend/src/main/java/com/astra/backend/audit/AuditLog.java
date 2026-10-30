
package com.astra.backend.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 20)
    private String outcome;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {}

    public AuditLog(
            String userEmail,
            String action,
            String outcome,
            String details,
            String ipAddress
    ) {
        this.userEmail = userEmail;
        this.action = action;
        this.outcome = outcome;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }
}

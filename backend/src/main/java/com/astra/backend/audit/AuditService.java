
package com.astra.backend.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String email,
            String action,
            String outcome,
            String details,
            String ipAddress
    ) {
        AuditLog entry = new AuditLog(
                email,
                action,
                outcome,
                details,
                ipAddress
        );

        repository.save(entry);
    }
}

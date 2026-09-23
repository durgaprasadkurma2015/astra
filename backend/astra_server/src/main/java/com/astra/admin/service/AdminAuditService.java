package com.astra.admin.service;

import org.springframework.stereotype.Service;

@Service
public class AdminAuditService {

    public Object getAuditLogs(
            Long userId,
            String action,
            int page,
            int size) {

        // TODO: Implement audit log retrieval
        return null;
    }

    public Object getAuditLog(Long id) {

        // TODO: Implement audit log retrieval by ID
        return null;
    }
}

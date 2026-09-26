package com.astra.audit.repository;
import com.astra.audit.entity.AuditLog; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{ Page<AuditLog> findByUserId(Long userId,Pageable p); Page<AuditLog> findByActionContainingIgnoreCase(String action,Pageable p); Page<AuditLog> findByUserIdAndActionContainingIgnoreCase(Long userId,String action,Pageable p); }

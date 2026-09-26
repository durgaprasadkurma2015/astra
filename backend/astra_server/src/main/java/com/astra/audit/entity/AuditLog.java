package com.astra.audit.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="audit_logs",indexes={@Index(name="idx_audit_user",columnList="user_id"),@Index(name="idx_audit_created",columnList="created_at")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(name="user_id") Long userId; String username; @Column(nullable=false,length=100) String action; @Column(length=10) String httpMethod; @Column(length=500) String endpoint; @Column(length=20) String outcome; Long durationMs; @Column(length=2000) String details; @Column(nullable=false,updatable=false) LocalDateTime createdAt; @PrePersist void create(){if(createdAt==null)createdAt=LocalDateTime.now();} }

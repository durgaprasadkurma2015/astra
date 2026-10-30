package com.astra.backend.user;

import com.astra.backend.audit.AuditService;
import com.astra.backend.auth.RefreshTokenRepository;
import com.astra.backend.user.dto.AdminCustomerResponse;
import com.astra.backend.user.dto.UpdateCustomerStatusRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminCustomerService {
    private final AppUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final AuditService audit;

    public AdminCustomerService(AppUserRepository users,
            RefreshTokenRepository refreshTokens, AuditService audit) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<AdminCustomerResponse> search(String keyword, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Page must be non-negative and size must be between 1 and 100");
        }
        String cleaned = keyword == null || keyword.isBlank()
                ? null : keyword.trim();
        return users.searchCustomers(cleaned,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(AdminCustomerResponse::from);
    }

    @Transactional
    public AdminCustomerResponse updateStatus(Long customerId,
            UpdateCustomerStatusRequest request, String adminEmail,
            HttpServletRequest httpRequest) {
        AppUser customer = users.findById(customerId)
                .filter(user -> user.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));
        if (request == null || request.active() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Active status is required");
        }
        customer.setActive(request.active());
        if (!request.active()) {
            refreshTokens.revokeAllForUser(customer.getEmail(),
                    LocalDateTime.now(ZoneOffset.UTC));
        }
        AppUser saved = users.save(customer);
        audit.log(adminEmail, "CUSTOMER_STATUS_UPDATE", "SUCCESS",
                "customerId=" + customerId + ", active=" + request.active(),
                httpRequest.getRemoteAddr());
        return AdminCustomerResponse.from(saved);
    }
}

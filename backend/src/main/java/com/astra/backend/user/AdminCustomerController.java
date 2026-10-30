package com.astra.backend.user;

import com.astra.backend.user.dto.AdminCustomerResponse;
import com.astra.backend.user.dto.UpdateCustomerStatusRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {
    private final AdminCustomerService service;

    public AdminCustomerController(AdminCustomerService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AdminCustomerResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(keyword, page, size);
    }

    @PutMapping("/{id}/active")
    public AdminCustomerResponse updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateCustomerStatusRequest request,
            Authentication authentication, HttpServletRequest httpRequest) {
        return service.updateStatus(id, request, authentication.getName(), httpRequest);
    }
}

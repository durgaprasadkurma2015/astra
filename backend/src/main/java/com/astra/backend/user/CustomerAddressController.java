package com.astra.backend.user;

import com.astra.backend.user.dto.CustomerAddressRequest;
import com.astra.backend.user.dto.CustomerAddressResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/addresses")
public class CustomerAddressController {

    private final CustomerAddressService service;

    public CustomerAddressController(CustomerAddressService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerAddressResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerAddressResponse create(@Valid @RequestBody CustomerAddressRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CustomerAddressResponse update(@PathVariable Long id,
            @Valid @RequestBody CustomerAddressRequest request) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/default")
    public CustomerAddressResponse makeDefault(@PathVariable Long id) {
        return service.makeDefault(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

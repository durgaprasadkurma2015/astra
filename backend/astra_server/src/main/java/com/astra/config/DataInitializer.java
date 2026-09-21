package com.astra.config;

import com.astra.entity.Role;
import com.astra.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedRoles(RoleRepository roles) {
        return args -> {
            for (Role.RoleName name : Role.RoleName.values()) {
                roles.findByName(name).orElseGet(() -> roles.save(
                        Role.builder().name(name).build()));
            }
        };
    }
}

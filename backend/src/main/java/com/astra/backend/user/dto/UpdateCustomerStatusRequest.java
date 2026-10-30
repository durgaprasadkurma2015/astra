package com.astra.backend.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(@NotNull Boolean active) { }

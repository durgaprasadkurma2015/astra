package com.astra.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductImageReorderRequest(

        @NotNull
        List<Long> imageIds

) {
}
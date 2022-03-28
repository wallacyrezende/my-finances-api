package com.dev.minhasfinancas.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAuthenticated {
    private Long id;
    private String name;
    private String email;
}
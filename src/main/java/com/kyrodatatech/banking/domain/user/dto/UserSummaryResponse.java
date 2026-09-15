package com.kyrodatatech.banking.domain.user.dto;

import com.kyrodatatech.banking.domain.user.entity.User;

import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        List<String> roles,
        String status) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getRoleType().name())
                        .sorted()
                        .toList(),
                user.getStatus().name()
        );
    }
}

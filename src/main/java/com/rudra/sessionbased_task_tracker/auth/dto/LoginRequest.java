package com.rudra.sessionbased_task_tracker.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String password;
}

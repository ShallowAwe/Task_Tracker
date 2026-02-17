package com.rudra.sessionbased_task_tracker.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegisterUser {

    private String email;
    private String name;
    private String password;
}

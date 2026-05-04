package com.rudra.sessionbased_task_tracker.Home.Controller;

import com.rudra.sessionbased_task_tracker.Home.dto.HomeResponse;
import com.rudra.sessionbased_task_tracker.Home.Service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<HomeResponse> getHome(@AuthenticationPrincipal Long userId) {
        HomeResponse response = homeService.getHome(userId);

        return ResponseEntity.ok(response);
    }
}

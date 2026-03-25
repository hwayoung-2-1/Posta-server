package com.hwayoung.hwayoungserver.auth.controller;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.dto.LogoutRequest;
import com.hwayoung.hwayoungserver.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        loginService.logout(request.getToken());
        return ResponseEntity.noContent().build();
    }
}

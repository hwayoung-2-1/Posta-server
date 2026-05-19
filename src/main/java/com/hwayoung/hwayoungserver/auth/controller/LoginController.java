package com.hwayoung.hwayoungserver.auth.controller;

import com.hwayoung.hwayoungserver.auth.dto.LoginRequest;
import com.hwayoung.hwayoungserver.auth.dto.LoginResponse;
import com.hwayoung.hwayoungserver.auth.service.LoginService;
import com.hwayoung.hwayoungserver.auth.service.SignupService;
import com.hwayoung.hwayoungserver.user.dto.SignupRequest;
import com.hwayoung.hwayoungserver.user.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/auth", "/api/v1/auth"})
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    private final SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signupService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }
}

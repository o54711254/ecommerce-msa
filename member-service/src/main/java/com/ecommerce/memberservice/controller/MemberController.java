package com.ecommerce.memberservice.controller;

import com.ecommerce.memberservice.dto.req.JoinMemberRequest;
import com.ecommerce.memberservice.dto.req.LoginRequest;
import com.ecommerce.memberservice.dto.res.LoginResponse;
import com.ecommerce.memberservice.dto.res.MemberProfileResponse;
import com.ecommerce.memberservice.dto.res.MemberResponse;
import com.ecommerce.memberservice.entity.Role;
import com.ecommerce.memberservice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<MemberResponse> join(@RequestBody JoinMemberRequest request) {
        return ResponseEntity.ok(memberService.join(request, Role.MEMBER));
    }

    @PostMapping("/join/seller")
    public ResponseEntity<MemberResponse> joinSeller(@RequestBody JoinMemberRequest request) {
        return ResponseEntity.ok(memberService.join(request, Role.SELLER));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponse> getMember(@RequestHeader("X-Member-Id") Long memberId) {
        return ResponseEntity.ok(memberService.getMemberProfile(memberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberProfile(id));
    }
}

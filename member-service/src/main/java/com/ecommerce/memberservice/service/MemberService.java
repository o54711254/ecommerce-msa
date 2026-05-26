package com.ecommerce.memberservice.service;

import com.ecommerce.memberservice.dto.req.JoinMemberRequest;
import com.ecommerce.memberservice.dto.req.LoginRequest;
import com.ecommerce.memberservice.dto.res.LoginResponse;
import com.ecommerce.memberservice.dto.res.MemberProfileResponse;
import com.ecommerce.memberservice.dto.res.MemberResponse;
import com.ecommerce.memberservice.entity.Member;
import com.ecommerce.memberservice.entity.Role;
import com.ecommerce.memberservice.global.exception.custom.DuplicateEmailException;
import com.ecommerce.memberservice.global.exception.custom.InvalidPasswordException;
import com.ecommerce.memberservice.global.exception.custom.MemberNotFoundException;
import com.ecommerce.memberservice.repository.MemberRepository;
import com.ecommerce.memberservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse join(JoinMemberRequest request, Role role) {
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException();
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = request.toEntity(role, encodedPassword);

        return memberRepository.save(member).toResponse();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(MemberNotFoundException::new);
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidPasswordException();
        }

        String token = jwtUtil.generateToken(member.getId(), member.getRole());
        return new LoginResponse(token);
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        return member.toProfileResponse();
    }
}

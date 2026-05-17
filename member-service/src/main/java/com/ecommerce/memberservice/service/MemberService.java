package com.ecommerce.memberservice.service;

import com.ecommerce.memberservice.dto.req.JoinMemberRequest;
import com.ecommerce.memberservice.dto.req.LoginRequest;
import com.ecommerce.memberservice.dto.res.LoginResponse;
import com.ecommerce.memberservice.dto.res.MemberProfileResponse;
import com.ecommerce.memberservice.dto.res.MemberResponse;
import com.ecommerce.memberservice.entity.Member;
import com.ecommerce.memberservice.entity.Role;
import com.ecommerce.memberservice.repository.MemberRepository;
import com.ecommerce.memberservice.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
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
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = request.toEntity(role, encodedPassword);

        return memberRepository.save(member).toResponse();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(member.getId(), member.getRole());
        return new LoginResponse(token);
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        return member.toProfileResponse();
    }
}

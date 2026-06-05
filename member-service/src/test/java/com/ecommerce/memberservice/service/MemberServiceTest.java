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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks private MemberService memberService;
    @Mock private MemberRepository memberRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @Nested
    @DisplayName("join - 회원가입")
    class JoinTest {

        @Test
        void 일반_회원_가입_성공() {
            JoinMemberRequest request = new JoinMemberRequest("test@test.com", "홍길동", "1234", "서울");
            Member savedMember = new Member("test@test.com", Role.MEMBER, "홍길동", "encoded", "서울");

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.empty());
            given(passwordEncoder.encode("1234")).willReturn("encoded");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            MemberResponse response = memberService.join(request, Role.MEMBER);

            assertThat(response.email()).isEqualTo("test@test.com");
            assertThat(response.role()).isEqualTo(Role.MEMBER);
        }

        @Test
        void 판매자_가입_성공() {
            JoinMemberRequest request = new JoinMemberRequest("seller@test.com", "판매자", "1234", "부산");
            Member savedMember = new Member("seller@test.com", Role.SELLER, "판매자", "encoded", "부산");

            given(memberRepository.findByEmail("seller@test.com")).willReturn(Optional.empty());
            given(passwordEncoder.encode("1234")).willReturn("encoded");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            MemberResponse response = memberService.join(request, Role.SELLER);

            assertThat(response.email()).isEqualTo("seller@test.com");
            assertThat(response.role()).isEqualTo(Role.SELLER);
        }

        @Test
        void 실패_이메일_중복() {
            JoinMemberRequest request = new JoinMemberRequest("test@test.com", "홍길동", "1234", "서울");
            Member existing = new Member("test@test.com", Role.MEMBER, "홍길동", "encoded", "서울");

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(existing));

            assertThatThrownBy(() -> memberService.join(request, Role.MEMBER))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("이미 사용 중인 이메일입니다");
        }
    }

    @Nested
    @DisplayName("login - 로그인")
    class LoginTest {

        @Test
        void 성공() {
            LoginRequest request = new LoginRequest("test@test.com", "1234");
            Member member = new Member("test@test.com", Role.MEMBER, "홍길동", "encoded", "서울");

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("1234", "encoded")).willReturn(true);
            given(jwtUtil.generateToken(any(), any())).willReturn("jwt-token");

            LoginResponse response = memberService.login(request);

            assertThat(response.token()).isEqualTo("jwt-token");
        }

        @Test
        void 실패_이메일_없음() {
            LoginRequest request = new LoginRequest("none@test.com", "1234");

            given(memberRepository.findByEmail("none@test.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.login(request))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessageContaining("회원을 찾을 수 없습니다");
        }

        @Test
        void 실패_비밀번호_불일치() {
            LoginRequest request = new LoginRequest("test@test.com", "wrong");
            Member member = new Member("test@test.com", Role.MEMBER, "홍길동", "encoded", "서울");

            given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
            given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

            assertThatThrownBy(() -> memberService.login(request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("비밀번호가 일치하지 않습니다");
        }
    }

    @Nested
    @DisplayName("getMemberProfile - 프로필 조회")
    class GetMemberProfileTest {

        @Test
        void 성공() {
            Long memberId = 1L;
            Member member = new Member("test@test.com", Role.MEMBER, "홍길동", "encoded", "서울");
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            MemberProfileResponse response = memberService.getMemberProfile(memberId);

            assertThat(response.email()).isEqualTo("test@test.com");
            assertThat(response.role()).isEqualTo(Role.MEMBER);
            assertThat(response.name()).isEqualTo("홍길동");
            assertThat(response.address()).isEqualTo("서울");
        }

        @Test
        void 실패_회원_없음() {
            given(memberRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getMemberProfile(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }
}

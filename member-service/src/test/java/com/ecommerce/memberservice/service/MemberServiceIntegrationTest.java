package com.ecommerce.memberservice.service;

import com.ecommerce.memberservice.AbstractIntegrationTest;
import com.ecommerce.memberservice.dto.req.JoinMemberRequest;
import com.ecommerce.memberservice.dto.req.LoginRequest;
import com.ecommerce.memberservice.dto.res.LoginResponse;
import com.ecommerce.memberservice.dto.res.MemberProfileResponse;
import com.ecommerce.memberservice.dto.res.MemberResponse;
import com.ecommerce.memberservice.entity.Role;
import com.ecommerce.memberservice.global.exception.custom.DuplicateEmailException;
import com.ecommerce.memberservice.global.exception.custom.InvalidPasswordException;
import com.ecommerce.memberservice.global.exception.custom.MemberNotFoundException;
import com.ecommerce.memberservice.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("join - 회원가입")
    class JoinTest {

        @Test
        void 성공_일반회원() {
            JoinMemberRequest request = new JoinMemberRequest("member@test.com", "홍길동", "1234", "서울");

            MemberResponse result = memberService.join(request, Role.MEMBER);

            assertThat(result.email()).isEqualTo("member@test.com");
            assertThat(result.role()).isEqualTo(Role.MEMBER);
            assertThat(memberRepository.findByEmail("member@test.com")).isPresent();
        }

        @Test
        void 성공_판매자() {
            JoinMemberRequest request = new JoinMemberRequest("seller@test.com", "판매자", "1234", "부산");

            MemberResponse result = memberService.join(request, Role.SELLER);

            assertThat(result.email()).isEqualTo("seller@test.com");
            assertThat(result.role()).isEqualTo(Role.SELLER);
        }

        @Test
        void 실패_이메일_중복() {
            JoinMemberRequest request = new JoinMemberRequest("member@test.com", "홍길동", "1234", "서울");
            memberService.join(request, Role.MEMBER);

            assertThatThrownBy(() -> memberService.join(request, Role.MEMBER))
                    .isInstanceOf(DuplicateEmailException.class);
        }
    }

    @Nested
    @DisplayName("login - 로그인")
    class LoginTest {

        @Test
        void 성공() {
            memberService.join(new JoinMemberRequest("member@test.com", "홍길동", "1234", "서울"), Role.MEMBER);

            LoginResponse result = memberService.login(new LoginRequest("member@test.com", "1234"));

            assertThat(result.token()).isNotBlank();
        }

        @Test
        void 실패_이메일_없음() {
            assertThatThrownBy(() -> memberService.login(new LoginRequest("none@test.com", "1234")))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void 실패_비밀번호_불일치() {
            memberService.join(new JoinMemberRequest("member@test.com", "홍길동", "1234", "서울"), Role.MEMBER);

            assertThatThrownBy(() -> memberService.login(new LoginRequest("member@test.com", "wrong")))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }

    @Nested
    @DisplayName("getMemberProfile - 프로필 조회")
    class GetMemberProfileTest {

        @Test
        void 성공() {
            MemberResponse joined = memberService.join(new JoinMemberRequest("member@test.com", "홍길동", "1234", "서울"), Role.MEMBER);

            MemberProfileResponse result = memberService.getMemberProfile(joined.memberId());

            assertThat(result.email()).isEqualTo("member@test.com");
            assertThat(result.name()).isEqualTo("홍길동");
            assertThat(result.role()).isEqualTo(Role.MEMBER);
            assertThat(result.address()).isEqualTo("서울");
        }

        @Test
        void 실패_회원없음() {
            assertThatThrownBy(() -> memberService.getMemberProfile(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }
}

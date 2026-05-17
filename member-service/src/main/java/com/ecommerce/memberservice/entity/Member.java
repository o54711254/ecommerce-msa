package com.ecommerce.memberservice.entity;

import com.ecommerce.memberservice.dto.res.MemberProfileResponse;
import com.ecommerce.memberservice.dto.res.MemberResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String address;

    public Member(String email, Role role, String name, String password, String address) {
        this.email = email;
        this.role = role;
        this.name = name;
        this.password = password;
        this.address = address;
    }

    public MemberResponse toResponse() {
        return new MemberResponse(this.id, this.role, this.email);
    }

    public MemberProfileResponse toProfileResponse() {
        return new MemberProfileResponse(this.email, this.role, this.name, this.address);
    }
}

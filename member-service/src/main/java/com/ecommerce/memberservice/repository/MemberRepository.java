package com.ecommerce.memberservice.repository;

import com.ecommerce.memberservice.entity.Member;
import com.ecommerce.memberservice.repository.custom.MemberRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Optional<Member> findByEmail(String email);
}

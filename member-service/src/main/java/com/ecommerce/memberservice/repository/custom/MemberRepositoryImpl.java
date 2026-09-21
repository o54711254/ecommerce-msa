package com.ecommerce.memberservice.repository.custom;

import com.ecommerce.memberservice.dto.res.MemberInfoResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.ecommerce.memberservice.entity.QMember.member;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<MemberInfoResponse> getMemberProfiles(List<Long> ids) {
        return jpaQueryFactory.select(Projections.constructor(MemberInfoResponse.class,
                        member.id,
                        member.name
                ))
                .from(member)
                .where(member.id.in(ids))
                .fetch();
    }
}

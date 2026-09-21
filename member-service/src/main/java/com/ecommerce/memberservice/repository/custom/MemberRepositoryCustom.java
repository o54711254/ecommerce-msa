package com.ecommerce.memberservice.repository.custom;

import com.ecommerce.memberservice.dto.res.MemberInfoResponse;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberInfoResponse> getMemberProfiles(List<Long> ids);
}

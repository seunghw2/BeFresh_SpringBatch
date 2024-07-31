package org.f17coders.befreshbatch.module.domain.member.service;

import org.f17coders.befreshbatch.module.domain.member.Member;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberSignupReq;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberTokenReq;

public interface MemberService {

    long registerMember(MemberSignupReq memberSignupReq);

    Long registerFcmToken(MemberTokenReq memberTokenReq, Member member);
}

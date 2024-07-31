package org.f17coders.befreshbatch.module.domain.member.dto.request;

public record MemberSignupReq(

    String id,
    String password,
    Long refrigeratorId
) {

}

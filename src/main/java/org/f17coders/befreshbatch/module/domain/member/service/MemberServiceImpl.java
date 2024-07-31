package org.f17coders.befreshbatch.module.domain.member.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.member.Member;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberSignupReq;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberTokenReq;
import org.f17coders.befreshbatch.module.domain.member.repository.MemberRepository;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.memberToken.repository.MemberTokenRepository;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;
import org.f17coders.befreshbatch.module.domain.refrigerator.repository.RefrigeratorRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberTokenRepository memberTokenRepository;
    private final RefrigeratorRepository refrigeratorRepository;

    @Override
    public long registerMember(MemberSignupReq memberSignupReq) {

        Optional<Refrigerator> refrigerator = refrigeratorRepository.findById(
            memberSignupReq.refrigeratorId());

        Member member = Member.createMember(memberSignupReq.password(),
            refrigerator.get());

        memberRepository.save(member);

        return member.getId();
    }

    @Override
    @Transactional
    public Long registerFcmToken(MemberTokenReq memberTokenReq, Member member) {
        String fcmToken = memberTokenReq.fcmToken();
        Optional<MemberToken> token = memberTokenRepository.findByTokenAndMember_Id(fcmToken, member.getId());

        if(token.isEmpty()){
            Member member1 = memberRepository.findById(member.getId()).orElseThrow();
            MemberToken memberToken = MemberToken.createMemberToken(member1, fcmToken);
            memberTokenRepository.save(memberToken);
        }
        return member.getId();
    }
}

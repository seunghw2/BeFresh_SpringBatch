package org.f17coders.befreshbatch.module.domain.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberSignupReq;
import org.f17coders.befreshbatch.module.domain.member.dto.request.MemberTokenReq;
import org.f17coders.befreshbatch.module.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class MemberController {

    private final MemberService memberService;
    @PostMapping("/signup")
    public ResponseEntity<String> registerFood(
        @RequestBody MemberSignupReq memberSignupReq) {

        long id = memberService.registerMember(memberSignupReq);

        return null;
    }

    @PostMapping("/fcmToken")
    public ResponseEntity<Long> registerFcmToken(
            @RequestBody MemberTokenReq memberTokenReq) {

        Long id = memberService.registerFcmToken(memberTokenReq, null); // TODO : 수정 필요

        return null;
    }
}

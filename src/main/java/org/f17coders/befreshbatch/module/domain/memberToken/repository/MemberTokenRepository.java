package org.f17coders.befreshbatch.module.domain.memberToken.repository;

import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTokenRepository extends JpaRepository<MemberToken, Long> {
    Optional<MemberToken> findByTokenAndMember_Id(String token, Long memberId);

}

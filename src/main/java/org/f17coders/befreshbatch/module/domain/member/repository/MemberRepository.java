package org.f17coders.befreshbatch.module.domain.member.repository;

import org.f17coders.befreshbatch.module.domain.member.Member;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

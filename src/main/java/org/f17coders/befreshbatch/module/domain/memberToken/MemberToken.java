package org.f17coders.befreshbatch.module.domain.memberToken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.f17coders.befreshbatch.module.domain.member.Member;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberToken_id", nullable = false, length = 30)
    private Long id;

    @ManyToOne
    @Setter
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 300)
    @Setter
    private String token;

    public static MemberToken createMemberToken(Member member, String token) {
        MemberToken memberToken = new MemberToken();

        memberToken.setMember(member);
        member.getMemberTokenSet().add(memberToken);
        memberToken.setToken(token);

        return memberToken;
    }
}

package org.f17coders.befreshbatch.module.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.f17coders.befreshbatch.module.domain.memberToken.MemberToken;
import org.f17coders.befreshbatch.module.domain.refrigerator.Refrigerator;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false, length = 30)
    private Long id;

    @Column(length = 100)
    @Setter
    private String password;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<MemberToken> memberTokenSet;

    //     Member - Refrigerator 연관 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refrigerator_id")
    private Refrigerator refrigerator;

    public static Member createMember(String password, Refrigerator refrigerator) {

        Member member = new Member();

        member.setPassword(password);
        member.setRefrigerator(refrigerator);
        return member;
    }
}


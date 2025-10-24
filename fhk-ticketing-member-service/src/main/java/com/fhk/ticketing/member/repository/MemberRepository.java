package com.fhk.ticketing.member.repository;

import com.fhk.ticketing.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByNickName(String nickName);
}

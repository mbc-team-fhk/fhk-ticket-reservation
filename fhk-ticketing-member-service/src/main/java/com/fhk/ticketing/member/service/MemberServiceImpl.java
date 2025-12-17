package com.fhk.ticketing.member.service;

import com.fhk.ticketing.member.domain.Member;
import com.fhk.ticketing.member.dto.getMyInfo.GetMyInfoRes;
import com.fhk.ticketing.member.dto.registerMember.RegisterMemberReq;
import com.fhk.ticketing.member.dto.registerMember.RegisterMemberRes;
import com.fhk.ticketing.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	@Override
	public boolean checkMember(Long accountId) {
		return memberRepository.existsById(accountId);
	}

	@Override
	public GetMyInfoRes getMyInfo(Long accountId) {

		var member = memberRepository.findById(accountId)
				.orElseThrow(()-> new UsernameNotFoundException("not found user"));

		return GetMyInfoRes.builder()
				.accountId(accountId)
				.nickName(member.getNickName())
				.memberName(member.getMemberName())
				.memberPhone(member.getMemberPhone())
				.build();
	}

	@Override
	public RegisterMemberRes registerMember(Long accountId, RegisterMemberReq request) {

		var nickName = request.getNickName();
		var memberName = request.getMemberName();
		var memberPhone = request.getMemberPhone().replaceAll("[^0-9]", "");

		if (memberRepository.existsByNickName(nickName)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "exist nickname");
		}

		var newMember = new Member();
		newMember.setId(accountId);
		newMember.setNickName(nickName);
		newMember.setMemberName(memberName);
		newMember.setMemberPhone(memberPhone);

		var member = memberRepository.save(newMember);
		return RegisterMemberRes.builder()
				.nickName(member.getNickName())
				.registeredDate(member.getRegTime().toLocalDate())
				.build();
	}

	@Override
	public void postMember() {

	}

	@Override
	public void findAll() {

	}

	@Override
	public void updateMember() {

	}

	@Override
	public void deleteMember() {

	}
}

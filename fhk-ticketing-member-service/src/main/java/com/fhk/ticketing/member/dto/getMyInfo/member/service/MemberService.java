package com.fhk.ticketing.member.dto.getMyInfo.member.service;

import com.fhk.ticketing.member.dto.getMyInfo.member.dto.getMyInfo.GetMyInfoRes;
import com.fhk.ticketing.member.dto.getMyInfo.member.dto.registerMember.RegisterMemberReq;
import com.fhk.ticketing.member.dto.getMyInfo.member.dto.registerMember.RegisterMemberRes;

public interface MemberService {

	boolean checkMember(Long id);

	GetMyInfoRes getMyInfo(Long accountId);

	void postMember();

	void findAll();

	void updateMember();

	void deleteMember();

	RegisterMemberRes registerMember(Long accountId, RegisterMemberReq request);
}

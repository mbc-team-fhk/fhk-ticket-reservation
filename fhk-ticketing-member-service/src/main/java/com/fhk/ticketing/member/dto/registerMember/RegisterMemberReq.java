package com.fhk.ticketing.member.dto.registerMember;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterMemberReq {
	private String nickName;
	private String memberName;
	private String memberPhone;
}

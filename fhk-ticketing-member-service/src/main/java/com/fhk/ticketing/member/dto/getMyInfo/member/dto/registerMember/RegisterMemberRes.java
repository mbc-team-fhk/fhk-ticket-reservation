package com.fhk.ticketing.member.dto.getMyInfo.member.dto.registerMember;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RegisterMemberRes {
	String nickName;
	LocalDate registeredDate;
}

package com.fhk.ticketing.member.dto.getMyInfo.member.dto.getMyInfo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetMyInfoRes {
	private Long accountId;
	private String nickName;
	private String memberName;
	private String memberPhone;
}

package com.fhk.ticketing.member.controller;

import com.fhk.common.api.ApiResponse;
import com.fhk.security.core.enums.Role;
import com.fhk.security.core.record.FhkUserPrincipal;
import com.fhk.ticketing.member.dto.registerMember.RegisterMemberReq;
import com.fhk.ticketing.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Log4j2
public class MemberController {

	private final MemberService memberService;

	/**
	 * 1. 회원 여부 확인
	 *  - 통합 로그인 서버에서 발급한 토큰, principal 내의 account_id로 member 테이블 컨트롤
	 * HEAD : GET 경량메서드, response body 가 필요없는 경우 활용
	 * ex) health check (/ping) 등
	 *
	 * @param principal
	 * @return
	 */
	@RequestMapping(method = RequestMethod.HEAD)
	public ResponseEntity<Void> checkMember(@AuthenticationPrincipal FhkUserPrincipal principal) {

		// 통합 로그인 서버의 AccountId
		Long accountId = principal.id();

		// Account Role
		com.fhk.security.core.enums.Role accountRole = Role.valueOf(principal.role());

		// Account Token version
		Long ver = principal.version();

		return memberService.checkMember(principal.id()) ? ResponseEntity.ok().build()
				: ResponseEntity.notFound().build();
	}

	/**
	 * 2. 신규 멤버 연동
	 *  - 통합 로그인 서버에서 전달받은 principal account_id로
	 *    member 테이블에 계정 insert
	 * @param request
	 * @param principal
	 * @return
	 */
	@PostMapping
	public ResponseEntity<?> registerMember(
			@RequestBody RegisterMemberReq request,
			@AuthenticationPrincipal FhkUserPrincipal principal) {
		return ApiResponse.created(memberService.registerMember(principal.id(), request));
	}

	/**
	 * 3. 내 정보 상세 조회하기
	 *  - 내 정보를 조회한다.
	 *
	 * @param principal
	 * @return
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(
			@AuthenticationPrincipal FhkUserPrincipal principal) {
		return ApiResponse.ok(memberService.getMyInfo(principal.id()));
	}


}

package com.fhk.ticketing.movie.controller;

import com.fhk.common.api.ApiResponse;
import com.fhk.security.core.record.FhkUserPrincipal;
import com.fhk.ticketing.movie.dto.reqeust.AddMovieRequest;
import com.fhk.ticketing.movie.dto.response.AddMovieResponse;
import com.fhk.ticketing.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 영화 메타데이터 컨트롤
 * 외부 api (Asset Service) 사용
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {

	private final MovieService movieService;

	// ------------------------------------------
	// [영화]
	// 1. 영화 등록
	// - 영화코드 (KOBIS 등 산업 등재 코드)
	// - 영화제목(한글)
	// - 영화제목(영문)
	// - 영화 개봉일 (2025-10-16)
	// - 시청 나이 제한
	// - 러닝타임 (광고시간 제외)
	// - 영화 설명
	// - 배급국가
	// - 감독 (목록)
	// - 출연 (목록)
	// - 포스터 사진 ( asset service )
	// - 트레일러 영상 목록 ( asset service )
	// - 스틸컷 사진 목록 ( asset service )
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> addMovie(@RequestBody AddMovieRequest request,
	                                  @AuthenticationPrincipal FhkUserPrincipal principal) {
		var res = AddMovieResponse.builder().build();
		return ApiResponse.created(res);
	}

	// 2. 영화 목록 조회
	// - pageable 조회
	@GetMapping
	public ResponseEntity<?> getMovieList(
			@RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
			@RequestParam(name = "size", defaultValue = "20", required = false) Integer size,
			@AuthenticationPrincipal FhkUserPrincipal principal) {

		// Request
		// 상영관 ID
		//

		return null;
	}

	// 3. 영화 상세 조회
	@GetMapping("/{movieId}")
	public ResponseEntity<?> getMovieDetail(@PathVariable Long movieId,
	                                  @AuthenticationPrincipal FhkUserPrincipal principal) {
		return null;
	}

	// 4. 영화 정보 수정
	// 티켓팅 토이프로젝트에서는 CMS 기능 제외

	// 5. 영화 정보 삭제
	// 티켓팅 토이프로젝트에서는 CMS 기능 제외



	// ------------------------------------------
	// [장르]
	// 1. 장르 등록
	// - 장르 코드
	// - 장르 이름
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> addGenre(@RequestBody Map<String, ?> request,
	                                  @AuthenticationPrincipal FhkUserPrincipal principal) {
		return null;
	}

	// 2. 장르 목록 조회
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getGenreList(@RequestBody Map<String, ?> request,
	                                  @AuthenticationPrincipal FhkUserPrincipal principal) {
		return null;
	}
}

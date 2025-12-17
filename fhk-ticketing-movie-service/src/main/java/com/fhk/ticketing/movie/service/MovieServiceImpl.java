package com.fhk.ticketing.movie.service;

import com.fhk.ticketing.movie.dto.reqeust.AddMovieRequest;
import com.fhk.ticketing.movie.dto.response.AddMovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

	public AddMovieResponse addMovie(AddMovieRequest request) {

		return AddMovieResponse.builder()
				.a(0)
				.build();
	}
}

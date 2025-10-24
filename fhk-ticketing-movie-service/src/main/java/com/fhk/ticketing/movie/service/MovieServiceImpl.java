package com.fhk.ticketing.movie.service;

import com.fhk.ticketing.movie.dto.reqeust.AddMovieRequest;
import com.fhk.ticketing.movie.dto.response.AddMovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

	private final SomeHttpModule httpModule;

	public AddMovieResponse addMovie(AddMovieRequest request) {

		httpModule.request(Method.POST, "http://ip.org/api/assets",
				~~~~, (res)-> {


				});

		return AddMovieResponse.builder()
				.a(0)
				.build();
	}
}

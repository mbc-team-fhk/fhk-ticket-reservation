package com.fhk.ticketing.movie.repository;

import com.fhk.ticketing.movie.domain.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}

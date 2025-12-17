package com.fhk.ticketing.movie.domain.movie;

import com.fhk.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 장르
 *
 * - id
 * - 장르코드
 * - 장르명
 *
 */
@Entity
@Table(name = "genre_tbl")
@Getter
@Setter
@ToString
public class Genre extends BaseEntity {

	@Id
	@Column(name = "genre_id")
	private Long genreId;

	@Column(name = "genre_code")
	private String genreCode;

	@Column(name = "genre_name")
	private String genreName;

	@ManyToMany(mappedBy = "genres")
	private List<Movie> movies = new ArrayList<>();
}

package com.fhk.ticketing.member.domain;

import com.fhk.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "member_tbl")
@Getter
@Setter
@ToString
public class Member extends BaseEntity {

	@Id
	@SequenceGenerator(
			name = "member_seq",
			sequenceName = "member_seq_tbl",
			allocationSize = 1 // sequence 캐싱 처리, 배포시 50정도 -> 병목시 늘리기
	)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	@Column(name = "member_id")
	private Long id;

	@Column(name = "nick_name")
	private String nickName;

	@Column(name = "member_name")
	private String memberName;

	@Column(name = "member_phone")
	private String memberPhone;
}

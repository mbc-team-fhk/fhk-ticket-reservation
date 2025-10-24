package com.fhk.ticketing.member.dto.getMyInfo.member.domain;

import com.fhk.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
	@Column(name = "member_id")
	private Long id;

	@Column(name = "nick_name")
	private String nickName;

	@Column(name = "member_name")
	private String memberName;

	@Column(name = "member_phone")
	private String memberPhone;
}

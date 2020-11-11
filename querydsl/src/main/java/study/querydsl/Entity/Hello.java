package study.querydsl.Entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


import lombok.Data;

@Entity
@Data
public class Hello {

	@Id @GeneratedValue
	private Long id;
}

package study.dataJPA.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import study.dataJPA.dto.MemberDto;
import study.dataJPA.entity.Member;
import study.dataJPA.entity.Team;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Test
	public void findByUsernameAndAgeGreaterThen() {
		Member m1 = new Member("aaa", 10);
		Member m2 = new Member("aaa", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);
		
		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);
		
		assertThat(result.get(0).getUsername()).isEqualTo("aaa");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}
	
	@Test
	public void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);
		
		Member m1 = new Member("aaa", 10);
		m1.setTeam(team);
		memberRepository.save(m1);
		
		List<MemberDto> memberDto = memberRepository.findMemberDto();
		for(MemberDto dto : memberDto) {
			System.out.println("Dto : "+dto);
		}
	}

	
	@Test
	public void paping() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));
		
		int age = 10;
		PageRequest pageReqeust = PageRequest.of(0, 3);
		
		//when
		//Page<Member> page = memberRepository.findByAge(age, pageReqeust);
		Slice<Member> page = memberRepository.findByAge2(age, pageReqeust);
		
		//then
		List<Member> content = page.getContent();
		
		assertThat(content.size()).isEqualTo(3);
		//assertThat(page.getTotalElements()).isEqualTo(5);
		assertThat(page.getNumber()).isEqualTo(0);
		//assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}
	
	@Test
	public void queryHint() {
		
	}
}

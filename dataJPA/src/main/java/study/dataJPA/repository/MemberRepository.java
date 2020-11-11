package study.dataJPA.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import study.dataJPA.dto.MemberDto;
import study.dataJPA.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
	
	@Query("select new study.dataJPA.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();
	
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") Collection<String> names);
	
	Page<Member> findByAge(int age, Pageable pageable);
	
	//Slice<Member> findByAge2(int age, Pageable pageable);
	
	@Modifying(clearAutomatically = true)//1차 캐쉬 날림 - 영속성 컨텍스트
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);
	
	@Override
	@EntityGraph(attributePaths = {"team"})
	List<Member> findAll();
	
	Member findReadOnlyByUsername(String username);
}

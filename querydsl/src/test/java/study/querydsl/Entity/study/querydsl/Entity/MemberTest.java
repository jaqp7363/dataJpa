package study.querydsl.Entity.study.querydsl.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.Entity.QMember.member;
import static study.querydsl.Entity.QTeam.team;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import study.querydsl.Entity.Member;
import study.querydsl.Entity.QMember;
import study.querydsl.Entity.Team;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;

@SpringBootTest
@Transactional
//@Commit //롤백 안되게 막음
class MemberTest {

	@Autowired
	EntityManager em;
	
	JPAQueryFactory queryFactory;
	
	@BeforeEach
	void test() {
		queryFactory = new JPAQueryFactory(em);
		
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);
		
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
		
		//초기화
		em.flush();
		em.clear();
		
		//확인
		List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
		
		for(Member member : members) {
			System.out.println("member = " + member);
			System.out.println("-> member.team" + member.getTeam());
		}
	}

	
	@Test
	public void startJPQL() {
		Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
		.setParameter("username", "member1")
		.getSingleResult();
	
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@Test
	public void startQuerydsl() {
		//같은 테이블 조인 시 사용
		//QMember m1 = new QMember("m1");
		
		Member findMember = queryFactory
		.select(member)
		.from(member)
		.where(member.username.eq("member1"))
		.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@Test
	public void search() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(
						member.username.eq("member1"),
						member.age.eq(10))
				.fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));
		
		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();
		
		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}
	
	@Test
	public void aggregation() {
		List<Tuple> result = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min()
				)
				.from(member).fetch();
		
		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
	}
	
	@Test
	public void group() throws Exception {
		List<Tuple> result = queryFactory
		.select(team.name, member.age.avg())
		.from(member)
		.join(member.team, team)
		.groupBy(team.name)
		.fetch();
		
		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);
		
		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}
	
	@Test
	public void join() throws Exception {
		List<Member> result = queryFactory
				.selectFrom(member)
				.join(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();
		assertThat(result)
			.extracting("username")
			.containsExactly("member1", "member2");
	}
	
	@Test
	public void join_on_filtering() throws Exception {
		List<Tuple> result = queryFactory
		.select(member, team)
		.from(member)
		.leftJoin(member.team, team).on(team.name.eq("teamA"))
		.fetch();
		
		for(Tuple tuple : result) {
			System.out.println("tuple = "+tuple);
		}
	}
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	@Test
	public void fetchJoin() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1"))
				.fetchOne();
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("패치 조인 미적용").isFalse();
		
		em.flush();
		em.clear();
		
		findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team).fetchJoin()
				.where(member.username.eq("member1"))
				.fetchOne();
		
		loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("패치 조인 적용").isTrue();
	}
	
	@Test
	public void subQuery() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
		.selectFrom(member)
		.where(member.age.eq(
				JPAExpressions.select(memberSub.age.max()).from(memberSub)
				))
		.fetch();
		
		assertThat(result).extracting("age").containsExactly(40);
	}
	
	@Test
	public void subQueryGoe() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
		.selectFrom(member)
		.where(member.age.goe(
				JPAExpressions.select(memberSub.age.avg()).from(memberSub)
				))
		.fetch();
		
		assertThat(result).extracting("age").containsExactly(30, 40);
	}
	
	@Test
	public void subQueryIn() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result = queryFactory
		.selectFrom(member)
		.where(member.age.in(
				JPAExpressions.select(memberSub.age).from(memberSub).where(memberSub.age.gt(10))
				))
		.fetch();
		
		assertThat(result).extracting("age").containsExactly(20, 30, 40);
	}
	
	@Test
	public void complexCase() {
		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20살")
						.when(member.age.between(21, 30)).then("21~30살")
						.otherwise("기타"))
				.from(member)
				.fetch();
		for(String s : result) {
			System.out.println("s = "+s);
		}
	}
	
	@Test
	public void concat() {
		List<String> result = queryFactory
				.select(member.username.concat("_").concat(member.age.stringValue()))
				.from(member)
				.fetch();
		for(String s : result) {
			System.out.println("s = "+s);
		}
	}
	
	@Test
	public void tupleProjection() {
		List<Tuple> result = queryFactory
				.select(member.username, member.age)
				.from(member)
				.fetch();
		
		for(Tuple tuple : result) {
			String username = tuple.get(member.username);
			int age = tuple.get(member.age);
			System.out.println("username = "+username);
			System.out.println("age = "+age);
		}
	}
	
	@Test
	public void projection() {
		List<MemberDto> result = queryFactory
				 .select(Projections.bean(MemberDto.class,
				 member.username,
				 member.age))
				 .from(member)
				 .fetch();
		
		List<UserDto> fetch = queryFactory
				 .select(Projections.fields(UserDto.class,
				 member.username.as("name"),
				 ExpressionUtils.as(
				 JPAExpressions
				 .select(member.age.max())
				 .from(member), "age")
				 )
				 ).from(member)
				 .fetch();
		
		List<MemberDto> resultconstructor = queryFactory
				 .select(Projections.constructor(MemberDto.class,
				 member.username,
				 member.age))
				 .from(member)
				 .fetch();
	}
	
	@Test
	public void findDtoByQueryPOrojection() {
		List<MemberDto> result = queryFactory
		.select(new QMemberDto(member.username, member.age))
		.from(member)
		.fetch();
		
		for(MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}
	
	
}

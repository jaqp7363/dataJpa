package study.dataJPA.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import study.dataJPA.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long>{

}

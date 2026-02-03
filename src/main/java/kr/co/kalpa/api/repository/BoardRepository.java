package kr.co.kalpa.api.repository;

import kr.co.kalpa.api.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByBoardCode(String boardCode);
}

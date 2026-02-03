package kr.co.kalpa.api.dto.response;

import kr.co.kalpa.api.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {

    private Long id;
    private String boardCode;
    private String boardNameKor;
    private String contentType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .boardCode(board.getBoardCode())
                .boardNameKor(board.getBoardNameKor())
                .contentType(board.getContentType())
                .description(board.getDescription())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}

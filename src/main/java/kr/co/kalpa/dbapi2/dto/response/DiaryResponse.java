package kr.co.kalpa.dbapi2.dto.response;

import kr.co.kalpa.dbapi2.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {

    private String ymd;
    private String content;
    private String summary;

    /**
     * Factory method to create from Diary entity
     */
    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .ymd(diary.getYmd())
                .content(diary.getContent())
                .summary(diary.getSummary())
                .build();
    }

    /**
     * Factory method for summary-only response
     */
    public static DiaryResponse summaryOnly(Diary diary) {
        return DiaryResponse.builder()
                .ymd(diary.getYmd())
                .summary(diary.getSummary())
                .build();
    }
}

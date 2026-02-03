package kr.co.kalpa.api.dto.response;

import kr.co.kalpa.api.entity.Jangbi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JangbiResponse {

    private Long id;
    private String ymd;
    private String item;
    private String location;
    private Integer cost;
    private String spec;
    private String lvl;
    private LocalDateTime modifyDt;

    /**
     * Factory method to create from Jangbi entity
     */
    public static JangbiResponse from(Jangbi jangbi) {
        return JangbiResponse.builder()
                .id(jangbi.getId())
                .ymd(jangbi.getYmd())
                .item(jangbi.getItem())
                .location(jangbi.getLocation())
                .cost(jangbi.getCost())
                .spec(jangbi.getSpec())
                .lvl(jangbi.getLvl())
                .modifyDt(jangbi.getModifyDt())
                .build();
    }

    /**
     * Factory method for summary-only response (without spec)
     */
    public static JangbiResponse summaryOnly(Jangbi jangbi) {
        return JangbiResponse.builder()
                .id(jangbi.getId())
                .ymd(jangbi.getYmd())
                .item(jangbi.getItem())
                .location(jangbi.getLocation())
                .cost(jangbi.getCost())
                .lvl(jangbi.getLvl())
                .modifyDt(jangbi.getModifyDt())
                .build();
    }
}

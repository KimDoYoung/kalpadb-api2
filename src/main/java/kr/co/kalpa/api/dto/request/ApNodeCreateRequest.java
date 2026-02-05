package kr.co.kalpa.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApNodeCreateRequest {
    private String nodeType; // 'D' for directory
    private String parentId;
    private String name;
}

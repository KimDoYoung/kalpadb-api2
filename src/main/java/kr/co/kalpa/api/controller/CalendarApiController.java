package kr.co.kalpa.api.controller;

import kr.co.kalpa.api.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarApiController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCalendar(
            @RequestParam("start") String startYmd,
            @RequestParam("end") String endYmd) {
        return ResponseEntity.ok(calendarService.getCalendarEvents(startYmd, endYmd));
    }
}

package kr.co.kalpa.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(value = "style", defaultValue = "1m") String style) {
        if ("3m".equals(style)) {
            return "calendar/calendar_3m";
        }
        return "calendar/calendar_1m";
    }
}

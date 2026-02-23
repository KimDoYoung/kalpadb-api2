package kr.co.kalpa.ui.controller;

import kr.co.kalpa.api.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public String list() {
        return "diary/list";
    }

    @GetMapping("/new")
    public Object create(@RequestParam(value = "ymd", required = false) String ymd) {
        // ymd가 없으면 오늘 날짜로 설정
        if (ymd == null || ymd.isEmpty()) {
            ymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        // 해당 날짜의 일기가 이미 존재하는지 확인
        try {
            diaryService.getDiary(ymd);
            // 존재하면 수정 페이지로 리다이렉트
            return new RedirectView("/kalpadb-api/diary/" + ymd + "/edit");
        } catch (Exception e) {
            // 존재하지 않으면 새 일기 작성 페이지 표시
            return "diary/create";
        }
    }

    @GetMapping("/{ymd}")
    public String view(@PathVariable String ymd) {
        return "diary/view";
    }

    @GetMapping("/{ymd}/edit")
    public String edit(@PathVariable String ymd) {
        return "diary/edit";
    }
}

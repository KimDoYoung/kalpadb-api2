package kr.co.kalpa.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/diary")
public class DiaryController {

    @GetMapping
    public String list() {
        return "diary/list";
    }

    @GetMapping("/new")
    public String newDiary() {
        return "diary/form";
    }

    @GetMapping("/{ymd}")
    public String view(@PathVariable String ymd) {
        return "diary/view";
    }

    @GetMapping("/edit/{ymd}")
    public String edit(@PathVariable String ymd) {
        return "diary/form";
    }
}

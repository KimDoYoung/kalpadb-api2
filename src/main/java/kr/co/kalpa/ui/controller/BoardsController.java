package kr.co.kalpa.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/boards")
public class BoardsController {

    @GetMapping
    public String list() {
        return "boards/list";
    }

    @GetMapping("/{boardId}")
    public String view(@PathVariable Long boardId) {
        return "boards/view";
    }
}

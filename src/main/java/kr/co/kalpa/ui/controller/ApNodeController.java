package kr.co.kalpa.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/apnode")
public class ApNodeController {

    @GetMapping
    public String list() {
        return "apnode/list";
    }
}

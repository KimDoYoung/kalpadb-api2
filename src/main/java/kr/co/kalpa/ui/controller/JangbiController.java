package kr.co.kalpa.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jangbi")
public class JangbiController {

    @GetMapping
    public String list() {
        return "jangbi/list";
    }

    @GetMapping("/new")
    public String create() {
        return "jangbi/create";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id) {
        return "jangbi/view";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id) {
        return "jangbi/edit";
    }
}

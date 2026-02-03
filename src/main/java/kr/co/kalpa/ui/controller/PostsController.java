package kr.co.kalpa.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/posts")
public class PostsController {

    @GetMapping
    public String list(@RequestParam(required = false) Long boardId) {
        return "posts/list";
    }

    @GetMapping("/new")
    public String create(@RequestParam Long boardId) {
        return "posts/create";
    }

    @GetMapping("/{postId}")
    public String view(@PathVariable Long postId) {
        return "posts/view";
    }

    @GetMapping("/{postId}/edit")
    public String edit(@PathVariable Long postId) {
        return "posts/edit";
    }
}

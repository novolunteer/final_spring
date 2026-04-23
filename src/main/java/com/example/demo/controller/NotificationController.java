package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Transactional
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    // 저장
    @PostMapping("/save")
    public String save(Notification n,
                       @RequestParam(name = "important", required = false) Boolean important) {

        n.setImportant(important != null);
        service.save(n);

        return "redirect:/notifications/list";
    }

    // 기본 → 목록
    @GetMapping
    public String main() {
        return "redirect:/notifications/list";
    }

    // 🔥 목록 (페이징 + 검색 + 중요공지)
    @GetMapping("/list")
    public String list(
           @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        model.addAttribute("pinned", service.getImportant());
        model.addAttribute("list", service.getList(pageable));
        return "list";
    }

    // 작성 화면
    @GetMapping("/form")
    public String showForm(Model model) {
        model.addAttribute("n", new Notification());
        return "form";
    }

    // 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/notifications/list";
    }

    // 상세보기 + 조회수 증가
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Notification n = service.findById(id);
        // 조회수 증가
        n.setViewCount(n.getViewCount() + 1);
        model.addAttribute("n", n);

        return "detail";
    }

    // 수정 화면
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("n", service.findById(id));
        return "form";
    }

    // 🔥 수정 (초보용 간단 버전)
    @PostMapping("/update")
    public String update(Notification n,
     @RequestParam(name = "important", required = false) Boolean important) {
     n.setImportant(important != null);
     service.save(n);
     return "redirect:/notifications/" + n.getNotificationId();
    }
}
package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationApiController {
    private final NotificationService service;

    //전체 조회
    @GetMapping
    public Page<Notification> list(Pageable pageable) {
        return service.getList(pageable);
    }

    // 상세조회
    @GetMapping("/{id}")
    public Notification detail(@PathVariable Long id) {
        return service.findById(id);
    }

    //  저장
    @PostMapping
    public Notification save(@RequestBody Notification n) {
        return service.save(n);
    }

    //삭제
    @DeleteMapping("/{id}")
    public  void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody Notification n) {
        service. update(id,n);
    }
}


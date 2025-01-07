package com.pyounani.mailloadmonitor.controller;

import com.pyounani.mailloadmonitor.domain.LoadTest;
import com.pyounani.mailloadmonitor.dto.LoadTestDto;
import com.pyounani.mailloadmonitor.repository.LoadTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class LoadTestResultController {

    private final LoadTestRepository loadTestRepository;

    @GetMapping("/")
    public String resultPage(Model model) {
        model.addAttribute("loadTestList",
                loadTestRepository.findAll().stream().map(LoadTestDto::from)
                        .collect(Collectors.toList()));
        return "load-test";
    }
}

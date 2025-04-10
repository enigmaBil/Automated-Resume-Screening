package com.enigma.automated_resume_screening.web.controllers;

import com.enigma.automated_resume_screening.business.services.MatchingService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    @GetMapping("/candidates/{jobOfferId}")
    public List<Candidate> getMatchingCandidates(@PathVariable Long jobOfferId) throws JsonProcessingException {
        return matchingService.findMatchingCandidates(jobOfferId);
    }
}

package com.enigma.automated_resume_screening.web.controllers;

import com.enigma.automated_resume_screening.business.services.MatchingResultService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.MatchingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matchings")
@RequiredArgsConstructor
public class MatchingResultController {
    private final MatchingResultService matchingResultService;

    @GetMapping("/job-offer/{jobOfferId}")
    public List<MatchingResult> getMatchingResultsByJobOffer(@PathVariable Long jobOfferId) {
        return matchingResultService.getResultsByJobOfferId(jobOfferId);
    }

    @GetMapping("/shortlisted/{jobOfferId}")
    public ResponseEntity<List<Candidate>> getShortlistedCandidates(@PathVariable Long jobOfferId) {
        List<Candidate> shortlisted = matchingResultService.getShortlistedCandidates(jobOfferId);
        return ResponseEntity.ok(shortlisted);
    }

}

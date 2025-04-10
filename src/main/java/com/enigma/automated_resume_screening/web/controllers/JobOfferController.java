package com.enigma.automated_resume_screening.web.controllers;

import com.enigma.automated_resume_screening.business.services.JobOfferService;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin("*")
public class JobOfferController {
    private final JobOfferService jobOfferService;

    // Récupérer toutes les offres d'emploi
    @GetMapping("/job-offers")
    public ResponseEntity<List<JobOffer>> getAllJobOffers() {
        return new ResponseEntity<>(jobOfferService.getLatestJobOffers(), HttpStatus.OK);
    }

    // Créer une nouvelle offre d'emploi
    @PostMapping("/job-offers")
    public ResponseEntity<JobOffer> createJobOffer(@RequestBody JobOffer jobOffer) {
        return ResponseEntity.ok(jobOfferService.createJobOffer(jobOffer));
    }

    // Mettre à jour une offre d'emploi existante
    @PutMapping("/job-offers/{id}")
    public ResponseEntity<JobOffer> updateJobOffer(@PathVariable long id, @RequestBody JobOffer jobOffer) {
        return ResponseEntity.ok(jobOfferService.updateJobOffer(id, jobOffer));
    }

    // Récupérer une offre d'emploi par son ID
    @GetMapping("/job-offers/{id}")
    public ResponseEntity<JobOffer> getJobOffer(@PathVariable long id) {
        return ResponseEntity.ok(jobOfferService.getJobOffer(id));
    }

    // Supprimer une offre d'emploi par son ID
    @DeleteMapping("/job-offers/{id}")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable long id) {
        jobOfferService.deleteJobOffer(id);
        return ResponseEntity.noContent().build();
    }
}

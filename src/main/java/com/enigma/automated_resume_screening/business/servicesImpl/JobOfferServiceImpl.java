package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.JobOfferService;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import com.enigma.automated_resume_screening.dao.repositories.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService {
    private final JobOfferRepository jobOfferRepository;
    @Override
    public List<JobOffer> getAllJobOffers() {
        return jobOfferRepository.findAll();
    }

    @Override
    public List<JobOffer> getLatestJobOffers() {
        return jobOfferRepository.findTop8ByOrderByCompanyAsc();
    }

    @Override
    public JobOffer createJobOffer(JobOffer jobOffer) {
        // Sauvegarder une nouvelle offre d'emploi dans la base de données
        return jobOfferRepository.save(jobOffer);
    }

    @Override
    public JobOffer updateJobOffer(long id, JobOffer updatedJobOffer) {
        // Vérifier si l'offre d'emploi existe
        JobOffer existingJobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée avec l'ID : " + id));

        // Mettre à jour les champs de l'offre existante
        existingJobOffer.setTitle(updatedJobOffer.getTitle());
        existingJobOffer.setDescription(updatedJobOffer.getDescription());
        existingJobOffer.setRequiredSkills(updatedJobOffer.getRequiredSkills());
        existingJobOffer.setExperienceYears(updatedJobOffer.getExperienceYears());
        existingJobOffer.setEducationLevel(updatedJobOffer.getEducationLevel());

        // Sauvegarder les modifications
        return jobOfferRepository.save(existingJobOffer);
    }

    @Override
    public JobOffer getJobOffer(long id) {
        // Récupérer une offre d'emploi par son ID
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée avec l'ID : " + id));
    }

    @Override
    public void deleteJobOffer(long id) {
        // Supprimer une offre d'emploi par son ID
        jobOfferRepository.deleteById(id);
    }
}

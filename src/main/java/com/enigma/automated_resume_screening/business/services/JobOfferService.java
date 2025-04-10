package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.JobOffer;

import java.util.List;

public interface JobOfferService {
    List<JobOffer> getAllJobOffers();
    List<JobOffer> getLatestJobOffers();
    JobOffer createJobOffer(JobOffer jobOffer);
    JobOffer updateJobOffer(long id, JobOffer jobOffer);
    JobOffer getJobOffer(long id);

    void deleteJobOffer(long id);

}

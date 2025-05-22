package com.enigma.automated_resume_screening.business.services;

import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;

public interface NotificationService {
    void sendShortlistNotification(String to, Candidate candidate, JobOffer offer, int score);
}

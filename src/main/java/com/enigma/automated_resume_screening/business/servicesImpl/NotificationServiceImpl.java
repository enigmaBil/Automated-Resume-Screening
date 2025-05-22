package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.NotificationService;
import com.enigma.automated_resume_screening.dao.entities.Candidate;
import com.enigma.automated_resume_screening.dao.entities.JobOffer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendShortlistNotification(String to, Candidate candidate, JobOffer offer, int score) {
        try {
            String subject = "Nouveau candidat shortlisté : " + candidate.getName();
            String body = """
                Bonjour,

                Un nouveau candidat a été shortlisté pour le poste : %s

                Nom : %s
                Email : %s
                Téléphone : %s
                Score de matching : %d

                Le CV du candidat est joint à ce mail.

                Ci-joint le cv du candidat.
                """.formatted(
                    offer.getTitle(),
                    candidate.getName(),
                    candidate.getEmail(),
                    candidate.getPhone(),
                    score
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart pour pièce jointe

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            // Joindre le fichier CV (chemin stocké dans resumeFilePath)
            File cvFile = new File(candidate.getResumeFilePath());
            if (cvFile.exists()) {
                FileSystemResource file = new FileSystemResource(cvFile);
                helper.addAttachment("CV_Candidat.pdf", file);
            } else {
                System.err.println("Le fichier CV est introuvable : " + cvFile.getAbsolutePath());
            }

            mailSender.send(message);
            System.out.println("Mail shortlist envoyé avec CV joint.");

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec pièce jointe", e);
        }
    }
}

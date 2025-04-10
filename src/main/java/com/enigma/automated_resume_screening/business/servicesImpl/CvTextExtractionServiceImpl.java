package com.enigma.automated_resume_screening.business.servicesImpl;

import com.enigma.automated_resume_screening.business.services.CvTextExtractionService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class CvTextExtractionServiceImpl implements CvTextExtractionService {

    @Override
    public String extractText(MultipartFile file) {
        Tika tika = new Tika();
        try(InputStream inputStream = file.getInputStream()){
            return tika.parseToString(inputStream);
        }catch (IOException | TikaException e){
            throw new RuntimeException("Erreur lors de l'extraction du texte du fichier CV",e);
        }
    }
}

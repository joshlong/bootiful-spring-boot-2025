package com.example.service.vet;

import com.example.service.adoptions.DogAdoptionEvent;
import com.example.service.adoptions.validations.Validation;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
class Dogtor {
    
    @ApplicationModuleListener
    void checkup(DogAdoptionEvent dogAdoptionEvent) throws Exception {
        Thread.sleep(5000);
        System.out.println("adopting " + dogAdoptionEvent.dogId() + ".");
    }
}

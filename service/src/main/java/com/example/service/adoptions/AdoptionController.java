package com.example.service.adoptions;

import com.example.service.adoptions.validations.Validation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@Controller
@ResponseBody
@Transactional
@RequestMapping("/dogs/adoptions")
class AdoptionController {

    private final DogRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    AdoptionController(DogRepository repository,
                       Validation validation,
                       ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/{id}")
    void adopt(@PathVariable Integer id, @RequestParam String name) {
        this.repository.findById(id).ifPresent(dog -> {
            var saved = this.repository.save(new Dog(id, dog.name(), name,
                    dog.description()));
            System.out.println("saved [" + saved + "]");
            this.applicationEventPublisher.publishEvent(new DogAdoptionEvent(id));
        });

    }

}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

// look mom, no Lombok!!
record Dog(@Id int id, String name, String owner, String description) {
}

// DATA ORIENTED PROGRAMMING
// - records
// - sealed types
// - pattern matching
// - smart switch expressions


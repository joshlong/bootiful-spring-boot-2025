package com.example.adoptions.adoptions;

import com.example.adoptions.adoptions.grpc.AdoptionsGrpc;
import com.example.adoptions.adoptions.grpc.Dogs;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;





@Service
@Transactional
class DogAdoptionsGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionsGrpcService(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @Override
    public void all(Empty request,
            StreamObserver<com.example.adoptions.adoptions.grpc.Dogs> responseObserver) {

        var all = this.dogAdoptionService
                .all()
                .stream()
                .map(ourDog -> com.example.adoptions.adoptions.grpc.Dog.newBuilder()
                        .setId(ourDog.id())
                        .setName(ourDog.name())
                        .setDescription(ourDog.description())
                        .build())
                .toList();

        responseObserver.onNext(Dogs.newBuilder().addAllDogs(all ).build());
        responseObserver.onCompleted();


    }
}


@Controller
class DogAdoptionGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @QueryMapping
    Collection<Dog> all() {
        return dogAdoptionService.all();
    }
}


@Controller
@ResponseBody
class DogAdoptionHttpController {

    private final DogAdoptionService service;

    DogAdoptionHttpController(DogAdoptionService service) {
        this.service = service;
    }

    @GetMapping("/dogs")
    Collection<Dog> all() {
        return this.service.all();
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.service.adopt(dogId, owner);
    }
}


@Transactional
@Service
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.dogRepository = dogRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    Collection<Dog> all() {
        return dogRepository.findAll();
    }


    void adopt(int dogId, String owner) {

        dogRepository.findById(dogId).ifPresent(dog -> {
            var updated = dogRepository.save(
                    new Dog(dog.id(), dog.name(), owner, dog.description()));
            System.out.println("adopted [" + updated + "]");
            applicationEventPublisher.publishEvent(new DogAdoptionEvent(dogId));
        });

    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}
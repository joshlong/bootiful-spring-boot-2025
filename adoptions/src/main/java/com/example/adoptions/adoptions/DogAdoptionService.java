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
class DogAdoptionGrpcService
        extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGrpcService(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @Override
    public void all(Empty request, StreamObserver<Dogs> responseObserver) {


        var all = this.dogAdoptionService
                .all()
                .stream()
                .map( ourDog -> com.example.adoptions.adoptions.grpc.Dog.newBuilder()
                        .setId(ourDog.id())
                        .setName(ourDog.name())
                        .setDescription(ourDog.description())
                        .build())
                .toList();

        responseObserver.onNext(Dogs.newBuilder().addAllDogs(all).build());
        responseObserver.onCompleted();

    }
}


@Controller
class DogAdoptionsGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionsGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @QueryMapping
    Collection<Dog> all() {
        return dogAdoptionService.all();
    }
}

@Controller
@ResponseBody
class DogAdoptionController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @GetMapping("/dogs")
    Collection<Dog> all() {
        return dogAdoptionService.all();
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        dogAdoptionService.adopt(dogId, owner);
    }
}

@Service
@Transactional
class DogAdoptionService {

    private final DogRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    DogAdoptionService(DogRepository repository, ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    void adopt(int dogId, String owner) {
        this.repository.findById(dogId).ifPresent(dog -> {
            var updated = this.repository.save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            System.out.println("adopted [" + updated + "]");
            applicationEventPublisher.publishEvent(new DogAdoptionEvent(dogId));

        });
    }

    Collection<Dog> all() {
        return repository.findAll();
    }
}


// look mom,no Lombok!!
record Dog(@Id int id, String name, String owner, String description) {
}


interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

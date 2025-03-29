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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Service
class DogAdoptionGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogRepository repository;

    DogAdoptionGrpcService(DogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void all(Empty request, StreamObserver<Dogs> responseObserver) {

        var all = this.repository.findAll()
                .stream()
                .map(ourDogs -> com.example.adoptions.adoptions.grpc.Dog.newBuilder()
                        .setId(ourDogs.id())
                        .setName(ourDogs.name())
                        .setDescription(ourDogs.description())
                        .build())
                .toList();


        responseObserver.onNext(Dogs.newBuilder().addAllDogs(all).build());
        responseObserver.onCompleted();

    }
}


@Controller
class DogAdoptionGraphqlController {

    private final DogRepository repository;

    DogAdoptionGraphqlController(DogRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return repository.findAll();
    }
}


@Controller
@ResponseBody
class DogAdoptionHttpController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionHttpController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        this.dogAdoptionService.adopt(dogId, owner);
    }
}

@Controller
@Transactional
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.dogRepository = dogRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    void adopt(int dogId, String owner) {

        this.dogRepository
                .findById(dogId)
                .ifPresent(dog -> {
                    var up = this.dogRepository.save(new Dog(dog.id(), dog.name(), dog.description(), owner));
                    System.out.println("updated " + up);
                    this.applicationEventPublisher.publishEvent(
                            new DogAdoptionEvent(dogId));
                });


    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String description, String owner) {
}
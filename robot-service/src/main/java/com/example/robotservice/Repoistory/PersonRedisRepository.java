package com.example.robotservice.Repoistory;

import com.example.robotservice.entity.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRedisRepository extends CrudRepository<Person, String> {
}
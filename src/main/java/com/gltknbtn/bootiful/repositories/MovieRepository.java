package com.gltknbtn.bootiful.repositories;

import com.gltknbtn.bootiful.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT p.id FROM Movie p")
    List<Long> getIds();

}

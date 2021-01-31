package com.gltknbtn.bootiful.services;

import com.gltknbtn.bootiful.models.Movie;
import com.gltknbtn.bootiful.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService implements IMovieService {

    @Autowired
    MovieRepository movieRepository;

    @Cacheable(cacheNames = "movies")
    @Override
    public List<Movie> findAll() {
        return movieRepository.findAll();
    }

    @Cacheable(cacheNames = "movie", key = "'movie#' + #id")
    @Override
    public Movie findOne(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    @CachePut(cacheNames = "movie", key = "'movie#' + #movie.id")
    @CacheEvict(cacheNames = "movies", allEntries = true)
    @Override
    public Movie newOne(Movie movie) {
        return movieRepository.save(movie);
    }
}

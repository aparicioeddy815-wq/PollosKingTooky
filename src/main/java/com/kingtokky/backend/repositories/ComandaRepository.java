package com.kingtokky.backend.repositories;

import com.kingtokky.backend.entities.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandaRepository extends JpaRepository<Comanda, Integer> {
}
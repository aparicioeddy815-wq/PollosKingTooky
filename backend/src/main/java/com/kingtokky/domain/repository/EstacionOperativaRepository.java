package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.personal.EstacionOperativa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacionOperativaRepository extends JpaRepository<EstacionOperativa, Long> {
    List<EstacionOperativa> findAllByOrderByNombreAsc();
}

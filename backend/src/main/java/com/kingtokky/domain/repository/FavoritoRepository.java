package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.menu.Favorito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    List<Favorito> findByUsuario_IdUsuario(Long idUsuario);
    Optional<Favorito> findByUsuario_IdUsuarioAndProducto_Id(Long idUsuario, Long idProducto);
}

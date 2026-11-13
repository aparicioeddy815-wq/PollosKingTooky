package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.entrevista.PostulanteDocumento;
import com.kingtokky.domain.model.entrevista.TipoDocumento;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostulanteDocumentoRepository extends JpaRepository<PostulanteDocumento, Long> {
    List<PostulanteDocumento> findByPostulante_IdOrderBySubidoEnDesc(Long idPostulante);
    List<PostulanteDocumento> findByPostulante_IdIn(Collection<Long> idsPostulante);
    Optional<PostulanteDocumento> findFirstByPostulante_IdAndTipoOrderBySubidoEnDesc(Long idPostulante, TipoDocumento tipo);
}

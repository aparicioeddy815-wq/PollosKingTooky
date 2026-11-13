package com.kingtokky.domain.model.entrevista;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulante_documento")
public class PostulanteDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_postulante", nullable = false)
    private Postulante postulante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDocumento tipo;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    private String nombreArchivo;

    @Column(name = "ruta", length = 500)
    private String ruta;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_subido_por")
    private Usuario subidoPor;

    @Column(name = "subido_en", nullable = false)
    private LocalDateTime subidoEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Postulante getPostulante() { return postulante; }
    public void setPostulante(Postulante postulante) { this.postulante = postulante; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public Usuario getSubidoPor() { return subidoPor; }
    public void setSubidoPor(Usuario subidoPor) { this.subidoPor = subidoPor; }

    public LocalDateTime getSubidoEn() { return subidoEn; }
    public void setSubidoEn(LocalDateTime subidoEn) { this.subidoEn = subidoEn; }
}

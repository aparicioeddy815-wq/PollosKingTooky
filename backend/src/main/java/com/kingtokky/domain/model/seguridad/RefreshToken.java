package com.kingtokky.domain.model.seguridad;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "revocado", nullable = false)
    private boolean revocado = false;

    @Column(name = "recordarme", nullable = false)
    private boolean recordarme = false;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "ultimo_uso")
    private LocalDateTime ultimoUso;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }

    public boolean isRevocado() { return revocado; }
    public void setRevocado(boolean revocado) { this.revocado = revocado; }

    public boolean isRecordarme() { return recordarme; }
    public void setRecordarme(boolean recordarme) { this.recordarme = recordarme; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getUltimoUso() { return ultimoUso; }
    public void setUltimoUso(LocalDateTime ultimoUso) { this.ultimoUso = ultimoUso; }
}

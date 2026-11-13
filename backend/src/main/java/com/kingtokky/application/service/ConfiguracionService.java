package com.kingtokky.application.service;

import com.kingtokky.domain.repository.ConfiguracionLocalRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lee los valores editables del local (tabla configuracion_local). */
@Service
@Transactional(readOnly = true)
public class ConfiguracionService {

    private final ConfiguracionLocalRepository repositorio;

    public ConfiguracionService(ConfiguracionLocalRepository repositorio) {
        this.repositorio = repositorio;
    }

    public String texto(String clave, String porDefecto) {
        return repositorio.findById(clave).map(c -> c.getValor()).orElse(porDefecto);
    }

    public int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(texto(clave, String.valueOf(porDefecto)).trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    public double decimal(String clave, double porDefecto) {
        try {
            return new BigDecimal(texto(clave, String.valueOf(porDefecto)).trim()).doubleValue();
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}

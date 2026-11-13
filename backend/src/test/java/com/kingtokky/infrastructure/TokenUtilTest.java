package com.kingtokky.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.infrastructure.security.TokenUtil;
import org.junit.jupiter.api.Test;

class TokenUtilTest {

    @Test
    void generaTokensDistintosYLargos() {
        assertThat(TokenUtil.generar()).isNotEqualTo(TokenUtil.generar()).hasSizeGreaterThan(40);
    }

    @Test
    void elHashEsSha256EnHexadecimalYEstable() {
        String hash = TokenUtil.sha256("king-tokky");
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(TokenUtil.sha256("king-tokky")).isEqualTo(hash);
    }
}

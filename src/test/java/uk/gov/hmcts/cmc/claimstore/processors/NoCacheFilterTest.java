package uk.gov.hmcts.cmc.claimstore.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NoCacheFilterTest {
    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private NoCacheFilter filter;

    @BeforeEach
    void setUp() {
        filter = new NoCacheFilter();
    }

    @Test
    void shouldAddHeaders() throws ServletException, IOException {
        filter.doFilterInternal(request, response, chain);
        assertAll(
            () -> verify(response).addHeader("Pragma", "no-cache"),
            () -> verify(response).addHeader("Cache-Control", "no-store"),
            () -> verifyNoInteractions(request)
        );
    }
}

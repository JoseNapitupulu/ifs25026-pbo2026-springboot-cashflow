package org.delcom.ifs25026pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Menyajikan halaman publik "/about" sebagai HTML yang sudah jadi (server-rendered).
 *
 * <p>Alasannya: Vaadin Flow mengirim kerangka kosong lalu menggambar isi halaman lewat
 * JavaScript. Crawler dan Lighthouse yang hanya membaca HTML awal jadi tidak menemukan
 * judul, deskripsi, maupun konten. Dengan menyajikan berkas statis langsung dari filter ini,
 * "/about" tetap 200 tanpa login, ringan (tanpa JavaScript), dan mudah diindeks.</p>
 */
public class PublicPageFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PublicPageFilter.class);

    /** Placeholder di berkas HTML yang diganti URL absolut halaman ini. */
    private static final String CANONICAL_PLACEHOLDER = "__CANONICAL_URL__";

    private final String resourcePath;
    private final String publicPath;
    private final String cacheControl;
    private volatile String template;

    public PublicPageFilter(String resourcePath, String publicPath, String cacheControl) {
        this.resourcePath = resourcePath;
        this.publicPath = publicPath;
        this.cacheControl = cacheControl;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String page = template();
        if (page == null) {
            // Berkas tidak ditemukan: biarkan Vaadin menangani permintaan seperti biasa.
            filterChain.doFilter(request, response);
            return;
        }

        // Canonical harus absolut agar diterima crawler dan Lighthouse. URL dirakit dari
        // permintaan (mendukung header proxy SnapDeploy/Render), bukan di-hardcode.
        byte[] body = page.replace(CANONICAL_PLACEHOLDER, canonicalUrl(request))
                .getBytes(StandardCharsets.UTF_8);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", cacheControl);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLength(body.length);

        if (!"HEAD".equalsIgnoreCase(request.getMethod())) {
            response.getOutputStream().write(body);
        }
        response.flushBuffer();
    }

    /** Schema + host dari permintaan, menghormati header proxy bila ada. */
    private String canonicalUrl(HttpServletRequest request) {
        String scheme = firstNonBlank(request.getHeader("X-Forwarded-Proto"), request.getScheme());
        String host = firstNonBlank(request.getHeader("X-Forwarded-Host"), request.getHeader("Host"));
        if (host == null || host.isBlank()) {
            host = request.getServerName()
                    + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");
        }
        return scheme.toLowerCase(Locale.ROOT) + "://" + host + publicPath;
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.split(",")[0].trim();
        }
        return second;
    }

    private String template() {
        String loaded = template;
        if (loaded != null) {
            return loaded;
        }

        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            LOG.warn("Berkas halaman publik '{}' tidak ditemukan di classpath.", resourcePath);
            return null;
        }

        try (InputStream input = resource.getInputStream()) {
            loaded = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            template = loaded;
            return loaded;
        } catch (IOException ex) {
            LOG.error("Gagal membaca berkas halaman publik '{}'.", resourcePath, ex);
            return null;
        }
    }
}

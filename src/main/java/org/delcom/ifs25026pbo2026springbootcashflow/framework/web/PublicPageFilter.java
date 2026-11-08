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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Menyajikan berkas publik (halaman "/about", sitemap, robots) sebagai isi yang sudah jadi.
 *
 * <p>Alasannya: Vaadin Flow mengirim kerangka kosong lalu menggambar isi halaman lewat
 * JavaScript. Crawler dan Lighthouse yang hanya membaca HTML awal jadi tidak menemukan
 * judul, deskripsi, maupun konten. Dengan menyajikan berkas statis langsung dari filter ini,
 * "/about" tetap 200 tanpa login, ringan (tanpa JavaScript), dan mudah diindeks.</p>
 *
 * <p>Sitemap dan robots memakai filter yang sama karena keduanya wajib memuat URL absolut,
 * yang hanya bisa diketahui saat permintaan datang.</p>
 */
public class PublicPageFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PublicPageFilter.class);

    /** Placeholder di berkas yang diganti URL absolut halaman ini (mis. canonical, og:url). */
    private static final String CANONICAL_PLACEHOLDER = "__CANONICAL_URL__";

    /** Placeholder untuk origin situs, dipakai sitemap dan robots. */
    private static final String BASE_PLACEHOLDER = "__BASE_URL__";

    private final String resourcePath;
    private final String publicPath;
    private final String contentType;
    private final String cacheControl;
    private final String robotsDirective;
    private volatile String template;

    /**
     * @param robotsDirective nilai header {@code X-Robots-Tag}, atau {@code null} bila tidak perlu.
     *        Halaman HTML memakainya untuk menegaskan "index, follow", sebab sebagian proxy
     *        (mis. SnapDeploy) menambahkan {@code noindex} sendiri dan crawler membacanya
     *        sebagai halaman terlarang untuk diindeks.
     */
    public PublicPageFilter(
            String resourcePath,
            String publicPath,
            String contentType,
            String cacheControl,
            String robotsDirective
    ) {
        this.resourcePath = resourcePath;
        this.publicPath = publicPath;
        this.contentType = contentType;
        this.cacheControl = cacheControl;
        this.robotsDirective = robotsDirective;
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
        String baseUrl = baseUrl(request);
        byte[] body = page.replace(BASE_PLACEHOLDER, baseUrl)
                .replace(CANONICAL_PLACEHOLDER, baseUrl + publicPath)
                .getBytes(StandardCharsets.UTF_8);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", cacheControl);
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (robotsDirective != null) {
            response.setHeader("X-Robots-Tag", robotsDirective);
        }
        response.setContentLength(body.length);

        if (!"HEAD".equalsIgnoreCase(request.getMethod())) {
            response.getOutputStream().write(body);
        }
        response.flushBuffer();
    }

    /** Origin situs (schema + host) dari permintaan, menghormati header proxy bila ada. */
    private String baseUrl(HttpServletRequest request) {
        String host = firstNonBlank(request.getHeader("X-Forwarded-Host"), request.getHeader("Host"));
        if (host == null || host.isBlank()) {
            host = request.getServerName()
                    + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");
        }
        return scheme(request, host) + "://" + host;
    }

    /**
     * Skema URL publik. Proxy SnapDeploy/Cloudflare meneruskan permintaan ke origin lewat HTTP
     * biasa tanpa {@code X-Forwarded-Proto}, tetapi halaman diakses lewat HTTPS. Canonical
     * {@code http://} dianggap URL lain oleh mesin pencari, jadi host publik dipaksa HTTPS.
     */
    private static String scheme(HttpServletRequest request, String host) {
        String scheme = firstNonBlank(
                protoFromForwarded(request.getHeader("Forwarded")),
                request.getHeader("X-Forwarded-Proto"),
                protoFromCfVisitor(request.getHeader("CF-Visitor")),
                request.getScheme());
        if ("http".equalsIgnoreCase(scheme) && isPublicHost(host)) {
            return "https";
        }
        return scheme.toLowerCase(Locale.ROOT);
    }

    /** Skema dari header standar {@code Forwarded: for=1.2.3.4;proto=https;by=...}. */
    private static String protoFromForwarded(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        Matcher matcher = Pattern.compile("(?i)proto=<?\"?([a-z]+)\"?>?").matcher(header);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Skema dari header Cloudflare {@code CF-Visitor: {"scheme":"https"}}. */
    private static String protoFromCfVisitor(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        Matcher matcher = Pattern.compile("\"scheme\"\\s*:\\s*\"([a-zA-Z]+)\"").matcher(header);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** Host yang dijangkau publik (punya domain, bukan localhost/IP) selalu dilayani HTTPS. */
    private static boolean isPublicHost(String host) {
        if (host.startsWith("[") || host.startsWith("127.") || host.startsWith("0.0.0.0")) {
            return false;
        }
        String name = host;
        int colon = name.indexOf(':');
        if (colon > 0) {
            name = name.substring(0, colon);
        }
        return !"localhost".equalsIgnoreCase(name) && name.contains(".");
    }

    /** Nilai pertama yang terisi, dengan pilihan terakhir selalu tersedia (mis. skema default). */
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return "http";
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

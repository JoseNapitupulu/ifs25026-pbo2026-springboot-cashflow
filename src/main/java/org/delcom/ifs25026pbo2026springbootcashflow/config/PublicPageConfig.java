package org.delcom.ifs25026pbo2026springbootcashflow.config;

import org.delcom.ifs25026pbo2026springbootcashflow.framework.web.PublicPageFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Halaman publik dan berkas SEO disajikan sebagai isi statis yang sudah jadi.
 *
 * <p>Vaadin mengirim kerangka kosong lalu menggambar isi lewat JavaScript, sehingga crawler
 * dan Lighthouse tidak menemukan konten indeks. Filter di sini menyajikan HTML yang sudah jadi
 * untuk "/about" (tetap 200 tanpa login), sitemap XML yang valid, dan robots.txt yang
 * mencantumkan alamat sitemap.</p>
 *
 * <p>Ketiganya butuh URL absolut, dan URL itu baru diketahui saat permintaan datang (domain
 * SnapDeploy/Render berbeda dari localhost), jadi tidak bisa berupa berkas statis biasa.</p>
 */
@Configuration
public class PublicPageConfig {

    private static final String HTML = "text/html;charset=UTF-8";
    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    /**
     * Ditegaskan lewat header, bukan hanya meta tag, agar tidak tertimpa noindex dari proxy.
     */
    private static final String INDEXABLE = "index, follow";

    @Bean
    public FilterRegistrationBean<PublicPageFilter> publicAboutPageFilter() {
        return register(
                "publicAboutPageFilter",
                new PublicPageFilter("META-INF/resources/about.html", "/about", HTML,
                        "public, max-age=600", INDEXABLE),
                "/about", "/about/"
        );
    }

    @Bean
    public FilterRegistrationBean<PublicPageFilter> publicSitemapFilter() {
        return register(
                "publicSitemapFilter",
                new PublicPageFilter("META-INF/resources/sitemap.xml", "/sitemap.xml",
                        "application/xml;charset=UTF-8", "public, max-age=3600", null),
                "/sitemap.xml"
        );
    }

    @Bean
    public FilterRegistrationBean<PublicPageFilter> publicRobotsFilter() {
        return register(
                "publicRobotsFilter",
                new PublicPageFilter("META-INF/resources/robots.txt", "/robots.txt",
                        "text/plain;charset=UTF-8", "public, max-age=3600", null),
                "/robots.txt"
        );
    }

    private static FilterRegistrationBean<PublicPageFilter> register(
            String name,
            PublicPageFilter filter,
            String... patterns
    ) {
        FilterRegistrationBean<PublicPageFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName(name);
        registration.addUrlPatterns(patterns);
        registration.setOrder(ORDER);
        return registration;
    }
}

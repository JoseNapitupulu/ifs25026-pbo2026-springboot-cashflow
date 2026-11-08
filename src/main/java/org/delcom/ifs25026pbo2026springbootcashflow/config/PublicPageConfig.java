package org.delcom.ifs25026pbo2026springbootcashflow.config;

import org.delcom.ifs25026pbo2026springbootcashflow.framework.web.PublicPageFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Halaman publik "/about" disajikan sebagai HTML statis.
 *
 * <p>Vaadin mengirim kerangka kosong lalu menggambar isi lewat JavaScript, sehingga crawler
 * dan Lighthouse tidak menemukan konten indeks. Filter ini menyajikan HTML yang sudah jadi,
 * tetap 200 tanpa login, dan menjadi halaman yang diperiksa crawler.</p>
 */
@Configuration
public class PublicPageConfig {

    @Bean
    public FilterRegistrationBean<PublicPageFilter> publicAboutPageFilter() {
        FilterRegistrationBean<PublicPageFilter> registration = new FilterRegistrationBean<>(
                new PublicPageFilter("META-INF/resources/about.html", "/about", "public, max-age=600")
        );
        registration.setName("publicAboutPageFilter");
        registration.addUrlPatterns("/about", "/about/");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}

package org.delcom.ifs25026pbo2026springbootcashflow.config;

import org.delcom.ifs25026pbo2026springbootcashflow.framework.web.StaticResourceCacheFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Konfigurasi web di luar Vaadin: header cache untuk aset statis.
 */
@Configuration
public class WebConfig {

    /** Aset build Vaadin dan favicon diberi masa cache panjang. */
    @Bean
    public FilterRegistrationBean<StaticResourceCacheFilter> staticResourceCacheFilter() {
        FilterRegistrationBean<StaticResourceCacheFilter> registration =
                new FilterRegistrationBean<>(new StaticResourceCacheFilter());
        registration.setName("staticResourceCacheFilter");
        registration.addUrlPatterns("/VAADIN/*", "/favicon.ico");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }
}

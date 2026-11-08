package org.delcom.ifs25026pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Memberi header cache panjang untuk aset statis (bundle Vaadin, favicon).
 *
 * <p>Vaadin mengirim aset build dengan {@code Cache-Control: no-cache}, sehingga browser
 * mengunduh ulang bundle JavaScript berukuran besar pada setiap kunjungan. Nama berkas build
 * Vaadin sudah mengandung hash konten, jadi aman di-cache lama. Filter ini membungkus respons
 * agar nilai dari Vaadin tidak menimpa header yang kita set.</p>
 */
public class StaticResourceCacheFilter extends OncePerRequestFilter {

    private static final String IMMUTABLE_CACHE = "public, max-age=31536000, immutable";
    private static final String LONG_CACHE = "public, max-age=2592000";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String cacheControl = path != null && path.startsWith("/VAADIN/build/")
                ? IMMUTABLE_CACHE
                : LONG_CACHE;

        filterChain.doFilter(request, new CacheControlResponseWrapper(response, cacheControl));
    }

    /**
     * Wrapper yang memaksa satu nilai {@code Cache-Control} dan menghapus header
     * pembatal cache ({@code Pragma}, {@code Expires}) yang mungkin ditambahkan kemudian.
     */
    private static final class CacheControlResponseWrapper extends HttpServletResponseWrapper {

        private final String cacheControl;

        private CacheControlResponseWrapper(HttpServletResponse response, String cacheControl) {
            super(response);
            this.cacheControl = cacheControl;
            super.setHeader("Cache-Control", cacheControl);
        }

        @Override
        public void setHeader(String name, String value) {
            if ("Cache-Control".equalsIgnoreCase(name)) {
                super.setHeader(name, cacheControl);
                return;
            }
            if ("Pragma".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name)) {
                return;
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if ("Cache-Control".equalsIgnoreCase(name)) {
                super.setHeader(name, cacheControl);
                return;
            }
            if ("Pragma".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name)) {
                return;
            }
            super.addHeader(name, value);
        }

        @Override
        public void setDateHeader(String name, long date) {
            if ("Expires".equalsIgnoreCase(name)) {
                return;
            }
            super.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            if ("Expires".equalsIgnoreCase(name)) {
                return;
            }
            super.addDateHeader(name, date);
        }

        @Override
        public String getHeader(String name) {
            String header = super.getHeader(name);
            if (header == null || header.isBlank()) {
                return "Cache-Control".equalsIgnoreCase(name) ? cacheControl : header;
            }
            return header;
        }

        @Override
        public java.util.Collection<String> getHeaders(String name) {
            List<String> headers = new ArrayList<>(super.getHeaders(name));
            if (headers.isEmpty() && "Cache-Control".equalsIgnoreCase(name)) {
                headers.add(cacheControl);
            }
            return headers;
        }
    }
}

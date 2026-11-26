package com.jouletracker.platform.shared.infrastructure.i18n.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configures locale resolution for REST requests.
 */
@Configuration
public class LocaleConfiguration {

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        var spanish = Locale.forLanguageTag("es");
        resolver.setDefaultLocale(spanish);
        resolver.setSupportedLocales(List.of(spanish, Locale.ENGLISH));
        return resolver;
    }
}


package com.sindikbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationPropertiesTest {
    @Test
    void credentialsAndJwtSecretHaveNoConfigurationDefaults() throws Exception {
        var application = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml")).get(0);
        var sources = new MutablePropertySources();
        sources.addFirst(application);
        var resolver = new PropertySourcesPropertyResolver(sources);

        assertEquals("jdbc:postgresql://localhost:5432/sindik",
                resolver.getRequiredProperty("spring.datasource.url"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.getRequiredProperty("spring.datasource.username"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.getRequiredProperty("spring.datasource.password"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.getRequiredProperty("security.jwt.secret"));
    }
}

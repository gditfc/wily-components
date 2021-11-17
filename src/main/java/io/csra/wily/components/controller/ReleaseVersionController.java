package io.csra.wily.components.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * To enable:
 * 1. Add line 'release.version=@project.version@' to application.properties
 * 2. Add resource filtering to your pom.xml via:
 * {@code
 * <build>
 * <resources>
 * <resource>
 * <directory>src/main/resources</directory>
 * <filtering>true</filtering>
 * </resource>
 * </resources>
 * </build>
 * }
 * 3. Add line 'release.version.enabled=false' to application.properties to disable this controller
 */
@RestController
@ConditionalOnProperty(value="release.version.enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/api/public/release-version")
public class ReleaseVersionController {

    @Value("${release.version:unknown}")
    private String releaseVersion;

    /**
     * The mapping to fetch the current release version from the environment.
     *
     * @return the current release version fetched from the environment
     */
    @GetMapping
    public Map<String, String> getReleaseVersion() {
        return singletonMap("version", releaseVersion);
    }

}

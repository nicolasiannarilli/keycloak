/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.it.cli.dist;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.keycloak.it.junit5.extension.DistributionTest;
import org.keycloak.it.junit5.extension.LegacyStore;
import org.keycloak.it.utils.KeycloakDistribution;

import io.quarkus.test.junit.main.Launch;

@DistributionTest(keepAlive =true)
@LegacyStore
public class MetricsDistTest {

    @Test
    @Launch({ "start-dev" })
    void testMetricsEndpointNotEnabled() {
        when().get("/metrics").then()
                .statusCode(404);
        when().get("/q/metrics").then()
                .statusCode(404);
    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true" })
    void testMetricsEndpoint() {
        when().get("/metrics").then()
                .statusCode(200)
                .body(containsString("jvm_gc_"))
                .body(containsString("vendor_cache_manager_keycloak_cache_realms_"));
    }

    @Test
    @Launch({ "start-dev", "--http-relative-path=/auth", "--metrics-enabled=true" })
    void testMetricsEndpointUsingRelativePath() {
        when().get("/auth/metrics").then()
                .statusCode(200)
                .body(containsString("jvm_gc_"));
    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true" })
    void testMetricsEndpointDoesNotEnableHealth() {
        when().get("/health").then()
                .statusCode(404);
    }

    @Test
    void testUsingRelativePath(KeycloakDistribution distribution) {
        for (String relativePath : List.of("/auth", "/auth/")) {
            distribution.run("start-dev", "--metrics-enabled=true", "--http-relative-path=" + relativePath);
            if (!relativePath.endsWith("/")) {
                relativePath = relativePath + "/";
            }
            when().get(relativePath + "metrics").then().statusCode(200);
            distribution.stop();
        }
    }

    @Test
    void testMultipleRequests(KeycloakDistribution distribution) throws Exception {
        for (String relativePath : List.of("/", "/auth/")) {
            distribution.run("start-dev", "--metrics-enabled=true", "--http-relative-path=" + relativePath);
            CompletableFuture future = CompletableFuture.completedFuture(null);

            for (int i = 0; i < 3; i++) {
                future = CompletableFuture.allOf(CompletableFuture.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 200; i++) {
                            when().get(relativePath + "metrics").then().statusCode(200);
                        }
                    }
                }), future);
            }

            future.get(5, TimeUnit.MINUTES);

            distribution.stop();
        }
    }
}

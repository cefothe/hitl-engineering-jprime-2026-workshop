package com.example.payhub.ingress;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/// Enforces the hot-path framework ban (CLAUDE.md, ADR 0019, prd/002 AC5).
///
/// ADR 0019 chose a single Maven module, so Spring/JPA jars remain on the BUILD
/// classpath. Physical isolation does not protect the hot path — this rule does.
/// It is wired into {@code mvn test}; a forbidden import under the ingress
/// package fails the build. This is the load-bearing gate the band-check note
/// flagged: it must stay green before any merge.
class HotPathFrameworkBanTest {

    private static final String INGRESS_PACKAGE = "com.example.payhub.ingress";

    private static JavaClasses ingressClasses() {
        return new ClassFileImporter()
                // Analyse production code only; test classes legitimately use
                // junit/testcontainers and are not part of the hot path.
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(INGRESS_PACKAGE);
    }

    @Test
    void ingressMustNotDependOnSpring() {
        ArchRule rule = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("the hot path is pure Java 21 — no Spring (CLAUDE.md, ADR 0019)");

        rule.check(ingressClasses());
    }

    @Test
    void ingressMustNotDependOnJakartaPersistence() {
        ArchRule rule = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .because("the hot path writes via plain JDBC — no JPA entities (ADR 0002, ADR 0019)");

        rule.check(ingressClasses());
    }

    @Test
    void ingressMustNotDependOnSpringSecurityCrypto() {
        // spring-security-crypto is specifically called out as banned (ADR 0013);
        // its package lives under org.springframework.security.crypto.
        ArchRule rule = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework.security..")
                .because("HMAC verification will use javax.crypto only — spring-security-crypto is banned (ADR 0013)");

        rule.check(ingressClasses());
    }
}

package com.barber_manager.auth_service.architecture;

import com.barber_manager.auth_service.AuthServiceApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architektura: auth-service (czysta architektura)")
class LayeredArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(AuthServiceApplication.class);

    @Test
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAPackage("..controller..");

        rule.check(CLASSES);
    }

    @Test
    void controllersShouldNotAccessRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(CLASSES);
    }

    @Test
    void feignClientsShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..client..")
                .should().accessClassesThat().resideInAPackage("..controller..");

        rule.check(CLASSES);
    }

    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("..controller..", "..web..")
                .layer("Services").definedBy("..service..")
                .layer("AdaptersOut").definedBy("..repository..", "..client..")
                .layer("Domain").definedBy("..entity..", "..enums..", "..dto..", "..exceptions..")
                .layer("Infrastructure").definedBy("..config..", "..error..")
                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Infrastructure")
                .whereLayer("AdaptersOut").mayOnlyBeAccessedByLayers("Services")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Controllers", "Services", "AdaptersOut", "Infrastructure");

        rule.check(CLASSES);
    }
}

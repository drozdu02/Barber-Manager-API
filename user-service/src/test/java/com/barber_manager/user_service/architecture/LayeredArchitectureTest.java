package com.barber_manager.user_service.architecture;

import com.barber_manager.user_service.UserServiceApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Architektura: user-service (czysta architektura)")
class LayeredArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(UserServiceApplication.class);

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
    void repositoriesShouldNotDependOnServicesOrControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().accessClassesThat().resideInAnyPackage("..service..", "..controller..");

        rule.check(CLASSES);
    }

    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("..controller..")
                .layer("Services").definedBy("..service..")
                .layer("Repositories").definedBy("..repository..")
                .layer("Domain").definedBy("..entity..", "..enums..", "..dto..", "..exceptions..", "..mapper..")
                .layer("Infrastructure").definedBy("..config..", "..error..")
                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Infrastructure")
                .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Controllers", "Services", "Repositories", "Infrastructure");

        rule.check(CLASSES);
    }
}

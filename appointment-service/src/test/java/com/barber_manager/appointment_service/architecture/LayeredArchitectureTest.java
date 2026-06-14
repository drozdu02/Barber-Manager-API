package com.barber_manager.appointment_service.architecture;

import com.barber_manager.appointment_service.AppointmentServiceApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Architektura: appointment-service (czysta architektura)")
class LayeredArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(AppointmentServiceApplication.class);

    @Test
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAPackage("..controller..");

        rule.check(CLASSES);
    }

    @Test
    void publicControllersShouldNotAccessRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .and().resideOutsideOfPackage("..controller.admin..")
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
    void eventHandlersShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..events.handlers..")
                .should().accessClassesThat().resideInAPackage("..controller..");

        rule.check(CLASSES);
    }
}

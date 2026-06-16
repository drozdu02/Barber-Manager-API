package com.barber_manager.appointment_service.architecture;

import com.barber_manager.appointment_service.AppointmentServiceApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Architektura: appointment-service (heksagonalna)")
class LayeredArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(AppointmentServiceApplication.class);

    @Test
    void controllersShouldDependOnInboundPortsNotConcreteServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..service..");

        rule.check(CLASSES);
    }

    @Test
    void outboundAdaptersShouldResideInInfrastructure() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Adapter")
                .and().resideInAPackage("..adapter.out..")
                .should().resideInAPackage("..infrastructure..");

        rule.check(CLASSES);
    }

    @Test
    void applicationServicesShouldNotAccessJpaRepositoriesDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(CLASSES);
    }

    @Test
    void domainServicesShouldNotAccessJpaRepositoriesDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..schedule.domain..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(CLASSES);
    }

    @Test
    void domainServicesShouldNotAccessControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..schedule.domain..")
                .should().accessClassesThat().resideInAPackage("..controller..");

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

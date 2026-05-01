package com.example.petapi;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.example.petapi")
class ArchitectureTest {

    @ArchTest
    ArchRule layered_architecture = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    ArchRule controllers_must_not_access_repositories_directly = noClasses()
            .that().resideInAPackage("..controller..")
            .should().accessClassesThat().resideInAPackage("..repository..");

    @ArchTest
    ArchRule services_must_not_depend_on_controllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    ArchRule filters_must_not_access_services = noClasses()
            .that().resideInAPackage("..filter..")
            .should().accessClassesThat().resideInAPackage("..service..");

    @ArchTest
    ArchRule jobs_must_only_access_repositories = noClasses()
            .that().resideInAPackage("..job..")
            .should().accessClassesThat().resideInAPackage("..controller..");

    @ArchTest
    ArchRule exceptions_must_reside_in_exception_package = classes()
            .that().areAssignableTo(RuntimeException.class)
            .and().haveSimpleNameEndingWith("Exception")
            .and().resideInAPackage("com.example.petapi..")
            .should().resideInAPackage("..exception..");
}

package com.gihub.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.gihub.chriswhite199.upsourceapi.schema.CodeGenerator.DTO_PACKAGE;

public class Service {
  public String name;
  public String description;
  public List<Method> methods;

  public void generateSource(File outputDir) {
    final var methodSpecs = this.methods.stream()
            .map(method -> {
              final var returnPackage = CodeGenerator.extractClassPackage(method.returnType);
              final var returnType = CodeGenerator.extractClassName(method.returnType);

              final var argPackage = CodeGenerator.extractClassPackage(method.argumentType);
              final var argType = CodeGenerator.extractClassName(method.argumentType);

              return MethodSpec.methodBuilder(method.name)
                      .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                      .returns(ClassName.get(returnPackage.map(pkg -> DTO_PACKAGE + "." + pkg)
                              .orElse(DTO_PACKAGE), returnType.orElseThrow()))
                      .addParameter(ClassName.get(argPackage.map(pkg -> DTO_PACKAGE + "." + pkg)
                              .orElse(DTO_PACKAGE), argType.orElseThrow()), "arg")
                      .build();
            })
            .collect(Collectors.toList());

    TypeSpec service = TypeSpec.interfaceBuilder(this.name)
            .addModifiers(Modifier.PUBLIC)
            .addMethods(methodSpecs)
            .build();

    JavaFile javaFile = JavaFile.builder("com.github.chriswhite199.upsourceapi.service", service)
            .addFileComment(this.description)
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating service: " + this.name, e);
    }
  }
}

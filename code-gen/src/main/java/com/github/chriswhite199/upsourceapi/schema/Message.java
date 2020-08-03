package com.github.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Message {
  public String name;
  public String description;
  public List<Field> fields;

  public void generateSource(File outputDir) {
    final var className = ClassName.get(CodeGenerator.MSGS_PACKAGE, this.name);

    TypeSpec message = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addMethods(this.fields.stream()
                    .map(field -> Field.asWithMethodSpec(field, className))
                    .collect(Collectors.toList()))
            .addFields(this.fields.stream()
                    .map(Field::asFieldSpec)
                    .collect(Collectors.toList()))
            .build();

    JavaFile javaFile = JavaFile.builder(CodeGenerator.MSGS_PACKAGE, message)
            .addFileComment(Optional.ofNullable(this.description).orElse(""))
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating DTO: " + this.name, e);
    }
  }
}

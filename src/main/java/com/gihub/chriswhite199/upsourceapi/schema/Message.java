package com.gihub.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gihub.chriswhite199.upsourceapi.schema.CodeGenerator.DTO_PACKAGE;

public class Message {
  public String name;
  public String description;
  public List<Field> fields;

  public void generateSource(File outputDir) {
    TypeSpec message = TypeSpec.classBuilder(this.name)
            .addModifiers(Modifier.PUBLIC)
            .addFields(this.fields.stream()
                    .map(Field::asFieldSpec)
                    .collect(Collectors.toList()))
            .build();

    JavaFile javaFile = JavaFile.builder(DTO_PACKAGE + ".messages", message)
            .addFileComment(Optional.ofNullable(this.description).orElse(""))
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating DTO: " + this.name, e);
    }
  }
}

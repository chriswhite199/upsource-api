package com.github.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EnumModel {
  public String name;
  public String description;
  public List<EnumValue> values;

  public void generateSource(File outputDir) {
    TypeSpec.Builder eNumBuilder = TypeSpec.enumBuilder(this.name)
            .addField(int.class, "number", Modifier.PRIVATE, Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                    .addParameter(int.class, "number")
                    .addStatement("this.$N = $N", "number", "number")
                    .build())
            .addMethod(MethodSpec.methodBuilder("getNumber")
                    .addStatement("return this.$N", "number")
                    .returns(int.class).build())
            .addModifiers(Modifier.PUBLIC);

    eNumBuilder.addEnumConstant("_NA_", TypeSpec.anonymousClassBuilder("$L", 0)
            .addAnnotation(Deprecated.class)
            .build());

    values.forEach(value -> eNumBuilder.addEnumConstant(value.name,
            TypeSpec.anonymousClassBuilder("$L", value.number).build()));

    TypeSpec eNum = eNumBuilder.build();

    JavaFile javaFile = JavaFile.builder(CodeGenerator.ENUM_PACKAGE, eNum)
            .addFileComment(Optional.ofNullable(this.description).orElse(""))
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating enum: " + this.name, e);
    }
  }
}

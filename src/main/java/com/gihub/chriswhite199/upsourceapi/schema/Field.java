package com.gihub.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

public class Field {
  public String name;
  public String description;
  public String label;
  public String type;

  public static FieldSpec asFieldSpec(Field field) {
    return FieldSpec.builder(mapType(field), field.name)
            .addJavadoc(String.format("(%s) %s", field.label, field.description))
            .build();
  }

  public static TypeName mapType(Field field) {
    switch (field.type) {
      case "String":
        return field.label.equals("repeated")
                ? ArrayTypeName.get(String.class)
                : ClassName.get(String.class);

      case "Int32":
        return field.label.equals("repeated")
                ? ArrayTypeName.get(int.class)
                : field.label.equals("required") ? TypeName.INT : ClassName.get(Integer.class);

      case "Int64":
        return field.label.equals("repeated")
                ? ArrayTypeName.get(long.class)
                : field.label.equals("required") ? TypeName.LONG : ClassName.get(Long.class);

      case "Bool":
        return field.label.equals("repeated")
                ? ArrayTypeName.get(boolean.class)
                : field.label.equals("required") ? TypeName.BOOLEAN : ClassName.get(Boolean.class);

      default:
        final var typeName = ClassName.get(CodeGenerator.DTO_PACKAGE + ".messages", field.type);
        return field.label.equals("repeated")
                ? ArrayTypeName.of(typeName)
                : typeName;
    }
  }
}

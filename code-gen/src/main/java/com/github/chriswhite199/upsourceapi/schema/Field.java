package com.github.chriswhite199.upsourceapi.schema;

import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;

public class Field {
    public String name;
    public String description;
    public String label;
    public String type;

    public static FieldSpec asFieldSpec(Field field) {
        return FieldSpec.builder(mapType(field), field.name)
                .addModifiers(Modifier.PRIVATE)
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
                final var pkgName = field.type.endsWith("Enum")
                        ? CodeGenerator.ENUM_PACKAGE
                        : CodeGenerator.MSGS_PACKAGE;
                final var typeName = ClassName.get(pkgName, field.type);
                return field.label.equals("repeated")
                        ? ArrayTypeName.of(typeName)
                        : typeName;
        }
    }

    public static MethodSpec asWithMethodSpec(Field field, TypeName parentTypeName) {
        return MethodSpec.methodBuilder("with" + StringUtils.capitalize(field.name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapType(field), field.name, Modifier.FINAL)
                .addStatement("this.$1L = $1L; return this", field.name)
                .returns(parentTypeName)
                .build();
    }

    public static MethodSpec getterMethodSpec(Field field) {
        return MethodSpec.methodBuilder("get" + StringUtils.capitalize(field.name))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$1L", field.name)
                .returns(mapType(field))
                .build();
    }

    public static MethodSpec setterMethodSpec(Field field) {
        return MethodSpec.methodBuilder("set" + StringUtils.capitalize(field.name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapType(field), field.name, Modifier.FINAL)
                .addStatement("this.$1L = $1L", field.name)
                .build();
    }
}

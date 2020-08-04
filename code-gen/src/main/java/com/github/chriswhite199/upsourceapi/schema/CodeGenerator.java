package com.github.chriswhite199.upsourceapi.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CodeGenerator {
  public static final String DTO_PACKAGE = "com.github.chriswhite199.upsourceapi.dto";
  public static final String MSGS_PACKAGE = DTO_PACKAGE + ".messages";
  public static final String ENUM_PACKAGE = DTO_PACKAGE + ".enums";
  public static final ClassName RPC_ERROR_CLASS_NAME =
          ClassName.get(CodeGenerator.DTO_PACKAGE, "RpcError");

  public static final ClassName RPC_EXCEPTION_CLASS_NAME =
          ClassName.get(CodeGenerator.DTO_PACKAGE, "RpcException");

  public static void main(final String[] args) {
    final var inputDir = new File(args[0]);
    final var outputDir = new File(args[1]);

    outputDir.mkdirs();
    final var objMapper = new ObjectMapper();

    generateRpcError(outputDir);
    generateRpcException(outputDir);
    generateResultWrapper(outputDir);

    Arrays.stream(Objects.requireNonNull(inputDir.listFiles()))
            .forEach(inputFile -> {
              try {
                final var schema = objMapper.readValue(inputFile, Schema.class);

                Optional.ofNullable(schema.services)
                        .filter(services -> !services.isEmpty())
                        .ifPresent(services -> generateServices(schema.services, outputDir));

                Optional.ofNullable(schema.messages)
                        .filter(messages -> !messages.isEmpty())
                        .ifPresent(messages -> messages.forEach(message -> message.generateSource(outputDir)));

                Optional.ofNullable(schema.enums)
                        .filter(enums -> !enums.isEmpty())
                        .ifPresent(enums -> enums.forEach(eNum -> eNum.generateSource(outputDir)));
              } catch (IOException e) {
                throw new RuntimeException("Failed processing: " + inputFile, e);
              }
            });
  }

  public static Optional<String> extractClassPackage(String type) {
    final int lastPeriodIdx = type.lastIndexOf('.');
    return Optional.of(lastPeriodIdx)
            .filter(idx -> idx != -1)
            .map(idx -> type.substring(0, idx));
  }

  public static Optional<String> extractClassName(String type) {
    final int lastPeriodIdx = type.lastIndexOf('.');
    return Optional.of(lastPeriodIdx)
            .filter(idx -> idx != -1)
            .map(idx -> type.substring(idx + 1));
  }

  private static void generateServices(List<Service> services, File outputDir) {
    services.forEach(service -> service.generateSource(outputDir));
  }

  private static void generateResultWrapper(File outputDir) {
    final var className = ClassName.get(CodeGenerator.DTO_PACKAGE, "RpcResult");

    TypeSpec message = TypeSpec.classBuilder(className)
            .addTypeVariable(TypeVariableName.get("T"))
            .addField(FieldSpec.builder(TypeVariableName.get("T"), "result")
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            .addField(FieldSpec.builder(RPC_ERROR_CLASS_NAME, "error")
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            // default constructor
            .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            // all args constructor
            .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeVariableName.get("T"), "result")
                    .addParameter(RPC_ERROR_CLASS_NAME, "error")
                    .addCode(CodeBlock.builder()
                            .addStatement("this.$1L = $1L", "result")
                            .addStatement("this.$1L = $1L", "error")
                            .build())
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build();

    JavaFile javaFile = JavaFile.builder(className.packageName(), message)
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating RpcException", e);
    }
  }

  private static void generateRpcException(File outputDir) {
    TypeSpec message = TypeSpec.classBuilder(RPC_EXCEPTION_CLASS_NAME)
            .superclass(IOException.class)
            // all args constructor
            .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(RPC_ERROR_CLASS_NAME, "error")
                    .addCode(CodeBlock.builder()
                            .addStatement("super(String.format(\"RPC Error: code=%d, message=%s\", $L, $L))",
                                    "error.code", "error.message")
                            .addStatement("this.$1L = $1L", "error")
                            .build())
                    .build())
            .addField(FieldSpec.builder(RPC_ERROR_CLASS_NAME, "error")
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            .addModifiers(Modifier.PUBLIC)

            .build();

    JavaFile javaFile = JavaFile.builder(RPC_EXCEPTION_CLASS_NAME.packageName(), message)
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating RpcException", e);
    }
  }

  private static void generateRpcError(File outputDir) {
    TypeSpec message = TypeSpec.classBuilder(RPC_ERROR_CLASS_NAME)
            .addField(FieldSpec.builder(int.class, "code")
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            .addField(FieldSpec.builder(String.class, "message")
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            // default constructor
            .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .build())
            // all args constructor
            .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(int.class, "code")
                    .addParameter(String.class, "message")

                    .addCode(CodeBlock.builder()
                            .addStatement("this.$1L = $1L", "code")
                            .addStatement("this.$1L = $1L", "message")
                            .build())
                    .build())
            .addModifiers(Modifier.PUBLIC)
            .build();

    JavaFile javaFile = JavaFile.builder(RPC_ERROR_CLASS_NAME.packageName(), message)
            .build();

    try {
      javaFile.writeTo(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("Error generating RpcError", e);
    }
  }
}

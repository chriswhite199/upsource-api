package com.github.chriswhite199.upsourceapi.schema;

import com.fasterxml.jackson.databind.ObjectMapper;

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

  public static void main(final String args[]) {
    final var inputDir = new File(args[0]);
    final var outputDir = new File(args[1]);

    outputDir.mkdirs();
    final var objMapper = new ObjectMapper();
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
}

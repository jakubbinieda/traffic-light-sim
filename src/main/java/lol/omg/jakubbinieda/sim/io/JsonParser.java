package lol.omg.jakubbinieda.sim.io;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class JsonParser {
  public static final JsonParser INSTANCE = new JsonParser();

  private final ObjectMapper mapper;

  private JsonParser() {
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
    printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    printer =
        printer.withSeparators(DefaultPrettyPrinter.DEFAULT_SEPARATORS.withArrayEmptySeparator(""));

    this.mapper = new ObjectMapper();
    mapper.setDefaultPrettyPrinter(printer);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public static JsonParser getInstance() {
    return INSTANCE;
  }

  public SimulationInput readInput(Path path) throws IOException {
    Objects.requireNonNull(path, "path cannot be null");

    return mapper.readValue(path.toFile(), SimulationInput.class);
  }

  public void writeOutput(Path path, SimulationOutput output) throws IOException {
    Objects.requireNonNull(path, "path cannot be null");
    Objects.requireNonNull(output, "output cannot be null");

    mapper.writeValue(path.toFile(), output);
  }
}

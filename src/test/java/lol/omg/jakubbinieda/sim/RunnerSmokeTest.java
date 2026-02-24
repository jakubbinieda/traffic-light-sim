package lol.omg.jakubbinieda.sim;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class RunnerSmokeTest {
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  @TempDir Path tempDir;
  private ByteArrayOutputStream capturedOut;
  private ByteArrayOutputStream capturedErr;

  @BeforeEach
  void setup() {
    capturedOut = new ByteArrayOutputStream();
    capturedErr = new ByteArrayOutputStream();
    System.setOut(new PrintStream(capturedOut));
    System.setErr(new PrintStream(capturedErr));
  }

  @AfterEach
  void teardown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "input.json", "input.json output.json extra"})
  @DisplayName("Wrong number of arguments fails")
  public void Wrong_number_of_arguments_fails(String args) {
    assertDoesNotThrow(() -> Runner.main(args.split(" ")));
    assertEquals("Invalid number of arguments", capturedErr.toString().trim());
  }

  @Test
  @DisplayName("Non-existent input file prints error")
  void Non_existent_input_file_prints_error() {
    Path output = tempDir.resolve("output.json");
    Runner.main(new String[] {"/no/such/file.json", output.toString()});
    assertTrue(capturedErr.toString().contains("Error reading input"));
  }

  @Test
  @DisplayName("Invalid JSON input prints error")
  void Invalid_JSON_input_prints_error() throws Exception {
    Path input = tempDir.resolve("bad.json");
    Files.writeString(input, "not json at all");
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});
    assertTrue(capturedErr.toString().contains("Error reading input"));
  }

  @Test
  @DisplayName("Empty commands produces empty output")
  void Empty_commands_produces_empty_output() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(input, "{\"commands\": []}");
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});

    assertTrue(Files.exists(output));
    String content = Files.readString(output);
    assertTrue(content.contains("stepStatuses"));
  }

  @Test
  @DisplayName("Step-only input produces output with one stepStatus")
  void Step_only_input_produces_output_with_one_stepStatus() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(
        input,
        """
        {"commands": [{"type": "step"}]}
        """);
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});

    assertTrue(Files.exists(output));
    String content = Files.readString(output);
    assertTrue(content.contains("leftVehicles"));
  }

  @Test
  @DisplayName("Full run does not crash")
  void full_scenario_vehicles_leave() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(
        input,
        """
        {
          "commands": [
            {"type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north"},
            {"type": "addVehicle", "vehicleId": "v2", "startRoad": "north", "endRoad": "south"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"}
          ]
        }
        """);
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});

    assertTrue(Files.exists(output));
  }

  @Test
  @DisplayName("Output file is valid")
  void Output_file_is_valid() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(
        input,
        """
        {"commands": [{"type": "step"}, {"type": "step"}]}
        """);
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});

    String content = Files.readString(output);
    assertTrue(content.trim().startsWith("{"));
    assertTrue(content.contains("stepStatuses"));
  }

  @Test
  @DisplayName("Unwritable output path prints error")
  void Unwritable_output_path_prints_error() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(
        input,
        """
        {"commands": [{"type": "step"}]}
        """);
    Path output = tempDir.resolve("no").resolve("such").resolve("dir").resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});
    assertTrue(capturedErr.toString().contains("Error writing output"));
  }

  @Test
  @DisplayName("Adding vehicles works")
  void Adding_vehicles_work() throws Exception {
    Path input = tempDir.resolve("input.json");
    Files.writeString(
        input,
        """
        {
          "commands": [
            {"type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"},
            {"type": "step"}
          ]
        }
        """);
    Path output = tempDir.resolve("output.json");

    Runner.main(new String[] {input.toString(), output.toString()});

    String content = Files.readString(output);
    assertTrue(content.contains("v1"));
  }

  @Test
  @DisplayName("Private constructor for pitest ;)")
  void Private_constructor_for_pitest() throws Exception {
    var ctor = Runner.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    assertNotNull(ctor.newInstance());
  }
}

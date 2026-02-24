package lol.omg.jakubbinieda.sim.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lol.omg.jakubbinieda.sim.io.commands.AddVehicleCommand;
import lol.omg.jakubbinieda.sim.io.commands.StepCommand;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonParserTest {
  private final JsonParser parser = JsonParser.getInstance();

  @Test
  @DisplayName("There is only one instance")
  void There_is_only_one_instance() {
    JsonParser instance1 = JsonParser.getInstance();
    JsonParser instance2 = JsonParser.getInstance();
    assertSame(instance1, instance2);
  }

  @Nested
  class ReadTest {
    @Test
    @DisplayName("Reading throws NullPointerException on null path")
    void Reading_throws_NullPointerException_on_null_path() {
      Exception e = assertThrows(NullPointerException.class, () -> parser.readInput(null));
      assertEquals("path cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Reading throws IOException on nonexistent file")
    void Reading_throws_IOException_on_nonexistent_file() {
      assertThrows(IOException.class, () -> parser.readInput(Path.of("fsahifhsaui.json")));
    }

    @Test
    @DisplayName("Reading throws IOException on unknown command")
    void Reading_throws_IOException_on_unknown_command_type(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          {
            "commands": [
              { "type": "explode" }
            ]
          }
          """);

      assertThrows(IOException.class, () -> parser.readInput(file));
    }

    @Test
    @DisplayName("Reading throws IOException on invalid JSON")
    void Reading_throws_IOException_on_invalid_json(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(file, "not json at all");

      assertThrows(IOException.class, () -> parser.readInput(file));
    }

    @Test
    @DisplayName("Reading throws IOException on invalid direction")
    void Reading_throws_IOException_on_invalid_direction(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          {
            "commands": [
              { "type": "addVehicle", "vehicleId": "v1", "startRoad": "diagonal", "endRoad": "north" }
            ]
          }
          """);

      assertThrows(Exception.class, () -> parser.readInput(file));
    }

    @Test
    @DisplayName("Parses adding vehicle")
    void Parses_adding_vehicle(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          {
            "commands": [
              {
                "type": "addVehicle",
                "vehicleId": "v1",
                "startRoad": "south",
                "endRoad": "north"
              }
            ]
          }
          """);

      SimulationInput input = parser.readInput(file);

      assertEquals(1, input.commands().size());
      assertInstanceOf(AddVehicleCommand.class, input.commands().getFirst());
      AddVehicleCommand cmd = (AddVehicleCommand) input.commands().getFirst();
      assertEquals("v1", cmd.vehicleId());
      assertEquals(new Movement(Direction.SOUTH, Direction.NORTH), cmd.movement());
    }

    @Test
    @DisplayName("Parses step")
    void Parses_step(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          {
            "commands": [
              { "type": "step" }
            ]
          }
          """);

      SimulationInput input = parser.readInput(file);

      assertEquals(1, input.commands().size());
      assertInstanceOf(StepCommand.class, input.commands().getFirst());
    }

    @Test
    @DisplayName("Parses mixed commands in order")
    void Prases_mixed_commands_in_order(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          {
            "commands": [
              { "type": "addVehicle", "vehicleId": "v1", "startRoad": "south", "endRoad": "north" },
              { "type": "addVehicle", "vehicleId": "v2", "startRoad": "north", "endRoad": "south" },
              { "type": "step" },
              { "type": "step" },
              { "type": "addVehicle", "vehicleId": "v3", "startRoad": "west", "endRoad": "south" },
              { "type": "step" }
            ]
          }
          """);

      SimulationInput input = parser.readInput(file);

      assertEquals(6, input.commands().size());
      assertInstanceOf(AddVehicleCommand.class, input.commands().get(0));
      assertInstanceOf(AddVehicleCommand.class, input.commands().get(1));
      assertInstanceOf(StepCommand.class, input.commands().get(2));
      assertInstanceOf(StepCommand.class, input.commands().get(3));
      assertInstanceOf(AddVehicleCommand.class, input.commands().get(4));
      assertInstanceOf(StepCommand.class, input.commands().get(5));

      AddVehicleCommand v3 = (AddVehicleCommand) input.commands().get(4);
      assertEquals(new Movement(Direction.WEST, Direction.SOUTH), v3.movement());
    }

    @Test
    @DisplayName("Parses empty")
    void Parses_empty(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("input.json");
      Files.writeString(
          file,
          """
          { "commands": [] }
          """);

      SimulationInput input = parser.readInput(file);
      assertTrue(input.commands().isEmpty());
    }
  }

  @Nested
  class WriteOutputTest {
    @Test
    @DisplayName("Reading throws NullPointerException on null output")
    void Reading_throws_NullPointerException_on_null_output(@TempDir Path dir) {
      Exception e =
          assertThrows(
              NullPointerException.class, () -> parser.writeOutput(dir.resolve("out.json"), null));
      assertEquals("output cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Reading throws NullPointerException on null path")
    void Reading_throws_NullPointerException_on_null_path() {
      Exception e =
          assertThrows(
              NullPointerException.class,
              () -> parser.writeOutput(null, new SimulationOutput(List.of())));
      assertEquals("path cannot be null", e.getMessage());
    }

    @Test
    @DisplayName("Writes step statuses to file")
    void Writes_step_statuses_to_file(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("output.json");

      SimulationOutput output =
          new SimulationOutput(
              List.of(
                  new StepStatus(List.of("v1", "v2")),
                  new StepStatus(List.of()),
                  new StepStatus(List.of("v3"))));

      parser.writeOutput(file, output);

      String json = Files.readString(file);
      assertTrue(json.contains("\"stepStatuses\""));
      assertTrue(json.contains("\"leftVehicles\""));
      assertTrue(json.contains("\"v1\""));
      assertTrue(json.contains("\"v2\""));
      assertTrue(json.contains("\"v3\""));
    }

    @Test
    @DisplayName("Writes empty step statuses")
    void Writes_empty_step_statuses(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("output.json");

      SimulationOutput output = new SimulationOutput(List.of());

      parser.writeOutput(file, output);

      String json = Files.readString(file);
      assertTrue(json.contains("\"stepStatuses\""));
      assertTrue(json.contains("[]"));
    }
  }

  @Nested
  class RoundTripTest {
    @Test
    @DisplayName("Write then read produces equivalent output")
    void write_then_read_round_trip(@TempDir Path dir) throws IOException {
      Path file = dir.resolve("round.json");

      SimulationOutput original =
          new SimulationOutput(
              List.of(
                  new StepStatus(List.of("v1", "v2")),
                  new StepStatus(List.of()),
                  new StepStatus(List.of("v3")),
                  new StepStatus(List.of("v4"))));

      parser.writeOutput(file, original);

      SimulationOutput reread =
          new com.fasterxml.jackson.databind.ObjectMapper()
              .readValue(file.toFile(), SimulationOutput.class);

      assertEquals(4, reread.stepStatuses().size());
      assertEquals(List.of("v1", "v2"), reread.stepStatuses().get(0).leftVehicles());
      assertEquals(List.of(), reread.stepStatuses().get(1).leftVehicles());
      assertEquals(List.of("v3"), reread.stepStatuses().get(2).leftVehicles());
      assertEquals(List.of("v4"), reread.stepStatuses().get(3).leftVehicles());
    }
  }
}

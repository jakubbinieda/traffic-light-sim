package lol.omg.jakubbinieda.sim.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RoadTest {
  @Test
  @DisplayName("Construction throws NullPointerException when approach is null")
  public void Construction_throws_NullPointerException_when_approach_is_null() {
    Exception e =
        assertThrows(NullPointerException.class, () -> new Road(null, java.util.List.of()));
    assertEquals("approach cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws NullPointerException when lanes is null")
  public void Construction_throws_NullPointerException_when_lanes_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new Road(Direction.NORTH, null));
    assertEquals("lanes cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws IllegalArgumentException when lanes is empty")
  public void Construction_throws_IllegalArgumentException_when_lanes_is_empty() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new Road(Direction.NORTH, java.util.List.of()));
    assertEquals("Road must have at least one lane", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws IllegalArgumentException when lanes' ids are not unique")
  public void Construction_throws_IllegalArgumentException_when_lanes_ids_are_not_unique() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.NORTH)));
    Lane lane2 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));

    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> new Road(Direction.NORTH, java.util.List.of(lane1, lane2)));
    assertEquals("Lane IDs must be unique within a road", e.getMessage());
  }

  @Test
  @DisplayName(
      "Construction throws IllegalArgumentException when lane's approach does not match road's approach")
  public void Construction_throws_IllegalArgumentException_when_lane_approach_does_not_match() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.NORTH)));
    Lane lane2 =
        new Lane("lane2", Direction.SOUTH, Set.of(new Movement(Direction.SOUTH, Direction.SOUTH)));

    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> new Road(Direction.NORTH, java.util.List.of(lane1, lane2)));
    assertEquals(
        "Lane lane2 has approach SOUTH which does not match road approach NORTH", e.getMessage());
  }

  @Test
  @DisplayName("Getters work correctly")
  public void Getters_work_correctly() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.NORTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));

    Road road = new Road(Direction.NORTH, java.util.List.of(lane1, lane2));
    assertEquals(Direction.NORTH, road.approach());
    assertEquals(java.util.List.of(lane1, lane2), road.lanes());
  }

  @Test
  @DisplayName("getAllMovements returns all movements allowed by the lanes")
  public void getAllMovements_returns_all_movements_allowed_by_lanes() {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.NORTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.EAST)));

    Road road = new Road(Direction.NORTH, java.util.List.of(lane1, lane2));
    assertEquals(
        Set.of(
            new Movement(Direction.NORTH, Direction.NORTH),
            new Movement(Direction.NORTH, Direction.EAST)),
        Set.copyOf(road.getAllMovements()));
  }

  @Test
  @DisplayName("getLanesAllowingMovement returns lanes that allow the specified movement")
  public void getLanesAllowingMovement_returns_lanes_that_allow_specified_movement()
      throws Exception {
    Lane lane1 =
        new Lane("lane1", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.NORTH)));
    Lane lane2 =
        new Lane("lane2", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));
    Lane lane3 =
        new Lane("lane3", Direction.NORTH, Set.of(new Movement(Direction.NORTH, Direction.SOUTH)));

    Road road = new Road(Direction.NORTH, java.util.List.of(lane1, lane2, lane3));
    assertEquals(java.util.List.of(lane2, lane3), road.getLanesAllowingMovement(Direction.SOUTH));
  }
}

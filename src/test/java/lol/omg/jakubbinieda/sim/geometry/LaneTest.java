package lol.omg.jakubbinieda.sim.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class LaneTest {
  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Construction throws NullPointerException when id is null")
  public void Construction_should_throw_NullPointerException_when_id_is_null(Direction approach) {
    Exception e =
        assertThrows(
            NullPointerException.class,
            () -> new Lane(null, approach, Set.of(new Movement(approach, approach))));
    assertEquals("id cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws NullPointerException when approach is null")
  public void Construction_throws_NullPointerException_when_approach_is_null() {
    Exception e =
        assertThrows(
            NullPointerException.class,
            () -> new Lane("lane1", null, Set.of(new Movement(Direction.NORTH, Direction.NORTH))));
    assertEquals("approach cannot be null", e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Construction throws IllegalArgumentException when allowedMovements is empty")
  public void Construction_throws_IllegalArgumentException_when_allowedMovements_is_empty(
      Direction approach) {
    Exception e =
        assertThrows(IllegalArgumentException.class, () -> new Lane("lane1", approach, Set.of()));
    assertEquals("Lane must allow at least one maneuver", e.getMessage());
  }

  @Test
  @DisplayName(
      "Construction throws IllegalArgumentException when allowedMovements contains incompatible movement")
  public void
      Construction_throws_IllegalArgumentException_when_allowedMovements_contains_incompatible_movement() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new Lane(
                    "lane1",
                    Direction.NORTH,
                    Set.of(new Movement(Direction.SOUTH, Direction.SOUTH))));
    assertEquals(
        "Maneuver Movement[from=SOUTH, to=SOUTH] is not compatible with lane approach NORTH",
        e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("allowsMovement returns true for allowed movement")
  public void AllowsMovement_returns_true_for_allowed_movement(Direction approach) {
    Movement movement = new Movement(approach, approach);
    Lane lane = new Lane("lane1", approach, Set.of(movement));
    assertTrue(lane.allowsMovement(movement));
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("allowsMovement returns false for disallowed movement")
  public void AllowsMovement_returns_false_for_disallowed_movement(Direction approach) {
    Movement movement = new Movement(approach, approach.opposite());
    Lane lane = new Lane("lane1", approach, Set.of(new Movement(approach, approach)));
    assertFalse(lane.allowsMovement(movement));
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Getters returns correct values")
  public void Getters_returns_correct_values(Direction approach) {
    Lane lane =
        new Lane(
            "lane1",
            approach,
            Set.of(new Movement(approach, approach), new Movement(approach, approach.opposite())));
    assertEquals("lane1", lane.id());
    assertEquals(approach, lane.approach());
    assertEquals(
        Set.of(new Movement(approach, approach), new Movement(approach, approach.opposite())),
        lane.allowedMovements());
  }
}

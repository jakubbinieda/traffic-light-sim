package lol.omg.jakubbinieda.sim.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class MovementTest {
  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Construction throws NullPointerException when from is null")
  public void Construction_should_throw_NullPointerException_when_from_is_null(Direction to) {
    Exception e = assertThrows(NullPointerException.class, () -> new Movement(null, to));
    assertEquals("from cannot be null", e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Construction throws NullPointerException when to is null")
  public void Construction_should_throw_NullPointerException_when_to_is_null(Direction from) {
    Exception e = assertThrows(NullPointerException.class, () -> new Movement(from, null));
    assertEquals("to cannot be null", e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Turn type is U_TURN when from and to are the same")
  public void Turn_type_should_be_U_TURN_when_from_and_to_are_the_same(Direction direction) {
    Movement movement = new Movement(direction, direction);
    assertEquals(TurnType.U_TURN, movement.getTurnType());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Turn type is STRAIGHT when to is opposite of from")
  public void Turn_type_should_be_STRAIGHT_when_to_is_opposite_of_from(Direction from) {
    Movement movement = new Movement(from, from.opposite());
    assertEquals(TurnType.STRAIGHT, movement.getTurnType());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Turn type is LEFT when to is clockwise of from")
  public void Turn_type_should_be_LEFT_when_to_is_clockwise_of_from(Direction from) {
    Movement movement = new Movement(from, from.clockwise());
    assertEquals(TurnType.LEFT, movement.getTurnType());
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  @DisplayName("Turn type is RIGHT when to is counterclockwise of from")
  public void Turn_type_should_be_RIGHT_when_to_is_counterclockwise_of_from(Direction from) {
    Movement movement = new Movement(from, from.counterClockwise());
    assertEquals(TurnType.RIGHT, movement.getTurnType());
  }
}

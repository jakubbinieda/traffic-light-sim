package lol.omg.jakubbinieda.sim.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lol.omg.jakubbinieda.sim.model.Vehicle.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class VehicleTest {
  @Test
  @DisplayName("Construction throws NullPointerException when id is null")
  public void Construction_throws_NullPointerException_when_id_is_null() {
    Exception e =
        assertThrows(
            NullPointerException.class,
            () -> new Vehicle(null, new Movement(Direction.NORTH, Direction.SOUTH)));
    assertEquals("id cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws NullPointerException when movement is null")
  public void Construction_throws_NullPointerException_when_movement_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new Vehicle("vehicle1", null));
    assertEquals("movement cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Initial values are set correctly")
  public void Initial_values_are_set_correctly() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));

    assertEquals("vehicle1", vehicle.getId());
    assertEquals(new Movement(Direction.NORTH, Direction.SOUTH), vehicle.getMovement());
    assertEquals(State.QUEUED, vehicle.getState());
    assertEquals(0, vehicle.getWaitTime());
    assertEquals(0, vehicle.getCrossingTimeRemaining());
  }

  @Test
  @DisplayName("Cannot start crossing when state is not QUEUED")
  public void Cannot_start_crossing_when_state_is_not_QUEUED() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(5);

    Exception e = assertThrows(IllegalStateException.class, () -> vehicle.startCrossing(5));
    assertEquals("Cannot start crossing when state is CROSSING", e.getMessage());
  }

  @Test
  @DisplayName("Cannot start crossing with non-positive crossing time")
  public void Cannot_start_crossing_with_non_positive_crossing_time() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));

    Exception e = assertThrows(IllegalArgumentException.class, () -> vehicle.startCrossing(0));
    assertEquals("Crossing time must be positive", e.getMessage());

    e = assertThrows(IllegalArgumentException.class, () -> vehicle.startCrossing(-1));
    assertEquals("Crossing time must be positive", e.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 10, Integer.MAX_VALUE})
  @DisplayName("State changes to CROSSING and crossing time is set when startCrossing is called")
  public void State_changes_to_CROSSING_and_crossing_time_is_set_when_startCrossing_is_called(
      int crossingTime) {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(crossingTime);

    assertEquals(State.CROSSING, vehicle.getState());
    assertEquals(crossingTime, vehicle.getCrossingTimeRemaining());
  }

  @Test
  @DisplayName("Incrementing wait time when state is CROSSING throws IllegalStateException")
  public void Increment_wait_time_when_state_is_CROSSING_throws_IllegalStateException() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(5);

    Exception e = assertThrows(IllegalStateException.class, vehicle::incrementWaitTime);
    assertEquals("Cannot increment wait time when state is CROSSING", e.getMessage());
  }

  @Test
  @DisplayName("Incrementing wait time when state is EXITED throws IllegalStateException")
  public void Incrementing_wait_time_when_state_is_EXITED_throws_IllegalStateException() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(1);
    vehicle.tickCrossing();

    Exception e = assertThrows(IllegalStateException.class, vehicle::incrementWaitTime);
    assertEquals("Cannot increment wait time when state is EXITED", e.getMessage());
  }

  @Test
  @DisplayName("Incrementing wait time when state is QUEUED increments wait time")
  public void Incrementing_wait_time_when_state_is_QUEUED_increments_wait_time() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.incrementWaitTime();
    assertEquals(1, vehicle.getWaitTime());
    vehicle.incrementWaitTime();
    vehicle.incrementWaitTime();
    assertEquals(3, vehicle.getWaitTime());
  }

  @Test
  @DisplayName("Ticking crossing when state is QUEUED throws IllegalStateException")
  public void Ticking_crossing_when_state_is_QUEUED_throws_IllegalStateException() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));

    Exception e = assertThrows(IllegalStateException.class, vehicle::tickCrossing);
    assertEquals("Cannot tick crossing when state is QUEUED", e.getMessage());
  }

  @Test
  @DisplayName("Ticking crossing when state is EXITED throws IllegalStateException")
  public void Ticking_crossing_when_state_is_EXITED_throws_IllegalStateException() {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(1);
    vehicle.tickCrossing();

    Exception e = assertThrows(IllegalStateException.class, vehicle::tickCrossing);
    assertEquals("Cannot tick crossing when state is EXITED", e.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 15})
  @DisplayName(
      "Ticking crossing decrements crossing time and changes state to EXITED when crossing time reaches 0")
  public void
      Ticking_crossing_decrements_crossing_time_and_changes_state_to_EXITED_when_crossing_time_reaches_0(
          int crossingTime) {
    Vehicle vehicle = new Vehicle("vehicle1", new Movement(Direction.NORTH, Direction.SOUTH));
    vehicle.startCrossing(crossingTime);
    for (int i = 0; i < crossingTime - 1; i++) {
      boolean exited = vehicle.tickCrossing();
      assertEquals(State.CROSSING, vehicle.getState());
      assertEquals(crossingTime - 1 - i, vehicle.getCrossingTimeRemaining());
      assertFalse(exited);
    }

    assertTrue(vehicle.tickCrossing());
    assertEquals(State.EXITED, vehicle.getState());
  }
}

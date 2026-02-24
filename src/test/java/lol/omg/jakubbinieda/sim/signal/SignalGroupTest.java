package lol.omg.jakubbinieda.sim.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SignalGroupTest {
  @Test
  @DisplayName("Construction throws NullPointerException when id is null")
  void Construction_throws_NullPointerException_when_id_is_null() {
    Exception e =
        assertThrows(
            NullPointerException.class,
            () -> new SignalGroup(null, Set.of(new Movement(Direction.EAST, Direction.NORTH))));
    assertEquals("id cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws NullPointerException when movements is null")
  void Construction_throws_NullPointerException_when_movements_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new SignalGroup("group1", null));
    assertEquals("movements cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws IllegalArgumentException when movements is empty")
  void Construction_throws_NullPointerException_when_movements_is_empty() {
    Exception e =
        assertThrows(IllegalArgumentException.class, () -> new SignalGroup("group1", Set.of()));
    assertEquals("Signal group must control at least one movement", e.getMessage());
  }

  @Test
  @DisplayName("Getters work")
  void Getters_work() {
    SignalGroup group =
        new SignalGroup("group1", Set.of(new Movement(Direction.EAST, Direction.NORTH)));
    assertEquals("group1", group.id());
    assertEquals(1, group.movements().size());
  }
}

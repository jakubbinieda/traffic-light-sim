package lol.omg.jakubbinieda.sim.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class SignalCommandTest {
  @ParameterizedTest
  @EnumSource(SignalState.class)
  @DisplayName("Construction throws IllegalArgumentException when signalGroupId is null")
  void Construction_throws_IllegalArgumentException_when_signalGroupId_is_null(SignalState state) {
    Exception e = assertThrows(NullPointerException.class, () -> new SignalCommand(null, state));
    assertEquals("signalGroupId cannot be null", e.getMessage());
  }

  @Test
  @DisplayName("Construction throws IllegalArgumentException when newState is null")
  void Construction_throws_IllegalArgumentException_when_newState_is_null() {
    Exception e = assertThrows(NullPointerException.class, () -> new SignalCommand("group1", null));
    assertEquals("newState cannot be null", e.getMessage());
  }

  @ParameterizedTest
  @EnumSource(SignalState.class)
  @DisplayName("Getters work")
  void Getters_work(SignalState state) {
    SignalCommand command = new SignalCommand("group1", state);
    assert command.signalGroupId().equals("group1");
    assert command.newState() == state;
  }
}

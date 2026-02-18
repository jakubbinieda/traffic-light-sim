package lol.omg.jakubbinieda.sim;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class RunnerSmokeTest {
  @Test
  void main_doesNotThrow() {
    assertDoesNotThrow(() -> Runner.main(new String[0]));
  }
}

package lol.omg.jakubbinieda.sim.signal;

import java.util.Objects;

public record SignalCommand(String signalGroupId, SignalState newState) {
  public SignalCommand {
    Objects.requireNonNull(signalGroupId, "signalGroupId cannot be null");
    Objects.requireNonNull(newState, "newState cannot be null");
  }
}

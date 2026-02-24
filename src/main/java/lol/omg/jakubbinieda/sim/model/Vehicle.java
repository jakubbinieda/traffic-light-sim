package lol.omg.jakubbinieda.sim.model;

import java.util.Objects;

public class Vehicle {
  private final String id;
  private final Movement movement;
  private int crossingTimeRemaining;
  private State state;
  private int waitTime;

  public Vehicle(String id, Movement movement) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.movement = Objects.requireNonNull(movement, "movement cannot be null");

    this.state = State.QUEUED;
    this.waitTime = 0;
    this.crossingTimeRemaining = 0;
  }

  public String getId() {
    return id;
  }

  public Movement getMovement() {
    return movement;
  }

  public State getState() {
    return state;
  }

  public int getWaitTime() {
    return waitTime;
  }

  public int getCrossingTimeRemaining() {
    return crossingTimeRemaining;
  }

  public void incrementWaitTime() {
    if (state != State.QUEUED) {
      throw new IllegalStateException("Cannot increment wait time when state is " + state);
    }
    waitTime++;
  }

  public void startCrossing(int crossingTime) {
    if (state != State.QUEUED) {
      throw new IllegalStateException("Cannot start crossing when state is " + state);
    }
    if (crossingTime <= 0) {
      throw new IllegalArgumentException("Crossing time must be positive");
    }

    this.state = State.CROSSING;
    this.crossingTimeRemaining = crossingTime;
  }

  public boolean tickCrossing() {
    if (state != State.CROSSING) {
      throw new IllegalStateException("Cannot tick crossing when state is " + state);
    }

    crossingTimeRemaining--;
    if (crossingTimeRemaining == 0) {
      state = State.EXITED;
      return true;
    }

    return false;
  }

  public enum State {
    QUEUED,
    CROSSING,
    EXITED
  }
}

package lol.omg.jakubbinieda.sim.engine.loadbalancer;

import java.util.Objects;
import java.util.Random;
import lol.omg.jakubbinieda.sim.geometry.Lane;

public class RandomBalancer implements LoadBalancer {
  private final Random random;

  public RandomBalancer() {
    this(new Random());
  }

  public RandomBalancer(Random random) {
    this.random = Objects.requireNonNull(random, "random cannot be null");
  }

  @Override
  public Lane selectLane(java.util.List<Lane> lanes) {
    Objects.requireNonNull(lanes, "lanes cannot be null");
    if (lanes.isEmpty()) {
      throw new IllegalArgumentException("lanes cannot be empty");
    }

    return lanes.get(random.nextInt(lanes.size()));
  }
}

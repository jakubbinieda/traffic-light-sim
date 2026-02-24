package lol.omg.jakubbinieda.sim.engine.loadbalancer;

import java.util.List;
import lol.omg.jakubbinieda.sim.geometry.Lane;

public interface LoadBalancer {
  Lane selectLane(List<Lane> lanes);
}

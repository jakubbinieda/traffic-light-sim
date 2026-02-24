package lol.omg.jakubbinieda.sim.Factories;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lol.omg.jakubbinieda.sim.controller.Controller;
import lol.omg.jakubbinieda.sim.engine.Intersection;
import lol.omg.jakubbinieda.sim.engine.loadbalancer.LoadBalancer;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.geometry.Lane;
import lol.omg.jakubbinieda.sim.geometry.Road;
import lol.omg.jakubbinieda.sim.model.Direction;
import lol.omg.jakubbinieda.sim.model.Movement;
import lol.omg.jakubbinieda.sim.signal.SignalGroup;

public class SimpleIntersectionFactory {
  public static final SimpleIntersectionFactory INSTANCE = new SimpleIntersectionFactory();

  public static SimpleIntersectionFactory getInstance() {
    return INSTANCE;
  }

  public Intersection supply(Controller controller, LoadBalancer loadBalancer) {
    Map<Direction, Road> roads = new EnumMap<>(Direction.class);
    List<SignalGroup> signalGroups = new ArrayList<>();

    for (Direction from : Direction.values()) {
      Set<Movement> movements = new HashSet<>();
      for (Direction to : Direction.values()) {
        movements.add(new Movement(from, to));
      }

      Lane lane = new Lane(from.name() + "-0", from, movements);
      roads.put(from, new Road(from, List.of(lane)));
      signalGroups.add(new SignalGroup("sg-" + from.name().toLowerCase(), movements));
    }

    IntersectionLayout layout = new IntersectionLayout(roads, signalGroups);
    controller.initialize(layout);
    return new Intersection(layout, controller, loadBalancer);
  }
}

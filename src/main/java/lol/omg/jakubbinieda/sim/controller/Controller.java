package lol.omg.jakubbinieda.sim.controller;

import java.util.List;
import lol.omg.jakubbinieda.sim.engine.IntersectionState;
import lol.omg.jakubbinieda.sim.geometry.IntersectionLayout;
import lol.omg.jakubbinieda.sim.signal.SignalCommand;

public interface Controller {
  void initialize(IntersectionLayout layout);

  List<SignalCommand> decide(IntersectionState state);
}

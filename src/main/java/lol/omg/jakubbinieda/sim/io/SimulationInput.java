package lol.omg.jakubbinieda.sim.io;

import java.util.List;
import lol.omg.jakubbinieda.sim.io.commands.Command;

public record SimulationInput(List<Command> commands) {}

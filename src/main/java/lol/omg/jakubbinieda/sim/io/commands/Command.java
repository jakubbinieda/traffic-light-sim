package lol.omg.jakubbinieda.sim.io.commands;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AddVehicleCommand.class, name = "addVehicle"),
  @JsonSubTypes.Type(value = StepCommand.class, name = "step")
})
public sealed interface Command permits AddVehicleCommand, StepCommand {}

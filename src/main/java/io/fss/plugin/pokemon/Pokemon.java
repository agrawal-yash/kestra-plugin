package io.fss.plugin.pokemon;

import java.util.*;
import lombok.*;

@Data
public class Pokemon {
    List<DetailedAbility> abilities;
    long base_experience;
    long height;
    List<DetailedMove> moves;
}

@Data
class DetailedAbility {
    Ability ability;
}

@Data
class Ability {
    String name;
}

@Data
class DetailedMove {
    Move move;
}

@Data
class Move {
    String name;
}
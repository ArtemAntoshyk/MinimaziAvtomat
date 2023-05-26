import java.util.*;

public class FiniteAutomatonMinimizer {

    private static class State {
        String name;
        boolean isStart;
        boolean isFinal;

        State(String name, boolean isStart, boolean isFinal) {
            this.name = name;
            this.isStart = isStart;
            this.isFinal = isFinal;
        }
    }

    private static class Transition {
        State fromState;
        char symbol;
        State toState;

        Transition(State fromState, char symbol, State toState) {
            this.fromState = fromState;
            this.symbol = symbol;
            this.toState = toState;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Введення станів через кому
        System.out.print("Введіть стани (через кому): ");
        String[] stateNames = scanner.nextLine().split(",");
        List<State> states = new ArrayList<>();
        for (String stateName : stateNames) {
            states.add(new State(stateName.trim(), false, false));
        }

        // Визначення початкового та кінцевого станів
        System.out.print("Введіть початковий стан: ");
        String startStateName = scanner.nextLine().trim();
        System.out.print("Введіть кінцеві стани (через кому): ");
        String[] finalStateNames = scanner.nextLine().split(",");

        for (State state : states) {
            if (state.name.equals(startStateName)) {
                state.isStart = true;
            }

            for (String finalStateName : finalStateNames) {
                if (state.name.equals(finalStateName.trim())) {
                    state.isFinal = true;
                    break;
                }
            }
        }

        // Введення переходів у форматі стан1-символ-стан2
        System.out.println("Введіть переходи (стан1-символ-стан2): ");
        List<Transition> transitions = new ArrayList<>();
        String input;
        while (!(input = scanner.nextLine()).isEmpty()) {
            String[] parts = input.split("-");
            String fromStateName = parts[0].trim();
            char symbol = parts[1].trim().charAt(0);
            String toStateName = parts[2].trim();

            State fromState = getStateByName(states, fromStateName);
            State toState = getStateByName(states, toStateName);

            transitions.add(new Transition(fromState, symbol, toState));
        }

        // Мінімізація скінченого автомата
        List<Set<State>> partition = minimize(states, transitions);

        // Виведення мінімізованого автомата
        System.out.println("\nМінімізований автомат:");
        System.out.println("Стани:");
        for (Set<State> group : partition) {
            for (State state : group) {
                System.out.print(state.name);
                if (state.isStart) {
                    System.out.print(" (початковий)");
                }
                if (state.isFinal) {
                    System.out.print(" (кінцевий)");
                }
                System.out.print("\t");
            }
            System.out.println();
        }

        System.out.println("Переходи:");
        for (Set<State> group : partition) {
            for (State state : group) {
                for (Transition transition : transitions) {
                    if (transition.fromState == state) {
                        System.out.println(state.name + " --" + transition.symbol + "--> " + transition.toState.name);
                    }
                }
            }
        }
    }

    private static State getStateByName(List<State> states, String name) {
        for (State state : states) {
            if (state.name.equals(name)) {
                return state;
            }
        }
        return null;
    }

    private static List<Set<State>> minimize(List<State> states, List<Transition> transitions) {
        List<Set<State>> partition = new ArrayList<>();
        Set<State> finalStates = new HashSet<>();
        Set<State> nonFinalStates = new HashSet<>();

        for (State state : states) {
            if (state.isFinal) {
                finalStates.add(state);
            } else {
                nonFinalStates.add(state);
            }
        }

        partition.add(finalStates);
        partition.add(nonFinalStates);

        boolean changed;

        do {
            changed = false;
            List<Set<State>> newPartition = new ArrayList<>();

            for (Set<State> group : partition) {
                List<Set<State>> subgroups = splitGroup(group, partition, transitions);
                newPartition.addAll(subgroups);

                if (subgroups.size() > 1) {
                    changed = true;
                }
            }

            partition = newPartition;

        } while (changed);

        return partition;
    }

    private static List<Set<State>> splitGroup(Set<State> group, List<Set<State>> partition, List<Transition> transitions) {
        List<Set<State>> subgroups = new ArrayList<>();

        for (char symbol : getAlphabet(transitions)) {
            List<Set<State>> newSubgroups = new ArrayList<>();

            for (Set<State> subgroup : subgroups) {
                List<State> statesToMove = new ArrayList<>();

                for (State state : subgroup) {
                    for (Transition transition : transitions) {
                        if (transition.fromState == state && transition.symbol == symbol) {
                            State toState = transition.toState;

                            for (Set<State> otherGroup : partition) {
                                if (otherGroup.contains(toState)) {
                                    statesToMove.add(state);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!statesToMove.isEmpty()) {
                    subgroup.removeAll(statesToMove);
                    newSubgroups.add(new HashSet<>(statesToMove));
                }
            }

            subgroups.addAll(newSubgroups);
        }

        subgroups.addAll(Collections.singleton(group));

        return subgroups;
    }

    private static Set<Character> getAlphabet(List<Transition> transitions) {
        Set<Character> alphabet = new HashSet<>();
        for (Transition transition : transitions) {
            alphabet.add(transition.symbol);
        }
        return alphabet;
    }
}

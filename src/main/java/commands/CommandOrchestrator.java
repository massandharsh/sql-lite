package commands;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommandOrchestrator {
    private final List<Commands<?>> commandsList;

    public CommandOrchestrator(List<Commands<?>> commandsList) {
        this.commandsList = commandsList;
    }

    public Object execute(String command) {
        Commands<?> commands = commandsList.stream()
                .filter(cmd -> cmd.checkEqual(command))
                .findFirst()
                .orElseThrow(RuntimeException::new);
        try {
            return commands.invoke(command);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

package commands;

import enums.CommandEnum;

import java.util.Arrays;

public interface Commands<T>{
    default boolean checkEqual(String command){
        return this.verifyCommand(command);
    }
    boolean verifyCommand(String command);
    T invoke(String command) throws Exception;
    default CommandEnum getCommandEnum(String command) {
        return Arrays.stream(CommandEnum.values()).filter((commandEnum) -> commandEnum.getCommand().equals(command)).findFirst().orElse(null);
    }

    default String[] commandExtractor(String command){
        return command.split("\\s");
    }
}

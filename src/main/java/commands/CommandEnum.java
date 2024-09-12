package commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    DB_INFO(".dbinfo");
    private final String command;
}

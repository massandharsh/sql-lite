package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    DB_INFO(".dbinfo"),
    TABLES(".tables");
    private final String command;
}

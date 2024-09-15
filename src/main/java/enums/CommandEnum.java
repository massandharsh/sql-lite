package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    DB_INFO(".dbinfo"),
    TABLES(".tables"),
    COUNT_FROM_TABLE("count");
    private final String command;
}

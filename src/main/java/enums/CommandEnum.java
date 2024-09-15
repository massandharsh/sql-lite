package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandEnum {
    DB_INFO(".dbinfo"),
    TABLES(".tables"),
    COUNT_FROM_TABLE("count"),
    SINGLE_COL_SELECT("select"),
    MULTI_COL_SELECT("multiSelect"),
    FILTER("filter");
    private final String command;
}

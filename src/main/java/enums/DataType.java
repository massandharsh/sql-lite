package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum DataType {
    NULL(""),
    INT8BIT("integer"),
    INT16BIT("integer16"),
    INT24BIT("integer24"),
    INT32BIT("integer32"),
    INT48BIT("integer48"),
    INT64BIT("integer64"),
    FLOAT64BIT("float"),
    PREDEFINED_INTEGER_ZERO("zero"),
    BLOB("blob"),
    TEXT("text");
    public final String dataType;
    public static DataType getDataType(String dataType) {
        return Arrays.stream(DataType.values()).filter(enumType -> enumType.dataType.equals(dataType)).findFirst().orElse(null);
    }
}

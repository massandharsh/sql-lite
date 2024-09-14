package enums;

import java.util.Arrays;


public enum CellFormatType {
    LEAF_CELL(0x0d),
    INTERIOR_CELL(0x05),
    LEAF_INDEX(0x0a),
    INTERIOR_INDEX(0x02);
    public final int value;
    CellFormatType(int i) {
        this.value = i;
    }
    public static CellFormatType getCellFormat(long i) {
        return Arrays.stream(CellFormatType.values()).filter((cellFormatType) -> cellFormatType.value == i).findFirst().orElse(null);
    }
}

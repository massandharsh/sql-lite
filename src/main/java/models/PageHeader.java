package models;
import enums.CellFormatType;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageHeader {
    private CellFormatType cellFormatType;
    private int noOfFreeBlocks;
    private int noOfCells;
    private int startOfCellContent;
    private int noOfFragmentedBytes;
    private Optional<Integer> rightMostPointer;
}

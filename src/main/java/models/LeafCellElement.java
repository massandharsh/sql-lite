package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Getter
public class LeafCellElement extends CellElement{
    private int totalPayload;
    private int initialPortion;
    private Optional<Integer> firstPageNumber;
}

package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Getter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class IndexElement extends CellFormat {
    private int bytesIncludingOverflow;
    private int initialPortionOfPayload;
    private Optional<Integer> firstPageNumber;
}

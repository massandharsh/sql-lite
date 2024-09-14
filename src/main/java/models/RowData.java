package models;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RowData {
    private List<ColumnDetails> columnDetailsList;
    private List<String> data;
}

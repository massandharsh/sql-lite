package utils;

import enums.DataType;
import models.ColumnDetails;
import models.Schema;
import java.util.List;

public class SchemaRelatedUtils {
    public static Schema getSqlLiteSchema(){
        List<ColumnDetails> columnDetails = List.of(
                new ColumnDetails(DataType.TEXT,"type"),
                new ColumnDetails(DataType.TEXT,"name"),
                new ColumnDetails(DataType.TEXT,"tbl_name"),
                new ColumnDetails(DataType.INT8BIT,"rootpage"),
                new ColumnDetails(DataType.TEXT,"text")
        );
        return new Schema(columnDetails);
    }
}

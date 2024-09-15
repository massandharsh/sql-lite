package strategy;

import models.RowData;

import java.util.List;

public interface DataExtractionStrategy{
    List<RowData> extractData(String fileName,String tableName);
}

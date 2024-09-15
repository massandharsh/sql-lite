package strategy;

import enums.DataType;
import models.ColumnDetails;
import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.Commons;
import utils.FileRelatedUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Commons.*;

public class ColDataExtractionStrategy implements DataExtractionStrategy {
    @Override
    public List<RowData> extractData(String fileName, String tblName) {
        int pageSize = FileRelatedUtils.getPageSize(fileName);
        List<RowData> rowData = Commons.getRowData(fileName,pageSize);
        String createQuery = rowData.stream()
                .filter(rowDatum -> {
                    int tblNameIndex = indexOfColumn(rowDatum, "tbl_name");
                    return tblNameIndex >= 0 && rowDatum.getData().get(tblNameIndex).equals(tblName);
                })
                .map(rowDatum -> rowDatum.getData().get(indexOfColumn(rowDatum, "text")))
                .findFirst()
                .orElse("");
        Pattern pattern1 = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher1 = pattern1.matcher(createQuery);
        String cols = "";
        List<ColumnDetails> columnDetails = new ArrayList<>();
        while (matcher1.find()) {
            cols = matcher1.group(1);
        }
        String [] columns1 = cols.split(",");
        //Since we have cols we can do extraction using split
        for(String column : columns1){
            String [] singleCol = column.trim().split("\\s");
            String colName = singleCol[0].toLowerCase();
            String colType = singleCol[1].toLowerCase();
            columnDetails.add(new ColumnDetails(DataType.getDataType(colType),colName));
        }

        Schema schema = Schema.builder().columnDetails(columnDetails).build();
        int pageNumber =  getPageNumberForTableFromSchema(fileName, tblName,pageSize);
        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,pageNumber,pageSize);
        assert pageHeader != null;
        List<Integer> contentOffset = FileRelatedUtils.contentOffsetForAllTheTables(fileName,pageHeader.getCellFormatType(),pageHeader.getNoOfCells(),pageNumber,pageSize);
        List<RowData> tableRowContent = new ArrayList<>();
        for(int offset : contentOffset){
            tableRowContent.add(getRowRelatedDataLeafCell(fileName,schema,offset));
        }
        return tableRowContent;
    }
}

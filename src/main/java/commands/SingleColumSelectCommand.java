package commands;

import enums.DataType;
import models.ColumnDetails;
import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.Commons;
import utils.FileRelatedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Commons.getPageNumberForTableFromSchema;
import static utils.Commons.getRowRelatedDataLeafCell;

public class SingleColumSelectCommand implements Commands<String>{
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 5 && command.toLowerCase().contains("select") && !command.toLowerCase().contains("count");
    }

    private static int indexOfColumn(RowData rowDatum, String columnName) {
        return rowDatum.getColumnDetailsList().stream()
                .filter(cd -> columnName.equals(cd.getColumnName()))
                .mapToInt(rowDatum.getColumnDetailsList()::indexOf)
                .findFirst()
                .orElse(-1);
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 1];
        String columnName = commands[2];
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
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(createQuery);
        String cols = "";
        List<ColumnDetails> columnDetails = new ArrayList<>();
        while (matcher.find()) {
            cols = matcher.group(1);
        }
        String [] columns = cols.split(",");
        //Since we have cols we can do extraction using split
        for(String column : columns){
            String [] singleCol = column.trim().split("\\s");
            String colName = singleCol[0];
            String colType = singleCol[1];
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

        return tableRowContent.stream()
                .map(rowDatum -> {
                    int colIndex = indexOfColumn(rowDatum, columnName);
                    return colIndex >= 0 ? rowDatum.getData().get(colIndex) : null;
                })
                .filter(Objects::nonNull) // Remove any nulls if column not found
                .reduce("",(b,c)-> b + c + "\n").trim();


    }
}

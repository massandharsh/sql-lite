package commands;

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
import java.util.stream.Collectors;

import static utils.Commons.*;

public class MultipleColumnSelectCommand implements Commands<String>{
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length >= 5 && command.toLowerCase().contains("select") && !command.toLowerCase().contains("count")
                && command.contains(",");
    }
    private static String getColumnValue(RowData rowDatum, String columnName) {
        int colIndex = indexOfColumn(rowDatum, columnName);
        return colIndex >= 0 ? rowDatum.getData().get(colIndex) : "";
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 1];
        String regex = "select\\s+(\\S+(?:\\s*,\\s*\\S+)*)\\s+from";
        Pattern pattern = Pattern.compile(regex);
        String [] query = Arrays.stream(commands)
                .skip(1)
                .toArray(String[]::new);
        Matcher matcher = pattern.matcher(Arrays.stream(query).reduce("",(a,b)->a+" "+b).trim().toLowerCase());

        List<String> columns = new ArrayList<>();
        if(matcher.find()){
            String cols = matcher.group(1);
            columns.addAll(Arrays.asList(cols.trim().split(",")));
        }

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
                .map(rowDatum -> columns.stream()
                        .map(colName -> getColumnValue(rowDatum, colName))
                        .collect(Collectors.joining("|"))
                )
                .collect(Collectors.joining("\n"));
    }
}

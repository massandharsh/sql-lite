package commands;

import enums.CommandEnum;
import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.FileRelatedUtils;
import utils.SchemaRelatedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static utils.Commons.getRowRelatedDataLeafCell;

public class TablesCommand implements Commands<String>{
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 2 && getCommandEnum(commands[1]) == CommandEnum.TABLES;
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        int pageSize = FileRelatedUtils.getPageSize(fileName);

        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,1,4096);
        assert pageHeader != null;
        List<RowData> rowData = new ArrayList<>();
        List<Integer> contentOffset = FileRelatedUtils.contentOffsetForAllTheTables(fileName,pageHeader.getCellFormatType(),pageHeader.getNoOfCells(),1,pageSize);
        //Now we have to read content from the offset based on the type of schema
        //Schema for sqlite_schema
        Schema sqlLiteSchema = SchemaRelatedUtils.getSqlLiteSchema();
        for(int offset : contentOffset){
           rowData.add(getRowRelatedDataLeafCell(fileName,sqlLiteSchema,offset));
        }
        return rowData.stream()
                .flatMap(row -> row.getColumnDetailsList().stream()
                        .filter(column -> "tbl_name".equals(column.getColumnName()))
                        .map(column -> row.getData().get(row.getColumnDetailsList().indexOf(column))))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> String.join(" ", list)
                ));
    }
}

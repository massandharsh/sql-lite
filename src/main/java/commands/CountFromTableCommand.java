package commands;

import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.FileRelatedUtils;
import utils.SchemaRelatedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static utils.Commons.getRowRelatedDataLeafCell;

public class CountFromTableCommand implements Commands<String>{

    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 5 && (command.contains("COUNT") || command.contains("count"));
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            throw new RuntimeException("Invalid command");
        }
        String [] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 1];
        int pageSize = FileRelatedUtils.getPageSize(fileName);

        int pageNumber =  getPageNumber(fileName, tblName,pageSize);
        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,pageNumber,pageSize);
        assert pageHeader != null;
        return pageHeader.getNoOfCells()+"";
    }

    private static int getPageNumber(String fileName, String tblName,int pageSize) {

        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,1,pageSize);
        assert pageHeader != null;
        List<RowData> rowData = new ArrayList<>();
        List<Integer> contentOffset = FileRelatedUtils.contentOffsetForAllTheTables(fileName,pageHeader.getCellFormatType(),pageHeader.getNoOfCells(),1,pageSize);
        //Now we have to read content from the offset based on the type of schema
        //Schema for sqlite_schema
        Schema sqlLiteSchema = SchemaRelatedUtils.getSqlLiteSchema();
        for(int offset : contentOffset){
            rowData.add(getRowRelatedDataLeafCell(fileName,sqlLiteSchema,offset));
        }
        //We can get the root page for the corresponding table and query in that root page accordingly
        int pageNoForTable = rowData.stream()
                .flatMap(rowDatum -> {
                    // Get indices for tbl_name and rootpage columns
                    int tblNameIndex = rowDatum.getColumnDetailsList().stream()
                            .filter(cd -> "tbl_name".equals(cd.getColumnName()))
                            .mapToInt(rowDatum.getColumnDetailsList()::indexOf)
                            .findFirst()
                            .orElse(-1);

                    int rootPageIndex = rowDatum.getColumnDetailsList().stream()
                            .filter(cd -> "rootpage".equals(cd.getColumnName()))
                            .mapToInt(rowDatum.getColumnDetailsList()::indexOf)
                            .findFirst()
                            .orElse(-1);

                    // Ensure both indices are valid and tbl_name matches
                    if (tblNameIndex >= 0 && rootPageIndex >= 0 &&
                            rowDatum.getData().get(tblNameIndex).equals(tblName)) {
                        try {
                            return Optional.of(Integer.parseInt(rowDatum.getData().get(rootPageIndex))).stream();
                        } catch (NumberFormatException e) {
                            // If parsing fails, return empty stream
                            return Optional.<Integer>empty().stream();
                        }
                    }

                    return Optional.<Integer>empty().stream();
                })
                .findFirst()
                .orElse(-1);
        if(pageNoForTable == -1){
            throw new RuntimeException("Table not found");
        }
        return pageNoForTable;
    }
}

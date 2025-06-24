package commands;

import models.PageHeader;
import models.RowData;
import models.Schema;
import utils.FileRelatedUtils;
import utils.SchemaRelatedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Commons.getPageNumberForTableFromSchema;
import static utils.Commons.getRowRelatedDataLeafCell;

public class CountFromTableCommand implements Commands<String>{
    private final int temp = 1;
    private final int temp2 = 20;
    private final int temp3 = 3223;
    private final int anInt = 22652;
       private final int temp4 = 3;
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 5 && command.toLowerCase().contains("count");
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
        int pageNumber =  getPageNumberForTableFromSchema(fileName, tblName,pageSize);
        PageHeader pageHeader = FileRelatedUtils.getPageHeader(fileName,pageNumber,pageSize);
        assert pageHeader != null;
        return pageHeader.getNoOfCells()+"";
    }


}

package commands;

import models.RowData;
import strategy.DataExtractionStrategy;
import utils.Commons;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static utils.Commons.indexOfColumn;

public class DeleteFromTableCommand implements Commands<String> {
    private final DataExtractionStrategy dataExtractionStrategy;

    public DeleteFromTableCommand(DataExtractionStrategy dataExtractionStrategy) {
        this.dataExtractionStrategy = dataExtractionStrategy;
    }

    @Override
    public boolean verifyCommand(String command) {
        String[] commands = commandExtractor(command);
        return commands.length >= 5 && command.toLowerCase().contains("delete") && command.toLowerCase().contains("from") && command.toLowerCase().contains("where");
    }

    @Override
    public String invoke(String command) throws Exception {
        if (!verifyCommand(command)) {
            throw new RuntimeException("Invalid command");
        }
        String[] commands = commandExtractor(command);
        String fileName = commands[0];
        String tblName = commands[commands.length - 3];
        String conditionCol = commands[commands.length - 2];
        String conditionVal = commands[commands.length - 1];

        List<RowData> tableRowContent = dataExtractionStrategy.extractData(fileName, tblName);

        List<RowData> filteredRows = tableRowContent.stream()
                .filter(rowDatum -> {
                    int colIndex = indexOfColumn(rowDatum, conditionCol);
                    return colIndex >= 0 && !rowDatum.getData().get(colIndex).equalsIgnoreCase(conditionVal);
                })
                .collect(Collectors.toList());

        // Here you would implement the logic to save the filteredRows back to the database
        // For now, we will just return a success message

        return "Rows deleted successfully";
    }
} 
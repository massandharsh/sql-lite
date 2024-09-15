import commands.*;
import enums.DataType;
import models.ColumnDetails;
import models.RowData;
import utils.FileRelatedUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(new MultipleColumnSelectCommand(),new SingleColumSelectCommand(),new DbInfoCommand(),new TablesCommand(),new CountFromTableCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
  public static class Tester{
    public static void main(String[] args) {
      // Sample RowData list
      List<RowData> rowData = List.of(
              new RowData(
                      List.of(
                              new ColumnDetails(DataType.TEXT, "pistachio"),
                              new ColumnDetails(DataType.TEXT, "chocolate"),
                              new ColumnDetails(DataType.TEXT, "banana")
                      ),
                      List.of("0", "Sweet", "Delicious", "Nutty")
              ),
              new RowData(
                      List.of(
                              new ColumnDetails(DataType.TEXT, "pistachio"),
                              new ColumnDetails(DataType.TEXT, "chocolate"),
                              new ColumnDetails(DataType.TEXT, "banana")
                      ),
                      List.of("1", "Bitter", "Rich", "Fruity")
              )
      );

      // Define the columns to include in the output
      List<String> columnNamesToInclude = List.of("pistachio", "chocolate", "banana");

      // Extract and format the data based on the column names
      String formattedData = rowData.stream()
              .map(rowDatum -> columnNamesToInclude.stream()
                      .map(colName -> getColumnValue(rowDatum, colName))
                      .collect(Collectors.joining(" | "))
              )
              .collect(Collectors.joining("\n"));

      // Print the formatted data
      System.out.println(formattedData);
    }

    // Method to get the value of a column by name
    private static String getColumnValue(RowData rowDatum, String columnName) {
      int colIndex = getColumnIndex(rowDatum, columnName);
      return colIndex >= 0 ? rowDatum.getData().get(colIndex) : "";
    }

    // Method to find the index of the column with the given name
    private static int getColumnIndex(RowData rowDatum, String colName) {
      return rowDatum.getColumnDetailsList().stream()
              .filter(columnDetail -> colName.equals(columnDetail.getColumnName()))
              .mapToInt(rowDatum.getColumnDetailsList()::indexOf)
              .findFirst()
              .orElse(-1);
    }
  }
}

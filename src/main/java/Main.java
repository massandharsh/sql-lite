import commands.*;
import enums.DataType;
import models.ColumnDetails;
import models.RowData;
import strategy.ColDataExtractionStrategy;
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
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(
            new FilterCommand(new ColDataExtractionStrategy()),
            new MultipleColumnSelectCommand(new ColDataExtractionStrategy()),new SingleColumSelectCommand(new ColDataExtractionStrategy()),new DbInfoCommand(),new TablesCommand(),new CountFromTableCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
  public static class Tester {
    public static void main(String[] args) {
      // Define the SQL query
      String query = "SELECT name, color FROM apples WHERE color = 'Yellow'";

      // Define the regex pattern
      String regex = "SELECT\\s+(\\w+(?:\\s*,\\s*\\w+)*)\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(\\w+)\\s*=\\s*'([^']+)'";

      // Compile the pattern
      Pattern pattern = Pattern.compile(regex);

      // Create a matcher for the query
      Matcher matcher = pattern.matcher(query);

      // Check if the pattern matches
      if (matcher.find()) {
        // Get the total number of groups
        int groupCount = matcher.groupCount();

        // Extract the first group
        String firstGroup = matcher.group(1);

        // Extract the last group
        String lastGroup = matcher.group(groupCount);

        // Print the results
        System.out.println("First Group: " + firstGroup);
        System.out.println("Last Group: " + lastGroup);
        System.out.println(matcher.group(groupCount - 1));
      } else {
        System.out.println("No match found.");
      }
    }
  }
}

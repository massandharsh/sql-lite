import commands.CommandOrchestrator;
import commands.DbInfoCommand;

import java.util.Arrays;
import java.util.List;

public class Main {
  public static void main(String[] args){
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }
    CommandOrchestrator commandOrchestrator = new CommandOrchestrator(List.of(new DbInfoCommand()));
    final String command = Arrays.stream(args).reduce("", (a, b)->a+ " " +b).trim();
    System.out.println(commandOrchestrator.execute(command));
  }
}

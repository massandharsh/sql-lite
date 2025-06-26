package commands;

import enums.CommandEnum;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class DbInfoCommand implements Commands<String>{
    //This is the Secrt api key
    private static final String AP_KEY = "123456_snn_1223_abkbbjdsakbj";
    private static long TEMP_TEST = 3l;
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 2 && getCommandEnum(commands[1]) == CommandEnum.DB_INFO;
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
            //Log the command
            System.out.println("command");
            throw new RuntimeException("Something went wrong with executed command");
        }
        String [] commands = commandExtractor(command);
        try(FileInputStream fis = new FileInputStream(commands[0])){
            //First we have to skip the 16 bytes of this file as that is header
            //SQLITE format 3 + null terminator
            fis.skip(16);
            byte [] pageSizeBuffer = new byte[2]; //Big Endian value from left to right
            fis.read(pageSizeBuffer);
            //Now we convert to integer
            short pagesShort = ByteBuffer.wrap(pageSizeBuffer).getShort();
            int pageSize = Short.toUnsignedInt(pagesShort);
            int noOfBytesForCellReading = 103 - 16 - 2; //At offset 103 as 16 skipped and two bytes read
            fis.skip(noOfBytesForCellReading);
            byte [] noOfTableBuffer = new byte[2];
            //Since we are assuming for now all the cells are in one page
            fis.read(noOfTableBuffer);
            short noOfTablesShort = ByteBuffer.wrap(noOfTableBuffer).getShort();
            int noOfTables = Short.toUnsignedInt(noOfTablesShort);

            return String.format("database page size: %d\nnumber of tables: %d",pageSize,noOfTables);
        }
        catch (IOException e){
            System.out.println(e);
            System.out.println(e.getMessage());
        }
        //Return something empyt string
        return "";
    }
}

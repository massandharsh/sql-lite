package commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class DbInfoCommand implements Commands<String>{
    @Override
    public boolean verifyCommand(String command) {
        String [] commands = commandExtractor(command);
        return commands.length == 2 && getCommandEnum(commands[1]) == CommandEnum.DB_INFO;
    }

    @Override
    public String invoke(String command) throws Exception {
        if(!verifyCommand(command)){
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
            return String.format("database page size: %d",pageSize);
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
        return "";
    }
}



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.io.*;
import java.util.*;

public class Configuration {
    private HashMap map;
    private BufferedReader reader;
    public Configuration(String confFile)
    {
        this.map = new HashMap();
        try{
            reader = new BufferedReader(new FileReader(new File(confFile)));
        }
        catch(Exception e)
        {
            System.out.println("The Configuration File Could Not Be Found");
            System.exit(1);
        }
        String line = "";
        try{
            while((line = reader.readLine()) != null)
            {
                String keyvals[] = line.split("=");
                if(keyvals.length > 1)
                    map.put(keyvals[0].trim().toLowerCase(), keyvals[1].trim().toLowerCase());
            }
        }
        catch(Exception e)
        {
            System.out.println("The Configuration File Could Not Be Read");
            System.exit(1);
        }

    }
    public String get(String key)
    {
        return (String)map.get(key);
    }
}

import java.util.HashMap;
import java.util.Map;

public class Memory {
    private Map<String, String> variables;

    public Memory() {
        this.variables = new HashMap<>();
    }

    public void storeVariable(String processId, String variableName, String value) {
        variables.put(processId + "_" +variableName, value);
    }

    public String retrieveVariable(String processId, String variableName) {
        return variables.get(processId + "_" +variableName);
    }
    public int memorySize()
    {
    	return variables.size();
    }
   
    public void printMemory() {
        System.out.println("Memory Contents:");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("End of Memory Contents");
    }
}

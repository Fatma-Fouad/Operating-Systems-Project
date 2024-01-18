import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class ProcessExecution {

    private static Queue<Process> readyQueue = new LinkedList<>();
    private static Memory memory = new Memory();
	public static ArrayList<String> timechart = new ArrayList<String>();
	public static int  time = -1;

    static String ANSI_RESET = "\u001B[0m";
    static String ANSI_RED = "\u001B[31m";
    static String ANSI_GREEN = "\u001B[32m";
    static String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) {
        // Reading programs from files
        ArrayList<Process> processes = new ArrayList<>();
        try {
            for (int i = 1; i <= 3; i++) {
                String fileName = "Program_" + i + ".txt";
                ArrayList<String> instructions = readProgram(fileName);
                int[] memoryBoundaries = {memory.memorySize(), memory.memorySize() + instructions.size()};
                PCB pcb = new PCB(i, 0, memoryBoundaries);
                Process process = new Process(pcb, instructions);
                processes.add(process);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Please choose algorithm enter 1 -> for shortestJobFirst or 2 -> for Round robin  ");
        String algorithm = scanner.nextLine();
        if (algorithm.equals("1") ) {
          System.out.println("\nShortest Job First Scheduling Running:");
          shortestJobFirst(new ArrayList<>(processes));       	
        } 
        if (algorithm.equals("2") ) {
            System.out.println("Round Robin Scheduling Running:");
            roundRobin(new ArrayList<>(processes));      	
          }
        GranntChart(timechart);
      
    }

    public static void roundRobin(ArrayList<Process> processes) {
        while (!processes.isEmpty()) {
            Process currentProcess = processes.remove(0);
            readyQueue.add(currentProcess);
            printReadyQueue();

            while (!readyQueue.isEmpty()) {
                Process executingProcess = readyQueue.poll();
                roundRoubinExecuteInstructions(executingProcess);
                if (executingProcess.pcb.programCounter < executingProcess.instructions.size()) {
                    processes.add(executingProcess);
                }
            }
        }
    }
    
    private static void shortestJobFirst(ArrayList<Process> processes) {
        processes.sort((p1, p2) -> p1.instructions.size() - p2.instructions.size());

        while (!processes.isEmpty()) {
            Process currentProcess = processes.remove(0);
            readyQueue.add(currentProcess);
            printReadyQueue();
            readyQueue.poll();
            shortestJobFirstExecuteInstructions(currentProcess);
        }

    }

    private static void shortestJobFirstExecuteInstructions(Process process) {
    	time++;
    	timechart.add("P"+process.pcb.processID +" at time: "+time);
    	System.out.println("process id" + process.pcb.processID);
        while (process.pcb.programCounter < process.instructions.size()) {
            String instruction = process.instructions.get(process.pcb.programCounter);
            process.pcb.programCounter++;

            if (instruction.startsWith("assign")) {
                handleAssignInstruction(process, instruction);
            } else if (instruction.startsWith("print")) {
                handlePrintInstruction(process, instruction);
            } else if (instruction.startsWith("writeFile")) {
            	handleWriteInstruction(process, instruction);
            }
            time++;
        }
        memory.printMemory();
    }
    
    private static void roundRoubinExecuteInstructions(Process process) {
    	System.out.println("process id" + process.pcb.processID);
    	int localCounter = 0;
        while (localCounter < 2 && process.pcb.programCounter < process.instructions.size()) {
            String instruction = process.instructions.get(process.pcb.programCounter);
            process.pcb.programCounter++;
            localCounter++;
            time++;
            timechart.add("P"+process.pcb.processID +" at time: "+time);
            if (instruction.startsWith("assign")) {
                handleAssignInstruction(process, instruction);
            } else if (instruction.startsWith("print")) {
                handlePrintInstruction(process, instruction);
            } else if (instruction.startsWith("writeFile")) {
            	handleWriteInstruction(process, instruction);
            }
        }
        memory.printMemory();
       
    }

    private static void handleAssignInstruction(Process process,String instruction) {
        String[] parts = instruction.split("\\s+");
        int partsLength = parts.length;
        String variable = parts[1];
        String operation = parts[2];
        String operand1 = "";
        String operand2 = "";
        if(partsLength >= 4) {
        	operand1 = parts[3];
        }
        
        if(partsLength >= 5) {
        	operand2 = parts[4];
        }

        switch (operation) {
            case "input":
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter a value for " + variable + ": ");
                String value = scanner.nextLine();
                memory.storeVariable(""+process.pcb.processID, variable, value);
                break;
            case "add":
            case "subtract":
            case "multiply":
            case "divide":
                // Handle arithmetic operations involving variables
                handleArithmeticOperation(process, variable, operation, operand1, operand2);
                break;
            case "readFile":
                // Handle arithmetic operations involving variables
                handleReadOperation(process, variable, operand1);
                break;
            default:
                System.out.println("Unsupported operation: " + operation);
        }
    }

    private static void handlePrintInstruction(Process process,String instruction) {
        String[] parts = instruction.split("\\s+");
        String variable = parts[1];
        String value = memory.retrieveVariable(""+process.pcb.processID,variable);
        System.out.println(ANSI_RED + " Variable " + variable + " :" + value + ANSI_RESET);
    }
    
    private static void handleWriteInstruction(Process process,String instruction) {
        String[] parts = instruction.split("\\s+");
        String operand1 = parts[1];
        String operand2 = parts[2];
        String value1 = memory.retrieveVariable(""+process.pcb.processID,operand1);
        String value2 = memory.retrieveVariable(""+process.pcb.processID,operand2);
        writeToFile("process_output", value1 + value2);
    }
    
    public static void writeToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            System.out.println(ANSI_RED + " Content successfully written to file: " + fileName + ANSI_RESET);
        } catch (IOException e) {
            System.err.println(ANSI_RED + " Error writing to file: " + fileName + ANSI_RESET);
            e.printStackTrace();
        }
    }

    
    private static void handleReadOperation(Process process, String variable, String fileNameVariable) {
        String fileName = memory.retrieveVariable(""+process.pcb.processID,fileNameVariable);
    	String value = "";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                value = line.trim(); // Corrected this line
            }
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
        memory.storeVariable(""+process.pcb.processID, variable, value);
    }
    
    private static void handleArithmeticOperation(Process process, String variable, String operation, String operand1, String operand2) {
    	
        int value1 =  Integer.parseInt(memory.retrieveVariable(""+process.pcb.processID,operand1));
        int value2 = Integer.parseInt(memory.retrieveVariable(""+process.pcb.processID, operand2));

        int result = 0;
        switch (operation) {
            case "add":
                result = value1 + value2;
                break;
            case "subtract":
                result = value1 - value2;
                break;
            case "multiply":
                result = value1 * value2;
                break;
            case "divide":
                result = value1 / value2;
                break;
        }

        memory.storeVariable(""+process.pcb.processID, variable, ""+result);
    }

    private static ArrayList<String> readProgram(String fileName) throws IOException {
        ArrayList<String> instructions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                instructions.add(line.trim());
            }
        }
        return instructions;
    }
    
    private static void printReadyQueue() {
        System.out.println(ANSI_GREEN + "Ready Queue Contents:" + ANSI_RESET);
        
        // Using an enhanced for loop
        for (Process process : readyQueue) {
            System.out.println(process.toString());
        }

        System.out.println(ANSI_GREEN + "End of Ready Queue Contents" + ANSI_RESET);
    }
    private static void GranntChart(ArrayList<String> timechart) {
        System.out.println(ANSI_BLUE + "Grannt chart:" + ANSI_RESET);
        
        // Using an enhanced for loop
        for (String string : timechart) {
            System.out.println(string.toString());
        }

        System.out.println(ANSI_BLUE + "End of Grannt chart:" + ANSI_RESET);
    }
}


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.controller;

/**
 *
 * @author gadyr
 */

import com.mycompany.view.App;
import com.mycompany.logic.CPU;
import com.mycompany.logic.Memory;
import com.mycompany.logic.Instruction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PCController {
    private App vista;
    private CPU cpu;
    private Memory memory;
    private List<Instruction> program;
    private int currentInstructionIndex;
    private boolean programLoaded;
    private boolean executing;
    
    public PCController(App vista) {
        this.vista = vista;
        this.memory = new Memory(); 
        this.cpu = new CPU(memory);
        this.program = new ArrayList<>();
        this.currentInstructionIndex = 0;
        this.programLoaded = false;
        this.executing = false;
        
        initializeView();
        setupEventListeners();
    }
    
    /**
     * Inicializa la vista con valores por defecto
     */
    private void initializeView() {
        vista.newMemorySize.setText("100");
        vista.userMemory.setText("20"); 

        updateMemoryTable();
        updateInstructionTable();
        updateBCPDisplay();

        vista.execute.setEnabled(false);
        vista.nextStep.setEnabled(false);
    }
    
    /**
     * Configura los listeners para todos los botones
     */
    private void setupEventListeners() {
        vista.loadFile.addActionListener(e -> loadAssemblyFile());
        vista.execute.addActionListener(e -> {
            if (!executing) {
                startExecution();
            } else {
                stopExecution();
            }
        });
        vista.nextStep.addActionListener(e -> executeNextStep());
        vista.setNewMemory.addActionListener(e -> configureMemory());
        vista.stadistics.addActionListener(e -> showStatistics());
    }
    
    /**
     * Carga un archivo .asm desde el sistema de archivos
     */
    private void loadAssemblyFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Assembly files (*.asm)", "asm");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(vista);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                loadProgramFromFile(filePath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vista, 
                    "Error cargando archivo: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    /**
     * Procesa SOLO la primera instrucción al cargar el programa
     * Establece IR con la primera instrucción y PC apuntando a la segunda
     */
    private void processFirstInstruction() {
        if (program.size() > 0) {
            Instruction firstInstruction = program.get(0);

            int binaryValue = Integer.parseInt(firstInstruction.getBinaryCode(), 2);
            cpu.setIR(binaryValue);

            cpu.incrementPC();

            if (firstInstruction.getOperation().equals("MOV") && firstInstruction.getValue() != 0) {
                String register = firstInstruction.getRegister();
                int value = firstInstruction.getValue();

                switch (register) {
                    case "AX":
                        cpu.setAX(value);
                        System.out.println("Primera instrucción procesada: AX = " + value);
                        break;
                    case "BX":
                        cpu.setBX(value);
                        System.out.println("Primera instrucción procesada: BX = " + value);
                        break;
                    case "CX":
                        cpu.setCX(value);
                        System.out.println("Primera instrucción procesada: CX = " + value);
                        break;
                    case "DX":
                        cpu.setDX(value);
                        System.out.println("Primera instrucción procesada: DX = " + value);
                        break;
                }
            }

            currentInstructionIndex = 1;

            System.out.println("IR establecido con primera instrucción: " + firstInstruction.getAssemblyCode());
            System.out.println("PC apunta a segunda instrucción: " + cpu.getPC());
        }
    }
    
    /**
     * Lee y parsea un archivo .asm
     */
    private void loadProgramFromFile(String filePath) throws IOException {
        program.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (!line.isEmpty() && !line.startsWith(";") && !line.startsWith("//")) {
                    if (!Instruction.isValidInstructionFormat(line)) {
                        throw new IllegalArgumentException("Formato inválido en línea " + lineNumber + ": " + line);
                    }
                    
                    try {
                        Instruction instruction = new Instruction(line);
                        program.add(instruction);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Error en línea " + lineNumber + ": " + ex.getMessage());
                    }
                }
                lineNumber++;
            }
        }
        
        if (program.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene instrucciones válidas");
        }
        
        resetExecution();
        
        loadProgramToMemory();
        
        processFirstInstruction();
        
        programLoaded = true;
        
                
        updateInstructionTable();
        updateMemoryTable();
        updateBCPDisplay(); 
        
        vista.execute.setEnabled(true);
        vista.nextStep.setEnabled(true);
        
        highlightCurrentInstruction();
        
        JOptionPane.showMessageDialog(vista, 
            "Programa cargado exitosamente\n" + program.size() + " instrucciones encontradas\n" +
            "Cargado en memoria desde posición " + memory.getUserMemoryStart(), 
            "Éxito", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Procesa las instrucciones MOV con valores inmediatos para establecer valores iniciales
     */
    private void processInitialMovInstructions() {
        
        for (Instruction instruction : program) {
            if (instruction.getOperation().equals("MOV") && instruction.getValue() != 0) {
                String register = instruction.getRegister();
                int value = instruction.getValue();
                
                switch (register) {
                    case "AX":
                        cpu.setAX(value);
                        System.out.println("Valor inicial: AX = " + value);
                        break;
                    case "BX":
                        cpu.setBX(value);
                        System.out.println("Valor inicial: BX = " + value);
                        break;
                    case "CX":
                        cpu.setCX(value);
                        System.out.println("Valor inicial: CX = " + value);
                        break;
                    case "DX":
                        cpu.setDX(value);
                        System.out.println("Valor inicial: DX = " + value);
                        break;
                }
            }
        }
    }
    
    /**
     * Carga las instrucciones del programa en memoria
     */
    private void loadProgramToMemory() {
        int startAddress = memory.getUserMemoryStart();
        
        cpu.setProgramStart(startAddress);
        
        for (int i = 0; i < program.size(); i++) {
            Instruction instruction = program.get(i);
            
            int binaryValue = Integer.parseInt(instruction.getBinaryCode(), 2);
            memory.writeToMemory(startAddress + i, binaryValue, instruction.getAssemblyCode());
            
            System.out.println("Cargando en memoria[" + (startAddress + i) + "]: " + 
                             instruction.getAssemblyCode() + " -> " + binaryValue);
        }
    }
    

    
    /**
     * Inicia la ejecución automática del programa
     */
    private void startExecution() {
        if (!programLoaded) {
            JOptionPane.showMessageDialog(vista, "Primero debe cargar un programa", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        executing = true;
        vista.execute.setText("Stop");
        vista.nextStep.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (executing && currentInstructionIndex < program.size()) {
                    SwingUtilities.invokeLater(() -> executeCurrentInstruction());
                    Thread.sleep(1000);
                }
                return null;
            }
            
            @Override
            protected void done() {
                stopExecution();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Detiene la ejecución automática
     */
    private void stopExecution() {
        executing = false;
        vista.execute.setText("Start");
        vista.nextStep.setEnabled(currentInstructionIndex < program.size());
        
        if (currentInstructionIndex >= program.size()) {
            JOptionPane.showMessageDialog(vista, "Programa terminado", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Ejecuta la siguiente instrucción paso a paso
     */
    private void executeNextStep() {
        if (!programLoaded) {
            JOptionPane.showMessageDialog(vista, "Primero debe cargar un programa", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        executeCurrentInstruction();
    }
    
   private void executeCurrentInstruction() {
       
        if (currentInstructionIndex >= program.size()) {
            JOptionPane.showMessageDialog(vista, "Programa terminado", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Instruction currentInstruction = program.get(currentInstructionIndex);
        System.out.println("=== EJECUTANDO PASO " + (currentInstructionIndex + 1) + " ===");
        System.out.println("Instrucción: " + currentInstruction.getAssemblyCode());

        try {
            boolean success = false;

            int binaryValue = Integer.parseInt(currentInstruction.getBinaryCode(), 2);
            cpu.setIR(binaryValue);

            if (currentInstruction.getOperation().equals("MOV") && currentInstruction.getValue() != 0) {
                success = executeMovWithValue(currentInstruction);
            } else {
                success = cpu.executeInstruction(currentInstruction.getBinaryCode());
            }

            if (!success) {
                JOptionPane.showMessageDialog(vista, 
                    "Error ejecutando instrucción: " + currentInstruction.getAssemblyCode(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                stopExecution();
                return;
            }

            cpu.incrementPC();
            currentInstructionIndex++;

            updateBCPDisplay();
            updateMemoryTable();

            highlightCurrentInstruction();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vista, 
                "Error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            stopExecution();
        }

        if (currentInstructionIndex >= program.size()) {
            stopExecution();
        }
    }
    
    /**
     * Ejecuta una instrucción MOV con valor inmediato
     */
    private boolean executeMovWithValue(Instruction instruction) {
        String register = instruction.getRegister();
        int value = instruction.getValue();
        
        System.out.println("Ejecutando MOV " + register + ", " + value);
        
        switch (register) {
            case "AX":
                cpu.setAX(value);
                return true;
            case "BX":
                cpu.setBX(value);
                return true;
            case "CX":
                cpu.setCX(value);
                return true;
            case "DX":
                cpu.setDX(value);
                return true;
            default:
                System.out.println("Registro desconocido: " + register);
                return false;
        }
    }
    
    /**
     * Configura el tamaño de la memoria
     */
    private void configureMemory() {
        try {
            int totalSize = Integer.parseInt(vista.newMemorySize.getText());
            int osSize = Integer.parseInt(vista.userMemory.getText()); // Ahora es para SO

            if (totalSize < 20) {
                JOptionPane.showMessageDialog(vista, "El tamaño total mínimo es 20", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (osSize < 5) {
                JOptionPane.showMessageDialog(vista, "La memoria del SO mínima es 5", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (osSize >= totalSize) {
                JOptionPane.showMessageDialog(vista, "La memoria del SO debe ser menor al total", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userSize = totalSize - osSize;
            if (userSize < 10) {
                JOptionPane.showMessageDialog(vista, "La memoria del usuario debe ser al menos 10", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (programLoaded) {
                int option = JOptionPane.showConfirmDialog(vista, 
                    "Cambiar la memoria reiniciará el programa. ¿Continuar?", 
                    "Confirmar", 
                    JOptionPane.YES_NO_OPTION);

                if (option != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            memory = new Memory(totalSize, osSize);
            cpu = new CPU(memory);

            if (programLoaded) {
                resetExecution();
            }

            updateMemoryTable();
            updateBCPDisplay();

            JOptionPane.showMessageDialog(vista, 
                "Memoria configurada:\n" +
                "Total: " + totalSize + "\n" +
                "SO: " + osSize + " (0-" + (osSize-1) + ")\n" +
                "Usuario: " + userSize + " (" + osSize + "-" + (totalSize-1) + ")", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Ingrese números válidos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Muestra estadísticas del programa
     */
    private void showStatistics() {
        String stats = "This button is not working yet";
        JOptionPane.showMessageDialog(vista, stats, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Actualiza la tabla de memoria en la interfaz
     */
    private void updateMemoryTable() {
        Object[][] data = memory.getMemoryForTable();
        String[] columnNames = {"Position", "Value in memory"};
        
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        vista.memoryTable.setModel(model);
    }
    
    /**
     * Actualiza la tabla de instrucciones en la interfaz
     */
    private void updateInstructionTable() {
        Object[][] data = new Object[program.size()][2];
        
        for (int i = 0; i < program.size(); i++) {
            Instruction inst = program.get(i);
            data[i][0] = inst.getAssemblyCode();
            data[i][1] = inst.getBinaryCode();
        }
        
        String[] columnNames = {"Instruction", "Binary"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        vista.instrucctionTable.setModel(model);
    }
    
    /**
     * Resalta la instrucción que corresponde según el estado actual
     */
    private void highlightCurrentInstruction() {
        if (programLoaded) {
            if (currentInstructionIndex == 1) {
                // Al cargar, resaltar la primera instrucción (que ya se ejecutó)
                vista.instrucctionTable.setRowSelectionInterval(0, 0);
            } else if (currentInstructionIndex > 1 && currentInstructionIndex <= program.size()) {
                // Durante ejecución, resaltar la instrucción anterior (recién ejecutada)
                vista.instrucctionTable.setRowSelectionInterval(currentInstructionIndex - 1, currentInstructionIndex - 1);
            } else {
                vista.instrucctionTable.clearSelection();
            }
        }
    }
    
    /**
     * Actualiza el display del BCP (Block Control Process)
     */
    private void updateBCPDisplay() {
        StringBuilder bcp = new StringBuilder();
        bcp.append(cpu.getStatus()); 
        vista.jTextArea1.setText(bcp.toString());
    }
    
    /**
     * Resetea la ejecución del programa
     */
    private void resetExecution() {
        cpu.reset();
        memory.clearUserMemory();
        currentInstructionIndex = 0;
        executing = false;
        vista.execute.setText("Start");
        vista.nextStep.setEnabled(programLoaded);
        updateBCPDisplay();
        updateMemoryTable();
        
        if (programLoaded) {
            highlightCurrentInstruction();
        }
    }
}
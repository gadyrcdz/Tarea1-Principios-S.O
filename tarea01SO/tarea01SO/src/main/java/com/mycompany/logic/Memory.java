/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.logic;

/**
 *
 * @author gadyr
 */

/**
 * Clase que representa la memoria de la mini computadora
 * Maneja la separación entre memoria del SO y del usuario
 */
public class Memory {
    private int[] memory;
    private String[] memoryLabels; 
    private int totalSize;
    private int osMemoryEnd; 
    private int userMemoryStart; 
    
    /**
     * Constructor por defecto con memoria de 100 posiciones
     */
    public Memory() {
        this(100, 20); 
    }
    
    /**
     * Constructor personalizado
     * @param totalSize tamaño total de la memoria
     * @param osSize tamaño reservado para el SO
     */
    public Memory(int totalSize, int osSize) {
        this.totalSize = totalSize;
        this.osMemoryEnd = osSize - 1;
        this.userMemoryStart = osSize;
        this.memory = new int[totalSize];
        this.memoryLabels = new String[totalSize];
        
        for (int i = 0; i < totalSize; i++) {
            memory[i] = 0;
            memoryLabels[i] = "";
        }
    }
    
    /**
     * Lee un valor de la memoria
     * @param address dirección a leer
     * @return valor en la dirección, o 0 si está fuera de rango
     */
    public int readFromMemory(int address) {
        if (isValidAddress(address)) {
            return memory[address];
        }
        return 0;
    }
    
    /**
     * Escribe un valor en la memoria (SOLO área de usuario para operaciones del programa)
     * @param address dirección donde escribir
     * @param value valor a escribir
     * @return true si la escritura fue exitosa
     */
    public boolean writeToMemory(int address, int value) {
        if (isValidUserAddress(address)) {
            memory[address] = value;
           
            return true;
        }
//        System.out.println("ERROR: Intento de escribir en área protegida. Dirección " + address + " no está en área de usuario (" + userMemoryStart + "-" + (totalSize-1) + ")");
        return false;
    }
    
    /**
     * Escribe un valor en la memoria con etiqueta (SOLO área de usuario)
     * @param address dirección donde escribir
     * @param value valor a escribir
     * @param label etiqueta descriptiva
     * @return true si la escritura fue exitosa
     */
    public boolean writeToMemory(int address, int value, String label) {
        if (isValidUserAddress(address)) {
            memory[address] = value;
            memoryLabels[address] = label != null ? label : "";
            return true;
        }
        System.out.println("ERROR: Intento de escribir en área protegida. Dirección " + address + " no está en área de usuario (" + userMemoryStart + "-" + (totalSize-1) + ")");
        return false;
    }
    
    /**
     * Escribe forzado (SOLO para inicialización del sistema, no para operaciones del usuario)
     */
    public boolean writeToMemoryForce(int address, int value, String label) {
        if (isValidAddress(address)) {
            memory[address] = value;
            memoryLabels[address] = label != null ? label : "";
            return true;
        }
        return false;
    }
    
    /**
     * Carga un programa en memoria con etiquetas (solo en área de usuario)
     */
    public boolean loadProgramWithLabels(Object[][] instructions, int startAddress) {
        if (startAddress < userMemoryStart) {
            return false;
        }
        
        if (startAddress + instructions.length > totalSize) {
            return false; 
        }
        
        for (int i = 0; i < instructions.length; i++) {
            int value = (Integer) instructions[i][0];
            String label = (String) instructions[i][1];
            
            memory[startAddress + i] = value;
            memoryLabels[startAddress + i] = label;
        }
        
        return true;
    }
    
    /**
     * Verifica si una dirección es válida en toda la memoria
     */
    private boolean isValidAddress(int address) {
        return address >= 0 && address < totalSize;
    }
    
    /**
     * Verifica si una dirección está en el área de usuario
     */
    private boolean isValidUserAddress(int address) {
        return address >= userMemoryStart && address < totalSize;
    }
    
    /**
     * Limpia toda la memoria del usuario
     */
    public void clearUserMemory() {
        for (int i = userMemoryStart; i < totalSize; i++) {
            memory[i] = 0;
            memoryLabels[i] = "";
        }
    }
    
    /**
     * Redimensiona la memoria manteniendo la proporción SO/Usuario
     */
    public boolean resizeMemory(int newSize) {
        if (newSize < 20) { 
            return false;
        }
        
        int[] newMemory = new int[newSize];
        String[] newLabels = new String[newSize];
        int copySize = Math.min(totalSize, newSize);
        
        System.arraycopy(memory, 0, newMemory, 0, copySize);
        System.arraycopy(memoryLabels, 0, newLabels, 0, copySize);
        
        for (int i = copySize; i < newSize; i++) {
            newMemory[i] = 0;
            newLabels[i] = "";
        }
        
        this.memory = newMemory;
        this.memoryLabels = newLabels;
        this.totalSize = newSize;
        
        int newOsSize = Math.max(10, newSize / 5);
        this.osMemoryEnd = newOsSize - 1;
        this.userMemoryStart = newOsSize;
        
        return true;
    }
    
    // Getters
    public int getTotalSize() { return totalSize; }
    public int getOsMemoryEnd() { return osMemoryEnd; }
    public int getUserMemoryStart() { return userMemoryStart; }
    public int getUserMemorySize() { return totalSize - userMemoryStart; }
    
    /**
     * Obtiene una representación de la memoria para mostrar en la tabla
     * @return arreglo bidimensional [posición][valor] para la tabla
     */
    public Object[][] getMemoryForTable() {
        Object[][] tableData = new Object[totalSize][2];

        for (int i = 0; i < totalSize; i++) {
            tableData[i][0] = i; // Posición

            String areaType;
            if (i <= osMemoryEnd) {
                areaType = "SO";
            } else {
                areaType = "User";
            }

            if (!memoryLabels[i].isEmpty()) {
                tableData[i][1] = areaType + " - " + memoryLabels[i] + " (" + memory[i] + ")";
            } else if (memory[i] != 0) {
                tableData[i][1] = areaType + " - " + memory[i];
            } else {
                tableData[i][1] = areaType + " empty space";
            }
        }

        return tableData;
    }
    
    /**
     * Obtiene el estado de la memoria para debugging
     */
    public String getMemoryStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== ESTADO MEMORIA ===\n");
        status.append("Tamaño total: ").append(totalSize).append("\n");
        status.append("Memoria SO: 0-").append(osMemoryEnd).append(" (PROTEGIDA)\n");
        status.append("Memoria Usuario: ").append(userMemoryStart).append("-").append(totalSize-1).append("\n");
        status.append("Posiciones usadas: ");
        
        int usedPositions = 0;
        for (int i = 0; i < totalSize; i++) {
            if (memory[i] != 0) {
                usedPositions++;
            }
        }
        status.append(usedPositions).append("/").append(totalSize).append("\n");
        
        return status.toString();
    }
}
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
 * Clase que representa el CPU de la mini computadora
 * Contiene los registros y la lógica de procesamiento
 */
public class CPU {
    // Registros de propósito general
    private int AX;  // 0001
    private int BX;  // 0010
    private int CX;  // 0011
    private int DX;  // 0100
    
    // Registros especiales
    private int AC;  // Acumulador
    private int IR;  // Registro de Instrucciones (instrucción ACTUAL)
    private int PC;  // Contador de Programa (PRÓXIMA instrucción)
    
    // Referencia a la memoria
    private Memory memory;
    
    public CPU(Memory memory) {
        this.memory = memory;
        reset();
    }
    
    /**
     * Reinicia todos los registros a 0
     */
    public void reset() {
        AX = BX = CX = DX = AC = IR = 0;
        PC = memory.getUserMemoryStart(); // PC apunta al inicio del área de usuario
    }
    
    /**
     * Establece el PC al inicio del programa cuando se carga
     */
    public void setProgramStart(int startAddress) {
        PC = startAddress;
        System.out.println("PC inicializado en posición: " + PC);
    }
    
    /**
     * Ejecuta una instrucción paso a paso
     * @param binaryInstruction instrucción en formato binario (8 bits)
     * @return true si la ejecución fue exitosa
     */
    public boolean executeInstruction(String binaryInstruction) {
        System.out.println("Ejecutando instrucción binaria: " + binaryInstruction + " (longitud: " + binaryInstruction.length() + ")");
        
        if (binaryInstruction.length() != 8) {
            System.out.println("Error: longitud de instrucción incorrecta");
            return false;
        }
        
        // IR contiene la instrucción ACTUAL que se está ejecutando
        IR = Integer.parseInt(binaryInstruction, 2);
        
        // Extraer operación (bits 0-3) y registro (bits 4-7)
        String opCode = binaryInstruction.substring(0, 4);
        String regCode = binaryInstruction.substring(4, 8);
        
        System.out.println("OpCode: " + opCode + ", RegCode: " + regCode);
        System.out.println("IR=" + IR + ", PC apunta a próxima instrucción: " + PC);
        
        switch (opCode) {
            case "0001": // LOAD
                return executeLoad(regCode);
            case "0010": // STORE
                return executeStore(regCode);
            case "0011": // MOV
                return executeMove(regCode);
            case "0100": // SUB
                return executeSubtract(regCode);
            case "0101": // ADD
                return executeAdd(regCode);
            default:
                System.out.println("Operación desconocida: " + opCode);
                return false;
        }
    }
    
    private boolean executeLoad(String regCode) {
        int registerValue = getRegisterValue(regCode);
        if (registerValue != Integer.MIN_VALUE) {
            AC = registerValue;
            System.out.println("LOAD: Cargando valor " + registerValue + " al AC");
            return true;
        }
        return false;
    }
    
    private boolean executeStore(String regCode) {
        int registerValue = getRegisterValue(regCode);
        if (registerValue != Integer.MIN_VALUE) {
            // Convertir la dirección relativa del registro a dirección absoluta en área de usuario
            int absoluteAddress = memory.getUserMemoryStart() + registerValue;

            // STORE guarda el AC en la dirección calculada (solo en área de usuario)
            // Usar writeToMemory sin etiqueta para preservar la etiqueta existente
            boolean success = memory.writeToMemory(absoluteAddress, AC);

            if (success) {
                // Solo actualizar el registro si la escritura en memoria fue exitosa
                setRegisterValue(regCode, AC);
                System.out.println("STORE: Guardando AC=" + AC + " en memoria[" + absoluteAddress + "] y actualizando registro");
            } else {
                System.out.println("STORE: Error - No se pudo escribir en memoria[" + absoluteAddress + "] (área protegida o fuera de rango)");
            }

            return success;
        }
        return false;
    }
    
    private boolean executeMove(String regCode) {
        // Para MOV básico entre registros (MOV reg, AC)
        // Nota: Los MOV con valores inmediatos se manejan en el controlador
        return setRegisterValue(regCode, AC);
    }
    
    private boolean executeAdd(String regCode) {
        int registerValue = getRegisterValue(regCode);
        if (registerValue != Integer.MIN_VALUE) {
            int oldAC = AC;
            AC += registerValue;
            System.out.println("ADD: " + oldAC + " + " + registerValue + " = " + AC);
            return true;
        }
        return false;
    }
    
    private boolean executeSubtract(String regCode) {
        int registerValue = getRegisterValue(regCode);
        if (registerValue != Integer.MIN_VALUE) {
            int oldAC = AC;
            AC -= registerValue;
            System.out.println("SUB: " + oldAC + " - " + registerValue + " = " + AC);
            return true;
        }
        return false;
    }
    
    /**
     * Obtiene el valor de un registro según su código binario
     */
    private int getRegisterValue(String regCode) {
        switch (regCode) {
            case "0001": return AX;
            case "0010": return BX;
            case "0011": return CX;
            case "0100": return DX;
            default: return Integer.MIN_VALUE; // Error
        }
    }
    
    /**
     * Establece el valor de un registro según su código binario
     */
    private boolean setRegisterValue(String regCode, int value) {
        switch (regCode) {
            case "0001": AX = value; return true;
            case "0010": BX = value; return true;
            case "0011": CX = value; return true;
            case "0100": DX = value; return true;
            default: return false;
        }
    }
    
    /**
     * Incrementa el contador de programa (apunta a la PRÓXIMA instrucción)
     */
    public void incrementPC() {
        PC++;
        System.out.println("PC incrementado a: " + PC);
    }
    
    // Getters para acceder a los registros desde el controlador
    public int getAX() { return AX; }
    public int getBX() { return BX; }
    public int getCX() { return CX; }
    public int getDX() { return DX; }
    public int getAC() { return AC; }
    public int getIR() { return IR; }
    public int getPC() { return PC; }
    
    // Setters para pruebas y configuración inicial
    public void setAX(int value) { AX = value; }
    public void setBX(int value) { BX = value; }
    public void setCX(int value) { CX = value; }
    public void setDX(int value) { DX = value; }
    public void setAC(int value) { AC = value; }
    public void setIR(int value) { IR = value; }
    
    /**
     * Obtiene el estado completo del CPU para mostrar en el BCP
     */
    public String getStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== REGISTROS CPU ===\n");
        status.append("AX: ").append(AX).append("\n");
        status.append("BX: ").append(BX).append("\n");
        status.append("CX: ").append(CX).append("\n");
        status.append("DX: ").append(DX).append("\n");
        status.append("AC: ").append(AC).append("\n");
        status.append("IR: ").append(IR).append(" (").append(String.format("%8s", Integer.toBinaryString(IR)).replace(' ', '0')).append(") - Instrucción actual\n");
        status.append("PC: ").append(PC).append(" - Próxima instrucción\n");
        return status.toString();
    }
}
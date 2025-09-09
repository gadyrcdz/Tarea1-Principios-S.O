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
 * Clase que representa una instrucción del mini lenguaje ensamblador
 */
public class Instruction {
    private String assemblyCode;    
    private String binaryCode;     
    private String operation;      
    private String register;       
    private int value;             
    
    public Instruction(String assemblyCode) {
        this.assemblyCode = assemblyCode.trim();
        parseInstruction();
    }
    
    /**
     * Parsea la instrucción de ensamblador y genera el código binario
     */
    private void parseInstruction() {
        String[] parts = assemblyCode.split("[, ]+");
        
        if (parts.length < 2) {
            throw new IllegalArgumentException("Instrucción inválida: " + assemblyCode);
        }
        
        operation = parts[0].toUpperCase();
        register = parts[1].toUpperCase();
        
        // Obtener valor si existe
        if (parts.length > 2) {
            try {
                value = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                value = 0;
            }
        }
        
        generateBinaryCode();
    }
    
    /**
     * Genera el código binario de 8 bits para la instrucción
     */
    private void generateBinaryCode() {
        String opCode = getOperationCode();
        String regCode = getRegisterCode();
        
        binaryCode = opCode + regCode;
        
        
//        System.out.println("Generando código para: " + assemblyCode);
//        System.out.println("OpCode: " + opCode + ", RegCode: " + regCode);
//        System.out.println("Código binario final: " + binaryCode + " (longitud: " + binaryCode.length() + ")");
    }
    
    /**
     * Obtiene el código de operación en binario (4 bits)
     */
    private String getOperationCode() {
        switch (operation) {
            case "LOAD": return "0001";
            case "STORE": return "0010";
            case "MOV": return "0011";
            case "SUB": return "0100";
            case "ADD": return "0101";
            default: 
                throw new IllegalArgumentException("Operación desconocida: " + operation);
        }
    }
    
    /**
     * Obtiene el código del registro en binario (4 bits)
     */
    private String getRegisterCode() {
        switch (register) {
            case "AX": return "0001";
            case "BX": return "0010";
            case "CX": return "0011";
            case "DX": return "0100";
            default:
                throw new IllegalArgumentException("Registro desconocido: " + register);
        }
    }
    
    /**
     * Determina si la instrucción necesita un valor inmediato
     */
    private boolean needsImmediateValue() {
        return operation.equals("MOV") && value != 0;
    }
    
    /**
     * Formatea un valor en 8 bits con signo
     * Bit 0: signo (0=positivo, 1=negativo)
     * Bits 1-7: valor absoluto
     */
    private String formatValue8Bits(int val) {
        if (val == 0) return "00000000";
        
        boolean isNegative = val < 0;
        int absValue = Math.abs(val);
        
        if (absValue > 127) {
            absValue = 127;
        }
        
        String valueBits = String.format("%7s", Integer.toBinaryString(absValue)).replace(' ', '0');
        
        return (isNegative ? "1" : "0") + valueBits;
    }
    
    /**
     * Crea una instrucción extendida para valores inmediatos
     * Retorna un arreglo con la instrucción y el valor si es necesario
     */
    public String[] getFullBinaryInstruction() {
        if (needsImmediateValue()) {
            String instrCode = getOperationCode() + getRegisterCode();
            String valueCode = formatValue8Bits(value);
            return new String[]{instrCode, valueCode};
        }
        
        return new String[]{binaryCode};
    }
    
    // Getters
    public String getAssemblyCode() { return assemblyCode; }
    public String getBinaryCode() { return binaryCode; }
    public String getOperation() { return operation; }
    public String getRegister() { return register; }
    public int getValue() { return value; }
    
    @Override
    public String toString() {
        return assemblyCode + " -> " + binaryCode;
    }
    
    /**
     * Método estático para validar formato de archivo
     */
    public static boolean isValidInstructionFormat(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = line.trim().split("[, ]+");
        
        if (parts.length < 2) {
            return false;
        }
        
        String operation = parts[0].toUpperCase();
        if (!operation.matches("LOAD|STORE|MOV|SUB|ADD")) {
            return false;
        }
        
        String register = parts[1].toUpperCase();
        if (!register.matches("AX|BX|CX|DX")) {
            return false;
        }
        
        if (parts.length > 2) {
            try {
                Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
}

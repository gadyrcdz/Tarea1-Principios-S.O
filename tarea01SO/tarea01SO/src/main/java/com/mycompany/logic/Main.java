/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.logic;

import com.mycompany.view.App;
import com.mycompany.controller.PCController;

/**
 *
 * @author gadyr
 */
public class Main {
    public static void main(String[] args) {
        App vista = new App();
        
        PCController controlador = new PCController(vista);
        
        vista.setVisible(true);
        vista.setTitle("Mini PC Simulator - Sistemas Operativos");
        vista.setLocationRelativeTo(null); 
    }
}

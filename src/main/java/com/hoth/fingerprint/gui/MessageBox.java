package com.hoth.fingerprint.gui;

import javax.swing.JOptionPane;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

public class MessageBox {
    public static void badQuality(Reader.CaptureQuality quality) {
        JOptionPane.showMessageDialog(null, quality.toString(), "Calidad deficiente", JOptionPane.WARNING_MESSAGE);
    }

    public static void badStatus(Reader.Status status) {
        String str = String.format("Estado del lector: %s", status);
        JOptionPane.showMessageDialog(null, str, "Estado del lector", JOptionPane.ERROR_MESSAGE);
    }

    public static void dpError(String functionName, UareUException e) {
        String str = String.format("%s devolvió error %d\n%s", functionName, (e.getCode() & 0xffff), e);
        JOptionPane.showMessageDialog(null, str, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void warning(String strText) {
        JOptionPane.showMessageDialog(null, strText, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }
    
}

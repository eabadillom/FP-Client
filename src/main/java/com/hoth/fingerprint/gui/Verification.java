package com.hoth.fingerprint.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

public class Verification extends JPanel implements ActionListener {
    private static final long serialVersionUID = 6;
    private static final String ACT_BACK = "back";
    private static Logger log = LogManager.getLogger(Verification.class);
    private Fmd[] m_fmds;
    private JDialog m_dlgParent;
    private JTextArea m_text;
    ReaderCollection m_Collection;
    Reader reader;
    static boolean finger_M;

    
    public static boolean isFinger_M() {
        return finger_M;
    }

    private final String m_strPrompt2 = "put the same or any other finger on the reader \n\n";

    private Verification(Fmd[] fmd_s) {

        m_fmds = new Fmd[2];
        m_fmds = fmd_s;

        final int vgap = 5;
        final int width = 380;
            
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);

        m_text = new JTextArea(22, 1);
        m_text.setEditable(false);
        JScrollPane paneReader = new JScrollPane(m_text);
        add(paneReader);
        Dimension dm = paneReader.getPreferredSize();
        dm.width = width;
        paneReader.setPreferredSize(dm);

        add(Box.createVerticalStrut(vgap));

        JButton btnBack = new JButton("Back");
        btnBack.setActionCommand(ACT_BACK);
        btnBack.addActionListener(this);
        add(btnBack);

        setOpaque(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACT_BACK)) {
            // cancelar captura
            m_dlgParent.setVisible(false);
        }

    }



    private boolean ProcessCaptureResult() {//condiciona el evento de captura el cual ya no ocupamos 
        boolean bCanceled = false;
 
                Engine engine = UareUGlobal.GetEngine();

     
                if (null != m_fmds[0] && null != m_fmds[1]) {
                    // perfom comparison
                    try {
                        int falsematch_rate = engine.Compare(m_fmds[0], 0, m_fmds[1], 0);
                        int target_falsematch_rate = Engine.PROBABILITY_ONE / 1000; // target rate is 0.00001

                        log.info("valor falsematch_rate: {}",falsematch_rate);
                        log.info("valor target_falsematch_rate: {}",target_falsematch_rate);

                        if (falsematch_rate < target_falsematch_rate) {
                            log.info("FingerPrint matched.\n" );
                            JOptionPane.showMessageDialog(null,"FingerPrint matched.\n");
                            finger_M = true;

                        } else {
                            JOptionPane.showMessageDialog(null,"FingerPrint did not match.\n");
                            finger_M = false;
                        }
                    } catch (UareUException e) {
                        MessageBox.DpError("Engine.CreateFmd()", e);
                    }
                    // discard FMDs
                    m_fmds[0] = null;
                    m_fmds[1] = null;

                } else {
                    m_text.append(m_strPrompt2);
                }
            
         
        return !bCanceled;
    }

    private void doModal(JDialog dlgParent) {


        ProcessCaptureResult();


        m_dlgParent = dlgParent;
        m_dlgParent.setContentPane(this);
        m_dlgParent.pack();
        m_dlgParent.setLocationRelativeTo(null);
        m_dlgParent.setAlwaysOnTop(true);		
        m_dlgParent.toFront();
        m_dlgParent.setVisible(false);
        m_dlgParent.dispose();

    }

    public static void Run(Fmd[] fmd_s) {
        JDialog dlg = new JDialog((JDialog) null, "Verification", true);
        Verification verification = new Verification(fmd_s);
        verification.doModal(dlg);
    }

}

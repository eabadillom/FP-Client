package com.hoth.fingerprint.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Capture extends JPanel implements ActionListener 
{
    private static final Logger log = LogManager.getLogger(Capture.class);
    private static final long serialVersionUID = 2L;

    private static Reader.CaptureResult captura;

    private final Reader reader;
    private CaptureThread captureThread;
    private final boolean streaming = false;
    private JDialog dialog;
    private ImagePanel imagePanel;
    private JLabel infoLabel;
    private JLabel focusLabel;
    private ScheduledExecutorService scheduler;
    private int tiempoCaptura;
    
    public void setTiempoCaptura(int tiempoCaptura) {
        this.tiempoCaptura = tiempoCaptura;
    }

    public static Reader.CaptureResult getCaptura() {
        return captura;
    }

    Capture() {
        this.reader = initializarLectora();
        this.captureThread = new CaptureThread(reader, streaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
        construirInterfaz();
    }

    private Reader initializarLectora() 
    {
        try {
            ReaderCollection readers = UareUGlobal.GetReaderCollection();
            readers.GetReaders();
            log.trace("Cantidad de lectores: {}", readers.size());
            Reader r = readers.get(0);
            log.debug("Lector detectado: {}", r.GetDescription().name);
            return r;
        } catch (UareUException e) {
            log.error("Error al obtener el lector de huella", e);
            throw new RuntimeException("No se puedo inicializar el lector de huellas");
        }
    }

    private void construirInterfaz() 
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final int vgap = 5;

        focusLabel = crearEtiqueta("Toque esta ventana para continuar.", "/images/information.png");
        focusLabel.setName("focusLabel");
        add(focusLabel);

        imagePanel = new ImagePanel();
        imagePanel.setPreferredSize(new Dimension(400, 500));
        add(imagePanel);
        add(Box.createVerticalStrut(vgap));

        infoLabel = crearEtiqueta("Coloca tu huella en el lector", "/images/huella.gif");
        infoLabel.setName("label1");
        add(infoLabel);
    }

    private JLabel crearEtiqueta(String text, String iconPath) 
    {
        JLabel label = new JLabel(text, new ImageIcon(getClass().getResource(iconPath)), JLabel.CENTER);
        label.setSize(200, 200);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void startCapture(JDialog dlg) 
    {
        captura = null;
        if (captureThread == null) return;

        try {
            captureThread = new CaptureThread(reader, streaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
            captureThread.start(this, dlg);
            infoLabel.setText("Escanea tu huella...");
        } catch (Exception e) {
            infoLabel.setText("La huella no fue capturada");
            log.error("Error iniciando captura", e);
        }
    }

    private void stopCaptureThread() 
    {
        if (captureThread != null) {
            captureThread.cancel();
        }
    }

    private void waitForCaptureThread() 
    {
        if (captureThread != null) {
            captureThread.join(1000);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if (!CaptureThread.ACT_CAPTURE.equals(e.getActionCommand())) return;

        log.trace("Evento de captura recibido");

        CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent) e;
        Reader.CaptureResult result = evt.captureResult;

        if (result == null || result.image == null) {
            log.error("Resultado o imagen de captura es null");
            return;
        }

        boolean goodQuality = streaming
                ? (result.quality == Reader.CaptureQuality.GOOD || result.quality == Reader.CaptureQuality.NO_FINGER)
                : result.quality == Reader.CaptureQuality.GOOD;

        if (goodQuality) {
            imagePanel.showImage(result.image);
            log.debug("Imagen capturada correctamente...");
            captura = result;
        }

        if (result.quality == Reader.CaptureQuality.CANCELED || evt.exception != null || evt.readerStatus != null) {
            log.debug("Captura cancelada o con error");
        }
    }

    private void mostrarDialogo(JDialog parent) 
    {
        try {
            reader.Open(Reader.Priority.COOPERATIVE);
        } catch (UareUException e) {
            log.warn("No se pudo abrir el lector", e);
            return;
        }

        if (streaming && !reader.GetCapabilities().can_stream) {
            log.info("El lector no admite streaming");
            return;
        }

        this.dialog = parent;
        startCapture(dialog);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this::stopCapture, tiempoCaptura, TimeUnit.MILLISECONDS);

        dialog.setContentPane(this);
        dialog.setAlwaysOnTop(true);
        dialog.setSize(400, 550);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setVisible(true);

        stopCaptureThread();
        waitForCaptureThread();

        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            MessageBox.dpError("UareUGlobal.destroyReaderCollection()", e);
        }
    }

    public void stopCapture() 
    {
        try {
            if(reader != null) reader.CancelCapture();
            if(dialog != null) dialog.setVisible(false);
            log.info("Captura detenida correctamente");
        } catch (UareUException e) {
            log.error("Error al detener la captura: {}", e.getCode());
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public void addFocusLabel() {
        add(focusLabel);
        revalidate();
    }

    public void removeFocusLabel() {
        remove(focusLabel);
        revalidate();
    }

    public static void Run(int tiempoCaptura) 
    {
        JDialog dialog = new JDialog((JDialog) null, "Lectura de huella", true);
        dialog.setIconImage(new ImageIcon(Capture.class.getResource("/images/FP-Client.png")).getImage());

        Capture capturePanel = new Capture();
        capturePanel.setTiempoCaptura(tiempoCaptura);

        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                capturePanel.addFocusLabel();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                capturePanel.removeFocusLabel();
            }
        });

        capturePanel.mostrarDialogo(dialog);
    }
}

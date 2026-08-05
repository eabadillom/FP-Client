package com.hoth.fingerprint.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

public class CaptureThread extends Thread 
{
    public static final String ACT_CAPTURE = "capture_thread_captured";
    private static final Logger log = LogManager.getLogger(CaptureThread.class);

    private final Reader reader;
    private final boolean streaming;
    private final Fid.Format format;
    private final Reader.ImageProcessing processing;

    private ActionListener listener;
    private JDialog dialog;
    private boolean cancelRequested = false;
    private CaptureEvent lastCapture;

    public static class CaptureEvent extends ActionEvent 
    {
        private static final long serialVersionUID = 101L;
        public final Reader.CaptureResult captureResult;
        public final Reader.Status readerStatus;
        public final UareUException exception;

        public CaptureEvent(Object source, String action, Reader.CaptureResult cr, Reader.Status rs, UareUException ex) 
        {
            super(source, ActionEvent.ACTION_PERFORMED, action);
            this.captureResult = cr;
            this.readerStatus = rs;
            this.exception = ex;
        }
    }

    public CaptureThread(Reader reader, boolean streaming, Fid.Format format, Reader.ImageProcessing processing) 
    {
        this.reader = reader;
        this.streaming = streaming;
        this.format = format;
        this.processing = processing;
    }

    public void start(ActionListener listener, JDialog dialog) 
    {
        this.listener = listener;
        this.dialog = dialog;
        super.start();
    }

    public void join(int milliseconds) 
    {
        try {
            super.join(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Join interrumpido", e);
        }
    }

    public CaptureEvent getLastCaptureEvent() {
        return lastCapture;
    }

    public void cancel() 
    {
        cancelRequested = true;
        try {
            if (!streaming) {
                reader.CancelCapture();
            }
        } catch (UareUException ignored) {
        }
    }

    @Override
    public void run() 
    {
        if (streaming) {
            runStreaming();
        } else {
            runCapture();
        }
    }

    private void runCapture() 
    {
        try {
            if (!waitForReaderReady()) {
                notifyCancelled();
                return;
            }

            Reader.CaptureResult result = reader.Capture(format, processing, reader.GetCapabilities().resolutions[0], -1);

            notifyListener(ACT_CAPTURE, result, null, null);
            reader.Close();

            mostrarMensaje();
            dialog.setVisible(false);
            log.debug("Huella capturada correctamente");

        } catch (UareUException e) {
            notifyListener(ACT_CAPTURE, null, null, e);
        }
    }

    private void runStreaming() {
        try {
            if (!waitForReaderReady()) {
                notifyCancelled();
                return;
            }

            reader.StartStreaming();
            while (!cancelRequested) {
                Reader.CaptureResult result = reader.GetStreamImage(format, processing, reader.GetCapabilities().resolutions[0]);
                notifyListener(ACT_CAPTURE, result, null, null);
            }
            reader.StopStreaming();

        } catch (UareUException e) {
            notifyListener(ACT_CAPTURE, null, null, e);
        } finally {
            if (cancelRequested) notifyCancelled();
        }
    }

    private boolean waitForReaderReady() 
    {
        try {
            while (!cancelRequested) {
                Reader.Status status = reader.GetStatus();
                log.debug("Estado del lector: {}", status);

                if (status.status == null) {
                    notifyListener(ACT_CAPTURE, null, status, null);
                    return false;
                }

                switch (status.status) {
                    case BUSY:
                        Thread.sleep(1000);
                        break;
                    case READY:
                    case NEED_CALIBRATION:
                        return true;
                }
            }
        } catch (UareUException | InterruptedException e) {
            log.error("Error esperando al lector", e);
        }
        return false;
    }

    private void notifyListener(String action, Reader.CaptureResult cr, Reader.Status status, UareUException ex) 
    {
        CaptureEvent event = new CaptureEvent(this, action, cr, status, ex);
        lastCapture = event;

        if (listener != null && action != null && !action.isEmpty()) {
            SwingUtilities.invokeLater(() -> listener.actionPerformed(event));
        }
    }

    private void notifyCancelled() {
        Reader.CaptureResult result = new Reader.CaptureResult();
        result.quality = Reader.CaptureQuality.CANCELED;
        notifyListener(ACT_CAPTURE, result, null, null);
    }

    private void mostrarMensaje() 
    {
        JLabel label = new JLabel("Huella capturada correctamente", JLabel.CENTER);
        label.setSize(200, 200);
        label.setIcon(new ImageIcon(getClass().getResource("/images/verificacion.gif")));
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        dialog.add(label);
        dialog.revalidate();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }
    
}

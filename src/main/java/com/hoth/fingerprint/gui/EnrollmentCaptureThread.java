package com.hoth.fingerprint.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

public class EnrollmentCaptureThread extends Thread {

    public static final String ACT_CAPTURE = "capture_thread_captured";
    private static Logger log = LogManager.getLogger(CaptureThread.class);

    public class CaptureEvent extends ActionEvent {

        private static final long serialVersionUID = 101;
        public Reader.CaptureResult capture_result;
        public Reader.Status reader_status;
        public UareUException exception;

        public CaptureEvent(Object source, String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) 
        {
            super(source, ActionEvent.ACTION_PERFORMED, action);
            capture_result = cr;
            reader_status = st;
            exception = ex;
        }
    }

    private ActionListener m_listener;
    private boolean m_bCancel;
    private Reader m_reader;
    private boolean m_bStream;
    private Fid.Format m_format;
    private Reader.ImageProcessing m_proc;
    private CaptureEvent m_last_capture;

    public EnrollmentCaptureThread(Reader reader, boolean bStream, Fid.Format img_format, Reader.ImageProcessing img_proc) 
    {
        m_bCancel = false;
        m_reader = reader;
        m_bStream = bStream;
        m_format = img_format;
        m_proc = img_proc;
    }

    public void start(ActionListener listener) 
    {
        try
        {
            Thread.sleep(1000);
            m_listener = listener;
            super.start();
        }catch (InterruptedException e) 
        {
            log.trace("El hilo aun no esta listo {}", e.toString());
        }
    }

    public void join(int milliseconds) {
        try {
            super.join(milliseconds);
        } catch (InterruptedException e) {
            log.error("Error al unir al enrolamiento");
        }
    }

    public CaptureEvent getLastCaptureEvent() {
        return m_last_capture;
    }

    public void Capture() 
    {
        boolean bLectorListo = false;
        try 
        {
            //wait for reader to become ready
            bLectorListo = estadoEnEspera(bLectorListo);
            
            if(m_bCancel)
            {
                Reader.CaptureResult cr = new Reader.CaptureResult();
                cr.quality = Reader.CaptureQuality.CANCELED;
                NotifyListener(ACT_CAPTURE, cr, null, null);
                log.info("Captura cancelada.....");
            }
            
            estadoEnListo(bLectorListo);
            
        } catch (UareUException e) 
        {
            NotifyListener(ACT_CAPTURE, null, null, e);
        }catch (InterruptedException ie) 
        {
            log.error("El hilo fue interrumpido: {}", ie.getMessage());
            Thread.currentThread().interrupt(); // Mantén el estado de interrupción del hilo
        }
    }
    
    private void estadoEnListo(boolean bLectorListo) throws UareUException
    {
        if (bLectorListo == false) 
        {
            return;
        }
        
        //capture
        log.info("Iniciando captura de huella para enrolamiento");
        log.trace("Format: {}", m_format);
        log.trace("Proceso de imagen: {}", m_proc);
        log.trace("Lector: {}", m_reader.GetStatus());//colocar condiciones para el estatus del lector se traba
        log.trace("Resolucion {}", m_reader.GetCapabilities().resolutions[0]);

        int resolution = m_reader.GetCapabilities().resolutions != null && m_reader.GetCapabilities().resolutions.length > 0
            ? m_reader.GetCapabilities().resolutions[0]
            : -1;

        if (resolution == -1) 
        {
            log.error("No hay resoluciones disponibles en el lector.");
            return;
        }

        estadoActualLector(resolution);

        log.info("Huella capturada....");
        //log.info("valor de la huella detectada {}",cr);
    }
    
    private void estadoActualLector(int resolucionImagen) throws UareUException 
    {
        try 
        {
            //Reader.ReaderStatus status = m_reader.GetStatus();
            log.trace("Estado actual del lector: {}", m_reader.GetStatus().status);

            if(Reader.ReaderStatus.READY != m_reader.GetStatus().status) 
            {
                log.warn("Lector no está listo. Estado actual: {}", m_reader.GetStatus().status);
                Thread.sleep(1000); // Pausa breve para evitar el consumo excesivo de recursos
            }

            if(Reader.ReaderStatus.READY == m_reader.GetStatus().status)
            {
                log.debug("Entre a capturar huellas");
                Reader.CaptureResult cr = null;

                log.trace("Info de resolucion: {}", resolucionImagen);

                cr = m_reader.Capture(m_format, m_proc, resolucionImagen, -1); //error no funciona correctamente el lector o metodo del lector
                log.trace("Captura exitosa: {}", cr.quality);

                NotifyListener(ACT_CAPTURE, cr, null, null);
            }

        }catch (UareUException e) 
        {
            m_reader.Reset();
            log.error("Mala captura de huella registrada {}", e.getMessage());
        }catch (InterruptedException ie) 
        {
            log.error("El hilo fue interrumpido: {}", ie.getMessage());
            Thread.currentThread().interrupt(); // Mantén el estado de interrupción del hilo
        }
    }
    
    private boolean estadoEnEspera(boolean bLectorListo) throws UareUException, InterruptedException
    {
        while (!bLectorListo && !m_bCancel) 
        {
            log.info("Estado del lector: {}", m_reader.GetStatus());
            Reader.Status rs = m_reader.GetStatus();
            
            if (rs.status == null) 
            {
                //reader failure
                NotifyListener(ACT_CAPTURE, null, rs, null);
                log.info("Captura fallida...");
                break;
            } 
            
            switch (rs.status) 
            {
                case BUSY:
                    //if busy, wait a bit
                    Thread.sleep(1000);
                    log.info("Se duerme hilo por 1 segundo");
                    break;
                case READY:
                case NEED_CALIBRATION:
                    //ready for capture
                    log.info("Inicia lector");
                    return true;
                default:
                    //reader failure
                    NotifyListener(ACT_CAPTURE, null, rs, null);
                    log.info("Captura fallida...");
                    break;
            }
        }
        return false;
    }

    private void Stream() 
    {
        try 
        {
            Reader.Status rs = m_reader.GetStatus();
            boolean bLectorListo = false;
            
            while (!bLectorListo && !m_bCancel) 
            {
                if (null == rs.status) 
                {
                    //reader failure
                    NotifyListener(ACT_CAPTURE, null, rs, null);
                    log.info("Comparacion fallida");
                    break;
                } 
                
                switch (rs.status) 
                {
                    case BUSY:
                        //if busy, wait a bit
                        Thread.sleep(1000);
                        log.info("Hilo durmiendo por 1 s..."); 
                        break;
                    case READY:
                    case NEED_CALIBRATION:
                        //ready for capture
                        bLectorListo = true;
                        log.info("Comparacion satisfactoria del status de la lectura de huella");
                        break;
                    default:
                        //reader failure
                        NotifyListener(ACT_CAPTURE, null, rs, null);
                        log.info("Comparacion fallida");
                        break;
                }
            }
            
            if (m_bCancel) 
            {
                Reader.CaptureResult cr = new Reader.CaptureResult();
                cr.quality = Reader.CaptureQuality.CANCELED;
                NotifyListener(ACT_CAPTURE, cr, null, null);
                log.info("Captura cancelada.....");
            }
            
            if (bLectorListo) 
            {
                //start streaming
                m_reader.StartStreaming();
                log.info("Lectura de huella en curso");
                //get images
                while (!m_bCancel) 
                {
                    Reader.CaptureResult cr = m_reader.GetStreamImage(m_format, m_proc, m_reader.GetCapabilities().resolutions[0]);
                    NotifyListener(ACT_CAPTURE, cr, null, null);
                    log.info("Lectura exitosa");
                }

                //stop streaming
                m_reader.StopStreaming();
                log.info("Lectura de huella detenida....");
            }
        } catch (UareUException e) 
        {
            NotifyListener(ACT_CAPTURE, null, null, e);
        }catch (InterruptedException e) 
        {
            log.error("Error en la interrupcion de thread {}", e.getMessage());
        }
        
    }

    private void NotifyListener(String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex) {
        final CaptureEvent evt = new CaptureEvent(this, action, cr, st, ex);

        //store last capture event
        m_last_capture = evt;

        if (null == m_listener || null == action || action.equals("")) {
            return;
        }

        //invoke listener on EDT thread
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_listener.actionPerformed(evt);

            }
        });
    }

    public void cancel() {
        m_bCancel = true;
        try {
            if (!m_bStream) {
                m_reader.Close();
            }
            log.info("Captura cancelada");
        } catch (UareUException e) {
        }
    }

    @Override
    public void run() {
        if (m_bStream) {
            Stream();
        } else {
            Capture();
        }
    }

}

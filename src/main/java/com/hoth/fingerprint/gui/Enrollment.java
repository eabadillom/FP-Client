package com.hoth.fingerprint.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

public class Enrollment extends JPanel implements ActionListener 
{
    private static final long serialVersionUID = 2871784652354280317L;
    private static Logger log = LogManager.getLogger(Enrollment.class);
    public static byte[] biometrico = null;
    public static Fmd fmd = null;
    ReaderCollection m_Collection;
    
    public static Fmd getFmd() {
        return fmd;
    }
    
    public static void setFmd(Fmd fmd) {
        Enrollment.fmd = fmd;
    }

    public static byte[] getBiometrico() {
        return biometrico;
    }

    public static void setBiometrico(byte[] biometrico) {
        Enrollment.biometrico = biometrico;
    }
    
    public class EnrollmentThread extends Thread implements Engine.EnrollmentCallback 
    {    
        public static final String ACT_PROMPT = "enrollment_prompt";
        public static final String ACT_CAPTURE = "enrollment_capture";
        public static final String ACT_FEACTURES = "enrollment_feature";
        public static final String ACT_DONE = "enrollment_done";
        public static final String ACT_CANCELED = "enrollment_canceled";

        public class EnrollmentEvent extends ActionEvent {

            private static final long serialVersionUID = 102;

            public Reader.CaptureResult capture_result;
            public Reader.Status reader_status;
            public UareUException exception;
            public Fmd enrollment_fmd;

            public EnrollmentEvent(Object source, String action, Fmd fmd, Reader.CaptureResult cr, Reader.Status st,
                    UareUException ex) {
                super(source, ActionEvent.ACTION_PERFORMED, action);

                capture_result = cr;
                reader_status = st;
                exception = ex;
                enrollment_fmd = fmd;
                /*
				 * public Enrollment( ) {
				 * JButton b=new JButton("click");//create button
				 * b.setBounds(130,100,100, 40);
				 * 
				 * add(b);//adding button on frame
				 * setSize(400,500);
				 * setLayout(null);
				 * setVisible(true);
				 * 
				 * }
                 */
            }
        }

        private final Reader m_reader;
        private EnrollmentCaptureThread m_capture;
        private ActionListener m_listener;
        private boolean m_bCancel;

        protected EnrollmentThread(Reader reader, ActionListener listener) {
            m_reader = reader;
            m_listener = listener;
        }

        public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) 
        {
            log.trace("Levante el lector ....................");

            Engine.PreEnrollmentFmd prefmd = null;

            while (null == prefmd && !m_bCancel) 
            {
                // start capture thread
                m_capture = new EnrollmentCaptureThread(m_reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
                m_capture.start(null);  //Desata el evento de captura para las 4 huellas (diferentes momentos)

                // prompt for finger
                SendToListener(ACT_PROMPT, null, null, null, null); //Se colocan leyendas iniciales para escanear huella o leyendas secundarias

                // wait till done
                m_capture.join(0);

                // check result
                EnrollmentCaptureThread.CaptureEvent evt = m_capture.getLastCaptureEvent();
                log.trace("evento {}", evt.getActionCommand());
                
                if (null == evt.capture_result)
                {
                    // send capture error
                    log.trace("evento ev fue null");
                    SendToListener(ACT_CAPTURE, null, evt.capture_result, evt.reader_status, evt.exception);
                    break;
                }
                
                if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) 
                {
                    // captura cancelada, retorna nulo
                    log.info("La captura fue cancelada");
                    break;
                }
                
                if (null == evt.capture_result.image && Reader.CaptureQuality.GOOD != evt.capture_result.quality)
                {
                    // send quiality result
                    log.info("La imagen es null o la calidad no es GOOD");
                    SendToListener(ACT_CAPTURE, null, evt.capture_result, evt.reader_status, evt.exception);
                    break;
                }
                
                if (null != evt.capture_result.image && Reader.CaptureQuality.GOOD == evt.capture_result.quality) 
                {
                    // acquire engine
                    Engine engine = UareUGlobal.GetEngine();
                    try {
                        // extract features
                        Fmd fmd = engine.CreateFmd(evt.capture_result.image, Fmd.Format.ANSI_378_2004);

                        // return prefmd
                        prefmd = new Engine.PreEnrollmentFmd();
                        prefmd.fmd = fmd;
                        prefmd.view_index = 0;

                        // send sucess
                        SendToListener(ACT_FEACTURES, null, null, null, null);
                        log.trace("Se extrajo el FMD correctamente");
                    } catch (UareUException e) {
                        // send extraction error
                        log.error("Error al extraer FMD", e);
                        SendToListener(ACT_FEACTURES, null, null, null, e);
                    }
                }
                
            }
            log.trace("Sali del metodo getFMD para ir al actionperformand");

            return prefmd;
        }

        public void cancel() {
            m_bCancel = true;
            if (null != m_capture)
                m_capture.cancel();
        }

        private void SendToListener(String action, Fmd fmd, Reader.CaptureResult cr, Reader.Status st,
                UareUException ex) {
            if (null == m_listener || null == action || action.equals(""))
                return;
            final EnrollmentEvent evt = new EnrollmentEvent(this, action, fmd, cr, st, ex);
            // invoke listener on EDT thread
            try {
                javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        m_listener.actionPerformed(evt);
                    }
                });
            } catch (InvocationTargetException | InterruptedException e) {
                log.error("Error: {}", e.toString());
            }
        }

        @Override
        public void run() {
            // acquire engine
            Engine engine = UareUGlobal.GetEngine();

            try {
                m_bCancel = false;
                while (!m_bCancel) { //entra dos veces
                    // run enrolmnet
                    log.info("Entre al metodo Run para crear el template");
                    Fmd fmd = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, this); //llama al metodo GetFmd (en teoria lo debe de llamar 4 veces para hacer el template)
                    log.trace("Entre a evaluar si el fmd esta lleno o vacio en el run para template");
                    // enviar resultado
                    //entra a evaluar pero debe de cancelar o cambiar valor de m_bCancel
                    if(null != fmd) {
                        SendToListener(ACT_DONE, fmd, null, null, null);
                        break;//se añadio break
                    }
                    
                    if(null == fmd){
                        SendToListener(ACT_CANCELED, null, null, null, null);
                        break;
                    }
                }
            } catch (UareUException e) 
            {
                SendToListener(ACT_DONE, null, null, null, e);
                log.error("Error durante la creacion de la informacion e importacion");
            }
        }

    }

    private static final String ACT_Back = "back";

    private EnrollmentThread m_enrollment;
    private Reader m_reader;
    private JDialog m_dlgParent;
    private JTextArea m_text;
    private boolean m_bJustStarted;

    public Enrollment() //Quitamos parametro READER
    { 
        try {
            m_Collection = UareUGlobal.GetReaderCollection();
            m_Collection.GetReaders();
            log.trace("Tamaño Mcollection enrollment: {}", m_Collection.size());
            log.trace("Nombre del lector enrollment es: {}", m_Collection.get(0).GetDescription().name);

            m_reader = m_Collection.get(0);

        } catch (UareUException e) {
            log.info("UareUGlobal.getReaderCollection() {}", e);
            return;
        }

        m_bJustStarted = true;
        m_enrollment = new EnrollmentThread(m_reader, this);
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

        JButton btnBack = new JButton("Salir");
        btnBack.setActionCommand(ACT_Back);
        btnBack.addActionListener(this);
        add(btnBack);
        add(Box.createVerticalStrut(vgap));
        log.trace("valor de m_bJustStarted {}", m_bJustStarted);
        setOpaque(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        log.info("Entrada para enrolamiento valor m_bJustStarted {}", m_bJustStarted);

        if (e.getActionCommand().equals("back")) 
        {
            m_dlgParent.setVisible(false);
        } else {
            EnrollmentThread.EnrollmentEvent evt = (EnrollmentThread.EnrollmentEvent) e;

            switch (e.getActionCommand()) 
            {
                case EnrollmentThread.ACT_PROMPT:
                    EnrollmentEventPrompt();
                    break;
                case EnrollmentThread.ACT_CAPTURE:
                    EnrollmentEventCaptura(evt);
                    break;
                case EnrollmentThread.ACT_FEACTURES:
                    EnrollmentEventFeature(evt);
                    break;
                case EnrollmentThread.ACT_DONE:
                    EnrollmentEventDone(evt);
                    break;
                case EnrollmentThread.ACT_CANCELED:
                    // canceled, destroy dialog
                    m_dlgParent.setVisible(false);
                    break;
                default:
                    break;
            }

            // cancel enrollment if any exception or bad reader status
            if (null != evt.exception) {
                m_dlgParent.setVisible(false);
            } 
            
            if (null != evt.reader_status && Reader.ReaderStatus.READY != evt.reader_status.status
                    && Reader.ReaderStatus.NEED_CALIBRATION != evt.reader_status.status) {
                m_dlgParent.setVisible(false);
            }
        }
    }
    
    private void EnrollmentEventPrompt()
    {
        log.info("Entre a la accion ACT_PROMPT...................");
        if (m_bJustStarted) {
            m_text.append("Registro preparado \n\n");
            m_text.append("Coloca tu dedo en el lector \n");
        } else {
            m_text.append("Coloca el mismo dedo en el lector \n");
        }   m_bJustStarted = false;
    }
    
    private void EnrollmentEventCaptura(EnrollmentThread.EnrollmentEvent evt)
    {
        if (null != evt.capture_result) 
        {
            MessageBox.BadQuality(evt.capture_result.quality);    
        } 

        if (null != evt.exception) 
        {
            MessageBox.DpError(evt.exception.getMessage(), evt.exception); //error 21
            log.info(evt.exception.getStackTrace());
        } 

        if (null != evt.reader_status) 
        {
            MessageBox.BadStatus(evt.reader_status);
        }   m_bJustStarted = false;
    }
    
    private void EnrollmentEventFeature(EnrollmentThread.EnrollmentEvent evt)
    {
        log.info("Entre a la accion ACT_FEACTURES...................");
        if (null == evt.exception) {
            m_text.append("Huella capturada, características extraidas \n\n");
        }

        if(null != evt.exception){
            MessageBox.DpError("Enrollment extraction", evt.exception);
            //JOptionPane.showMessageDialog(f, "WARNING");
        }   m_bJustStarted = false;
    }
    
    private void EnrollmentEventDone(EnrollmentThread.EnrollmentEvent evt)
    {
        if (null == evt.exception) {
            String str = String.format("Plantilla creada, tamaño: %d \n\n\n",
                    evt.enrollment_fmd.getData().length);
            log.info("Huella capturada: {}", evt.enrollment_fmd.getData());
            m_text.append(str);
            try {
                fmd = evt.enrollment_fmd;
                biometrico = evt.enrollment_fmd.getData();
                //m_enrollment.cancel();
                m_reader.Close();

                //m_dlgParent.setVisible(false);
            } catch (UareUException e1) {
                e1.printStackTrace();
            }
        }

        if(null != evt.exception){
            MessageBox.DpError("Enrollment template creation", evt.exception);
            //JOptionPane.showMessageDialog(f, "Error H");
        }   
        m_bJustStarted = true;
    }

    /*private void StopCaptureThread(){
        if(null != m_enrollment) m_enrollment.cancel();
    }
        
	private void WaitForCaptureThread(){
		if(null != m_enrollment)
			try {
				m_enrollment.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}*/
///revisar abajo
    private void doModal(JDialog dlgParent) 
    {
        // open reader
        try {
            m_reader.Open(Reader.Priority.COOPERATIVE);
        } catch (UareUException e) {
            //JOptionPane.showMessageDialog(f, "DO modal");
            MessageBox.DpError("Reader.Open()", e);
            log.error("Reader.Reader() {}", e.toString());
        }

        //COMIENZA ENROLAMIENTO DE HILO
        log.info("Entro al metodo run de CaptureThread");
        m_enrollment.start();
        
        // bring up modal dialog
        m_dlgParent = dlgParent;
        m_dlgParent.setContentPane(this);
        m_dlgParent.pack();
        m_dlgParent.setAlwaysOnTop(true);
        m_dlgParent.setLocationRelativeTo(null);
        m_dlgParent.setVisible(true);
        m_dlgParent.dispose();
        // stop enrollment thread
        m_enrollment.cancel();  //CANCELAR DESDE AQUI O DESDE ACT_DONE
        m_dlgParent.setVisible(false);
        //StopCaptureThread();
        //WaitForCaptureThread();
        
        try {
            UareUGlobal.DestroyReaderCollection();
        } catch (UareUException e) {
            MessageBox.DpError("UareUGlobal.destroyReaderCollection()", e);
        }

        // close reader
        /*try {
            m_reader.Close();
        } catch (UareUException e) {
            //JOptionPane.showMessageDialog(f, e);
            //MessageBox.DpError("Reader.Close()", e);
            log.error("Error al tratar de terminar la captura: {}", e);
        }*/
    }
    
    public static void Run() {
        log.info("Entre a metodo run de Enrollment");
        JDialog dlg = new JDialog((JDialog) null, "Registro", true);
        log.info("Entrada a enroll: ");
        Enrollment enrollment = new Enrollment();
        enrollment.doModal(dlg);
        dlg.getRootPane();
    }

}

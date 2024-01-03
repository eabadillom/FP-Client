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
	
	public class CaptureEvent extends ActionEvent{

		private static final long serialVersionUID = 101;
		public Reader.CaptureResult capture_result;
		public Reader.Status        reader_status;
		public UareUException       exception;
		

		public CaptureEvent(Object source, String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex){
			super(source, ActionEvent.ACTION_PERFORMED, action);
			capture_result = cr;
			reader_status = st;
			exception = ex;
		}
	}
	
	private ActionListener m_listener;
	private boolean m_bCancel;
	private Reader  m_reader;
	private boolean m_bStream;
	private Fid.Format             m_format;
	private Reader.ImageProcessing m_proc;
	private CaptureEvent m_last_capture;
	
	public EnrollmentCaptureThread(Reader reader, boolean bStream, Fid.Format img_format, Reader.ImageProcessing img_proc){
		
		m_bCancel = false;
		m_reader = reader;
		m_bStream = bStream;
		m_format = img_format;
		m_proc = img_proc;
	}
	
	public void start(ActionListener listener){
		m_listener = listener;		
		super.start();
	}

	public void join(int milliseconds){
		try{
			super.join(milliseconds);
		} 
		catch(InterruptedException e){ e.printStackTrace(); }
	}
	
	public CaptureEvent getLastCaptureEvent(){
		return m_last_capture;
	}
	
	

	public void Capture(){
		
		try{
			//wait for reader to become ready
			boolean bReady = false;
			while(!bReady && !m_bCancel){
				log.info("estado del lector: {}",m_reader.GetStatus());
				Reader.Status rs = m_reader.GetStatus();
				if(Reader.ReaderStatus.BUSY == rs.status){
					//if busy, wait a bit
					try{
						Thread.sleep(100);
                        log.info("Se duerme thread por 1 segundo");
					} 
					catch(InterruptedException e) {
						e.printStackTrace();
						break; 
					}
				}
				else if(Reader.ReaderStatus.READY == rs.status || Reader.ReaderStatus.NEED_CALIBRATION == rs.status){
					//ready for capture
					bReady = true;
                    log.info("inicia lector");
					break;
				}
				else{
					//reader failure
					NotifyListener(ACT_CAPTURE, null, rs, null);
                    log.info("captura fallida...");
					break;
				}
			}
			if(m_bCancel){
				Reader.CaptureResult cr = new Reader.CaptureResult();
				cr.quality = Reader.CaptureQuality.CANCELED;                
				NotifyListener(ACT_CAPTURE, cr, null, null);
                log.info("captura cancelada.....");
			}

			
			if(bReady){
				//capture
				log.info("Iniciando captura de huella para enrolamiento");
				log.info("format: {}", m_format);
				log.info("proceso de imagen: {}", m_proc);
				log.info("lector: {}", m_reader.GetStatus());
				
				
				Reader.CaptureResult cr = m_reader.Capture(m_format, m_proc, m_reader.GetCapabilities().resolutions[0], -1);
				log.info("captura hecha de enrolamiento");
				NotifyListener(ACT_CAPTURE, cr, null, null);
				
				
				
				/*try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				

                log.info("Huella capturada....");
				log.info("valor de la huella detectada {}",cr);
				
			}
		}
		catch(UareUException e){
			NotifyListener(ACT_CAPTURE, null, null, e);
		}
		
		

	}

	
	private void Stream(){
		try{
			//wait for reader to become ready
			boolean bReady = false;
			while(!bReady && !m_bCancel){
				Reader.Status rs = m_reader.GetStatus();
				if(Reader.ReaderStatus.BUSY == rs.status){
					//if busy, wait a bit
					try{
						Thread.sleep(100);
                        log.info("Thread durmiendo por 1 s...");
					} 
					catch(InterruptedException e) {
						e.printStackTrace();
						break; 
					}
				}
				else if(Reader.ReaderStatus.READY == rs.status || Reader.ReaderStatus.NEED_CALIBRATION == rs.status){
					//ready for capture
					bReady = true;
                    log.info("comparacion satisfactoria del status de la lectura de huella");
					break;
				}
				else{
					//reader failure
					NotifyListener(ACT_CAPTURE, null, rs, null);
                    log.info("comparacion fallida");
					break;
				}
			}
			
			if(bReady){
				//start streaming
				m_reader.StartStreaming();
                log.info("lectura de huella en curso");
				//get images
				while(!m_bCancel){
					Reader.CaptureResult cr = m_reader.GetStreamImage(m_format, m_proc, m_reader.GetCapabilities().resolutions[0]);
					NotifyListener(ACT_CAPTURE, cr, null, null);
                    log.info("lectura exitosa");
				}
				
				//stop streaming
				m_reader.StopStreaming();
                log.info("lectura de huella detenida....");
			}
		}
		catch(UareUException e){
			NotifyListener(ACT_CAPTURE, null, null, e);
		}

		if(m_bCancel){
			Reader.CaptureResult cr = new Reader.CaptureResult();
			cr.quality = Reader.CaptureQuality.CANCELED;
			NotifyListener(ACT_CAPTURE, cr, null, null);
            
		}
	}
	
	private void NotifyListener(String action, Reader.CaptureResult cr, Reader.Status st, UareUException ex){
		final CaptureEvent evt = new CaptureEvent(this, action, cr, st, ex);
		
		//store last capture event
		m_last_capture = evt; 

		if(null == m_listener || null == action || action.equals("")) return;
		
		//invoke listener on EDT thread
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				m_listener.actionPerformed(evt);

			}
		});
	}
	
	public void cancel(){
		m_bCancel = true;
		try{
			if(!m_bStream) 
			m_reader.CancelCapture();
		}
		catch(UareUException e){}
	} 
	
	public void run(){
		if(m_bStream){
			Stream();
		}
		else{
			Capture();
		}
	}
    
}


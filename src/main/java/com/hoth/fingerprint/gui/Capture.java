package com.hoth.fingerprint.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.*;

public class Capture extends JPanel implements ActionListener
{
    private static Logger log = LogManager.getLogger(Capture.class);
	private static final long serialVersionUID = 2;
	//private static final String ACT_BACK = "back";

    ReaderCollection m_Collection;
    Reader reader;

	private JDialog       m_dlgParent;
	private CaptureThread m_capture;
	private Reader        m_reader;
	private ImagePanel    m_image;
	private boolean       m_bStreaming = false;
	private JLabel        label;
	private JLabel        focusLabel;

	public static Reader.CaptureResult captura;
	
	
	public static Reader.CaptureResult getCaptura() {
		return captura;
	}

	public static void setCaptura(Reader.CaptureResult captura) {
		Capture.captura = captura;
	}

	Capture(){
		
        try {
            m_Collection = UareUGlobal.GetReaderCollection();
            m_Collection.GetReaders();
            log.trace("Tamaño Mcollection: {}",m_Collection.size());
            log.debug("Nombre del lector es: {}", m_Collection.get(0).GetDescription().name);
    
            m_reader = m_Collection.get(0);
            
        } catch (UareUException e) {
            log.error("UareUGlobal.getReaderCollection() {}", e);		
                return;
        }
		
		m_capture = new CaptureThread(m_reader, m_bStreaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);

		final int vgap = 5;
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);

		focusLabel = new JLabel();	
		focusLabel.setName("focusLabel");	
		focusLabel.setSize(200, 200);		
		focusLabel.setIcon(new ImageIcon(getClass().getResource("/images/information.png")));
		focusLabel.setText("Toque esta ventana para continuar.");
		focusLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(focusLabel);

		m_image = new ImagePanel();
		Dimension dm = new Dimension(400, 500);
		m_image.setPreferredSize(dm);
		add(m_image);
		add(Box.createVerticalStrut(vgap));

		label = new JLabel();	
		label.setName("label1");	
		label.setSize(200, 200);		
		label.setIcon(new ImageIcon(getClass().getResource("/images/huella.gif")));
		label.setText("Coloca tu huella en el lector");
		label.setFont(new Font("Tahoma", Font.BOLD, 18));						
		add(label);
	}

	private void StartCaptureThread(JDialog dlg) {
		JLabel labelstart = new JLabel();
		if (m_capture != null) {
			try {
				m_capture = new CaptureThread(m_reader, m_bStreaming, Fid.Format.ANSI_381_2004,
						Reader.ImageProcessing.IMG_PROC_DEFAULT);
				m_capture.start(this, dlg);
				labelstart.setText("Escanea tu huella.......");

			} catch (Exception e) {
				labelstart.setText("La huella no fue capturada");
			}
		}
	}

	private void StopCaptureThread(){
		if(null != m_capture) m_capture.cancel();
	}
	
	private void WaitForCaptureThread(){
		if(null != m_capture) m_capture.join(1000);
	}
	
	public static Reader.CaptureResult Captura(Reader.CaptureResult rc){
		return rc;
	}


	public void actionPerformed(ActionEvent e){
		log.trace("Entra a actionPerformed...");
		 if(e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)){
			//event from capture thread
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent)e;
			boolean bCanceled = false;
			
			if(null != evt.capture_result){
				boolean bGoodImage = false;
				if(null != evt.capture_result.image){
					if(m_bStreaming && (Reader.CaptureQuality.GOOD == evt.capture_result.quality || Reader.CaptureQuality.NO_FINGER == evt.capture_result.quality)) bGoodImage = true;
					if(!m_bStreaming && Reader.CaptureQuality.GOOD == evt.capture_result.quality) bGoodImage = true;
				}
				if(bGoodImage){
					//display image
					
					m_image.showImage(evt.capture_result.image);
					log.debug("imagen capturada.....");
					
					captura = evt.capture_result;
					
					
				}
				else if(Reader.CaptureQuality.CANCELED == evt.capture_result.quality){
					//capture or streaming was canceled, just quit
					bCanceled = true;
					log.debug("cancelado {}",bCanceled);
				}
				else{
					//bad quality
					log.debug(evt.capture_result.quality);
				}
			}
			else if(null != evt.exception){
				//exception during capture
				log.error("Capture",  evt.exception);
				bCanceled = true;
			}
			else if(null != evt.reader_status){
				log.debug(evt.reader_status);
				bCanceled = true;
			}
		}
	}


	private void doModal(JDialog dlgParent){
		//open reader
		try {
			m_reader.Open(Reader.Priority.COOPERATIVE);
		} catch (UareUException e) {
			log.info("Reader.Open()", e);
		}
		
		boolean bOk = true;
		if(m_bStreaming){
			//check if streaming supported
			Reader.Capabilities rc = m_reader.GetCapabilities();
			if(null != rc && !rc.can_stream){
				log.info("This reader does not support streaming");
				bOk = false;
			}
		}
		
		if(bOk){
			//start capture thread
			StartCaptureThread(dlgParent);
	
			//bring up modal dialog
			m_dlgParent = dlgParent;
			
			m_dlgParent.setContentPane(this);	
			m_dlgParent.setAlwaysOnTop(true);		
			m_dlgParent.pack();
			m_dlgParent.setLocationRelativeTo(null);			
			m_dlgParent.setSize(400,550);			
			m_dlgParent.toFront();
			m_dlgParent.dispose();				
			m_dlgParent.setVisible(true);		
			
			//cancel capture
			StopCaptureThread();
			//wait for capture thread to finish
			WaitForCaptureThread();
		}
		
		try {
			UareUGlobal.DestroyReaderCollection();
		} catch (UareUException e) {
			MessageBox.DpError("UareUGlobal.destroyReaderCollection()", e);
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
	
	public static void Run(){
    	JDialog dlg = new JDialog((JDialog)null, "Lectura de huella", true);
    	Capture capture = new Capture();

		dlg.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				capture.addFocusLabel();
			}

			public void windowGainedFocus(WindowEvent e) {
				capture.removeFocusLabel();
			}
		});

    	capture.doModal(dlg);
	}

	
}

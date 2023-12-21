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

public class Enrollment extends JPanel implements ActionListener {
	private static Logger log = LogManager.getLogger(Enrollment.class);
	
	public static byte[] biometrico = null;
	public static Fmd fmd = null;
	
	public static Fmd getFmd() {
		return fmd;
	}

	private static final long serialVersionUID = 2871784652354280317L;

	public static void setFmd(Fmd fmd) {
		Enrollment.fmd = fmd;
	}


	public static byte[] getBiometrico() {
		return biometrico;
	}


	public static void setBiometrico(byte[] biometrico) {
		Enrollment.biometrico = biometrico;
	}

	ReaderCollection m_Collection;

	public class EnrollmentThread extends Thread implements Engine.EnrollmentCallback {

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

		public Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
			Engine.PreEnrollmentFmd prefmd = null;

			while (null == prefmd && !m_bCancel) {
				// start capture thread
				m_capture = new EnrollmentCaptureThread(m_reader, false, Fid.Format.ANSI_381_2004,
						Reader.ImageProcessing.IMG_PROC_DEFAULT);
				m_capture.start(null);

				// prompt for finger
				SendToListener(ACT_PROMPT, null, null, null, null);

				// wait till done
				m_capture.join(0);

				// check result
				EnrollmentCaptureThread.CaptureEvent evt = m_capture.getLastCaptureEvent();
				if (null != evt.capture_result) {
					if (Reader.CaptureQuality.CANCELED == evt.capture_result.quality) {
						// captura cancelada, retorna nulo
						break;
					} else if (null != evt.capture_result.image
							&& Reader.CaptureQuality.GOOD == evt.capture_result.quality) {
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

						} catch (UareUException e) {
							// send extraction error
							SendToListener(ACT_FEACTURES, null, null, null, e);
						}

					} else {
						// send quiality result
						SendToListener(ACT_CAPTURE, null, evt.capture_result, evt.reader_status, evt.exception);
					}
				} else {
					// send capture error
					SendToListener(ACT_CAPTURE, null, evt.capture_result, evt.reader_status, evt.exception);
				}
			}

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
					public void run() {
						m_listener.actionPerformed(evt);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			// acquire engine
			Engine engine = UareUGlobal.GetEngine();

			try {
				m_bCancel = false;
				while (!m_bCancel) {
					// run enrolmnet
					Fmd fmd = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, this);

					// enviar resultado
					if (null != fmd) {
						SendToListener(ACT_DONE, fmd, null, null, null);
					} else {
						SendToListener(ACT_CANCELED, null, null, null, null);
						break;
					}
				}
			} catch (UareUException e) {

				SendToListener(ACT_DONE, null, null, null, e);

			}
		}

	}

	private static final long serialVersionUID = 6;
	private static final String ACT_Back = "back";

	private EnrollmentThread m_enrollment;
	private Reader m_reader;
	private JDialog m_dlgParent;
	private JTextArea m_text;
	private boolean m_bJustStarted;

	public Enrollment() { //Quitamos parametro READER
		
		try {
			m_Collection = UareUGlobal.GetReaderCollection();
            m_Collection.GetReaders();
            log.info("Tamaño Mcollection enrollment: {}",m_Collection.size());
            log.info("Nombre del lector enrollment es: {}", m_Collection.get(0).GetDescription().name);
			
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

		JButton btnBack = new JButton("BACK");
		btnBack.setActionCommand(ACT_Back);
		btnBack.addActionListener(this);
		add(btnBack);
		add(Box.createVerticalStrut(vgap));
		log.info("valor de m_bJustStarted {}", m_bJustStarted);
		setOpaque(true);
	}


	public void actionPerformed(ActionEvent e) {
		
		log.info("Entrada para enrolamiento valor m_bJustStarted {}", m_bJustStarted);
		EnrollmentThread.EnrollmentEvent evt = (EnrollmentThread.EnrollmentEvent) e;
		
		if (e.getActionCommand().equals(EnrollmentThread.ACT_PROMPT)) {
			if (m_bJustStarted) {
				m_text.append("Enrollment preparado \n");
				m_text.append("Coloca tu dedo en el lector \n");
			} else {
				m_text.append("Coloca el mismo dedo en el lector \n");
			}
			m_bJustStarted = false;
		} else if (e.getActionCommand().equals(EnrollmentThread.ACT_CAPTURE)) {
			if (null != evt.capture_result) {
				MessageBox.BadQuality(evt.capture_result.quality);
				
			}
			else if(null != evt.exception){
				MessageBox.DpError("Captures", evt.exception);
			}
			else if(null != evt.reader_status){
				MessageBox.BadStatus(evt.reader_status);
			}
			m_bJustStarted = false;
		} else if (e.getActionCommand().equals(EnrollmentThread.ACT_FEACTURES)) {
			if (null == evt.exception) {
				m_text.append("fingerprint captured, features extracted \n\n");
			} else {
				MessageBox.DpError("Enrollment extraction", evt.exception);
				//JOptionPane.showMessageDialog(f, "WARNING");
			}
			m_bJustStarted = false;
		} else if (e.getActionCommand().equals(EnrollmentThread.ACT_DONE)) {
			if (null == evt.exception) {
				String str = String.format("enrollment template created, size: %d \n\n\n",
						evt.enrollment_fmd.getData().length);
				log.info("Huella capturada: {}", evt.enrollment_fmd.getData());
				m_text.append(str);
				try {
					fmd = evt.enrollment_fmd;
					biometrico = evt.enrollment_fmd.getData();
					m_enrollment.cancel();
					m_reader.Close();					
					m_dlgParent.setVisible(false);
					
				} catch (UareUException e1) {						
					e1.printStackTrace();
				}
			} else {
				MessageBox.DpError("Enrollment template creation", evt.exception);
				//JOptionPane.showMessageDialog(f, "Error H");
			}
			m_bJustStarted = true;
		} else if (e.getActionCommand().equals(EnrollmentThread.ACT_CANCELED)) {
			// canceled, destroy dialog
			m_dlgParent.setVisible(false);
		}
		// cancel enrollment if any exception or bad reader status
		if (null != evt.exception) {
			m_dlgParent.setVisible(false);
		} else if (null != evt.reader_status && Reader.ReaderStatus.READY != evt.reader_status.status
				&& Reader.ReaderStatus.NEED_CALIBRATION != evt.reader_status.status) {
			m_dlgParent.setVisible(false);
		}		

	}
///revisar abajo
	private void doModal(JDialog dlgParent) {
		// open reader
		try {
			m_reader.Open(Reader.Priority.COOPERATIVE);
		} catch (UareUException e) {
			//JOptionPane.showMessageDialog(f, "DO modal");
			MessageBox.DpError("Reader.Open()", e);
		}

		//COMIENZA ENROLAMIENTO DE HILO
		m_enrollment.start();

		// bring up modal dialog
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();

		// stop enrollment thread
		//m_enrollment.cancel();

		// close reader
		/*try {
			m_reader.Close();
		} catch (UareUException e) {
			//JOptionPane.showMessageDialog(f, e);
			MessageBox.DpError("Reader.Close()", e); 
		}*/
	}

	public static void Run() {
		JDialog dlg = new JDialog((JDialog) null, "Enrollment", true);
		log.info("entrada a enroll: ");
		Enrollment enrollment = new Enrollment();
		enrollment.doModal(dlg);
	}
}

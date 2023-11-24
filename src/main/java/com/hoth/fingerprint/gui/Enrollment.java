package com.hoth.fingerprint.gui;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

public class Enrollment extends JFrame {
	private static Logger log = LogManager.getLogger(Enrollment.class);
	private ReaderCollection m_Collection = null;

	private static final long serialVersionUID = 2871784652354280317L;

	JFrame f;
	
	public Enrollment( ) {
		JButton b=new JButton("click");//create button  
		b.setBounds(130,100,100, 40);  
		          
		add(b);//adding button on frame  
		setSize(400,500);
		setLayout(null);
		setVisible(true);
		
		try {
			m_Collection = UareUGlobal.GetReaderCollection();
			
			for(Reader reader : m_Collection) {
				log.info(reader.GetDescription().name);
				System.out.println(reader.GetDescription().name);
			}
			
		} catch(UareUException ex) {
			log.error("", ex);
		}
		
		
	}
}
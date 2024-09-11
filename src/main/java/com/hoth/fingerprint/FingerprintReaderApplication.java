package com.hoth.fingerprint;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.TimeZone;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;



@SpringBootApplication
public class FingerprintReaderApplication extends JFrame {
    //private static Logger log = LogManager.getLogger(FingerprintReaderApplication.class);
    private static final long serialVersionUID = -3453006228197423647L;


	public static void main(String[] args) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-6"));
		//SpringApplication.run(FingerprintReaderApplication.class, args);
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(FingerprintReaderApplication.class)
				.headless(false).web(WebApplicationType.SERVLET).run(args);
		
		EventQueue.invokeLater(() -> {
            FingerprintReaderApplication ex = ctx.getBean(FingerprintReaderApplication.class);
            ex.setVisible(false);
        });
	}
	
	public FingerprintReaderApplication() {
        initUI();
    }
	
	private void initUI() {

		JButton quitButton = new JButton("Quit");

        quitButton.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });

        createLayout(quitButton);
        setTitle("Quit button");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


    }
	
	private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );
    }
    
}

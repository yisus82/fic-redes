import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class Ventana extends JFrame {
     
      private JTextField remitente;
	private JTextField destinatario;
	private JTextField asunto;
      private JTextField cc;
	private JTextArea mensaje;
	private JButton aceptar;
	private JButton cancelar;
	private JLabel e_rem;
	private JLabel e_des;
	private JLabel e_asu;
	private JLabel e_men;
      private JLabel e_cc;
     

	public Ventana() {
		
		remitente = new JTextField(" ",30);
            destinatario = new JTextField(" ",30);
		asunto = new JTextField(" ",30);
            cc = new JTextField(" ",150);
		mensaje = new JTextArea(50,50);
            e_rem = new JLabel("Remitente: ");
		e_des = new JLabel("Destinatario: ");
		e_asu = new JLabel("Asunto: ");
		e_men = new JLabel("Mensaje: "); 
            e_cc = new JLabel("Cc: ");

	      aceptar = new JButton("Enviar");
 		cancelar = new JButton("Cancelar");

            getContentPane().setLayout(null);
            setTitle("Cliente SMTP");
		addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    exitForm(evt);
                }

            });
   	     

          
            getContentPane().add(e_rem);
            e_rem.setBounds(50,50,80,28);  

		getContentPane().add(e_des);
            e_des.setBounds(50,100,80,28); 

		getContentPane().add(e_cc);
            e_cc.setBounds(50,150,80,28); 

		getContentPane().add(e_asu);
            e_asu.setBounds(50,200,80,28);           

		getContentPane().add(e_men);
            e_men.setBounds(50,250,80,28);

            getContentPane().add(aceptar);
            aceptar.setBounds(150,540,100,40);
		aceptar.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   aceptarMouseClicked(evt);
              }
             });



		getContentPane().add(cancelar);
            cancelar.setBounds(320,540,100,40);
		cancelar.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   cancelarMouseClicked(evt);
              }
             });


            getContentPane().add(remitente);
            remitente.setBounds(150,50,180,28);  
		remitente.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   remitenteMouseClicked(evt);
              }
             });
         

		getContentPane().add(destinatario);
            destinatario.setBounds(150,100,180,28);
		destinatario.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   destinatarioMouseClicked(evt);
              }
             });

		
		getContentPane().add(cc);
            cc.setBounds(150,150,300,28);
		cc.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   ccMouseClicked(evt);
              }
             });

            
		getContentPane().add(asunto);
            asunto.setBounds(150,200,180,28);
		asunto.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   asuntoMouseClicked(evt);
              }
             });
              

		getContentPane().add(mensaje);
            mensaje.setEnabled(true);
		mensaje.setEditable(true);
            mensaje.setBounds(150,250,300,250);
		mensaje.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   mensajeMouseClicked(evt);
              }
             });



		
    }

            private void remitenteMouseClicked(MouseEvent evt) {
                 remitente.setText(" ");      
             }

		private void destinatarioMouseClicked(MouseEvent evt) {
                 destinatario.setText(" ");      
             }


		private void ccMouseClicked(MouseEvent evt) {
                 cc.setText(" ");      
             }


		private void asuntoMouseClicked(MouseEvent evt) {
                 asunto.setText(" ");      
             }


		private void mensajeMouseClicked(MouseEvent evt) {
                 mensaje.setText("");      
             }

		private void cancelarMouseClicked(MouseEvent evt) {
                 remitente.setText(" ");
                 destinatario.setText(" ");
		     asunto.setText(" ");
		     mensaje.setText("");
                 cc.setText(" ");      
             }

 
		private void aceptarMouseClicked(MouseEvent evt) {
                 new Cliente(remitente.getText(),destinatario.getText(),cc.getText(),mensaje.getText(),asunto.getText());  
             }


            private void exitForm(WindowEvent evt) {
                  System.exit(0);
              }



       }
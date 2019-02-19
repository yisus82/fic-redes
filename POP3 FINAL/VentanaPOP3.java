import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class VentanaPOP3 extends JFrame {
     
      private JTextField login;
	private JPasswordField pass;
      private JTextField mens_borrar;
      private JButton conectar;
	private JButton cancelar;
	private JLabel e_login;
	private JLabel e_pass;
      private JCheckBox c_borrar;
      private TextArea area;


	public VentanaPOP3() {
		
		login = new JTextField(" ",20);
            pass = new JPasswordField("",20);
		e_login = new JLabel("Login: ");
		e_pass = new JLabel("Password: ");
            mens_borrar = new JTextField(" ",20);
            c_borrar = new JCheckBox(" Marcar la casilla para borrar los mensajes ",false);
 
            area = new TextArea(100,100);

		conectar = new JButton("Conectar");
 		cancelar = new JButton("Cancelar");

		getContentPane().setLayout(null);
            setTitle("Cliente POP3");
		addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    exitForm(evt);
                }

            });


            getContentPane().add(area);
		area.setEnabled(true); 
		area.setEditable(false);
            area.setBounds(80,180,450,320);


		getContentPane().add(e_login);
            e_login.setBounds(50,50,80,28);  

		getContentPane().add(e_pass);
            e_pass.setBounds(50,100,80,28); 

            getContentPane().add(c_borrar);
            c_borrar.setBounds(50,550,350,28);


		getContentPane().add(conectar);
            conectar.setBounds(150,630,100,40);
		conectar.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   conectarMouseClicked(evt);
              }
             });

		getContentPane().add(cancelar);
            cancelar.setBounds(310,630,100,40);
		cancelar.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   cancelarMouseClicked(evt);
              }
             });

		getContentPane().add(login);
            login.setBounds(150,50,180,28);  
		login.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   loginMouseClicked(evt);
              }
             });
         

		getContentPane().add(pass);
            pass.setBounds(150,100,180,28);
		pass.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   passMouseClicked(evt);
              }
            });


        }


            public void setEstado(boolean b) {
                 c_borrar.setSelected(b);
            }

            public boolean esSeleccionado() {
                 return c_borrar.isSelected();
            }

            public void anhadeTexto(String texto) {
                 area.append(texto);
            }

		private void loginMouseClicked(MouseEvent evt) {
                 login.setText(" ");      
             }

		private void passMouseClicked(MouseEvent evt) {
                 pass.setText("");      
             } 
		
		private void cancelarMouseClicked(MouseEvent evt) {
                 login.setText(" ");
                 pass.setText("");
                 area.setText("");
                 mens_borrar.setText(" ");      
             }

		
		private void conectarMouseClicked(MouseEvent evt) {
                 new Pop3(login.getText(),pass.getPassword(),this);  
             }


            private void exitForm(WindowEvent evt) {
                  System.exit(0);
              }

        
 }




package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Mensagem;
import util.Status;

public class ClientAssinc {

    Socket socket;
    Thread thread;
    ObjectInputStream input;
    ObjectOutputStream output;
    Boolean ligado;

    //Controla a vez de jogar
    Boolean vezDeJogar;

    public ClientAssinc() {
        vezDeJogar = false;

    }

    public void conectaServidor(String host, int porta) throws IOException {
        socket = new Socket(host, porta);
        input = new ObjectInputStream(socket.getInputStream());
        output = new ObjectOutputStream(socket.getOutputStream());
        ligado = false;

    }

    public void disparaThread() {
        thread = new Thread() {
            @Override
            public void run() {
                while (ligado) {
                    try {
                        Mensagem m = (Mensagem) input.readObject();

                        System.out.println("Mensagem recebida:\n" + m);

                        //deve-se tratar a mensagem chegada aqui...
                        if (m.getOperacao().equals("VEZDEJOGAR")) {
                            vezDeJogar = true;
                        }

                        if (m.getOperacao().equals("SAIRREPLY") && m.getStatus() == Status.OK) {
                            ligado = false;
                        }

                    } catch (IOException ex) {
                        System.out.println("Erro ao receber mensagem do servidor: " + ex.getMessage());
                        ligado = false;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientAssinc.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        thread.start();
    }

    public void controlaJogo() throws IOException, ClassNotFoundException {

        Mensagem m = new Mensagem("ENTRARNOJOGO");
        m.setParam("nome", "Henrique");
        output.writeObject(m);
        
        m = (Mensagem) input.readObject();
        
        if (m.getOperacao().equals("ENTRARNOJOGOREPLY") && m.getStatus() == Status.OK) {
            ligado = true;
            disparaThread();
        }
        
        while (ligado) {

            Scanner scanner = new Scanner(System.in);
            System.out.println("______MENU_______\n"
                    + " 1 - ATIRAR: \n"
                    + " 2 - STATUS: \n"
                    + " 3 - RANKING: \n"
                    + " 4 - SAIR:    \n"
                    + "Opção:   ");
            int op = scanner.nextInt();

            switch (op) {
                case 1:
                    if (vezDeJogar) {
                        System.out.println("DIGITE O VALOR DE X (0 a 9) ");
                        int x = scanner.nextInt();
                        System.out.println("DIGITE O VALOR DE Y (0 a 9) ");
                        int y = scanner.nextInt();

                        m = new Mensagem("JOGADA");
                        m.setParam("x", x);
                        m.setParam("y", y);
                        output.writeObject(m);

                        //tratar a resposta                      
                        if (m.getOperacao().equals("JOGADAREPLY") && m.getStatus() == Status.OK) {
                           if(m.getParam("res")=="AGUA")
                               vezDeJogar= false;
                        }
                    } else {
                        System.out.println("Não é sua vez de jogar!");
                    }
                    break;
                case 2:
                    m = new Mensagem("STATUS");
                    output.writeObject(m);
                    output.flush();
                    break;
                case 3:
                    m = new Mensagem("RANKING");
                    output.writeObject(m);
                    output.flush();
                    break;
                case 4:
                    m = new Mensagem("SAIR");
                    output.writeObject(m);
                    break;
            }
        }
    }

    public void desliga() {
        ligado = false;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ClientAssinc cliente = new ClientAssinc();

        try {
            cliente.conectaServidor("localhost", 5555);
            cliente.controlaJogo();

        } catch (IOException ex) {
            Logger.getLogger(ClientAssinc.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro no cliente: " + ex.getMessage());
            cliente.desliga();
        }

    }

}

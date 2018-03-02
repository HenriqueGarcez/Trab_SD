/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import battleship.Tabuleiro;
import battleship.TiroEnum;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Estados;
import util.Mensagem;
import util.Status;

/**
 *
 * @author elder
 */
public class TrataConexao implements Runnable {

    private String nome;
    private Integer pontuação;
    Boolean primeiro = false;
    String DesenhoTabuleiro = "";
    private Estados estado;
    private ObjectOutputStream output;

    @Override
    public void run() {
        try {
            trataConexao(socket);
        } catch (IOException ex) {
            System.out.println("Erro no tratamento do cliente: " + socket.toString() + ": " + ex.getMessage());
        } catch (ClassNotFoundException ex) {

        } catch (Exception ex) {
            Logger.getLogger(TrataConexao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ObjectOutputStream getOutput() {
        return output;
    }

    public void setOutput(ObjectOutputStream output) {
        this.output = output;
    }

    public Estados getEstado() {
        return estado;
    }

    public void setEstado(Estados estado) {
        this.estado = estado;
    }

    private void trataConexao(Socket socket) throws IOException, ClassNotFoundException, Exception {
        // * Cliente ------SOCKET-----servidor
        //protocolo da aplicação
        /*
         4 - Tratar a conversação entre cliente e 
         servidor (tratar protocolo);
         */

        try {
            /* 3 - Criar streams de entrada e saída;*/

            output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            System.out.println("Tratando...");
            estado = Estados.CONECTADO;
            while (estado != Estados.SAIR) {
                server.avisa();
                Mensagem m = (Mensagem) input.readObject();
                System.out.println("Mensagem do cliente:\n" + m.getOperacao());

                String operacao = m.getOperacao();
                Mensagem reply = new Mensagem(operacao + "REPLY");
                //estados conectado autenticado
                switch (estado) {
                    case CONECTADO:
                        switch (operacao) {
                            case "ENTRARNOJOGO":
                                //pega o nome do cliente
                                nome = (String) m.getParam("nome");
                                reply.setStatus(Status.OK);
                                //entrar na fila
                                server.addJogadorFila(this);
                                //testa se o 1º
                                if (server.eprimeiro(this)) {
                                    primeiro = true;
                                } else {
                                    estado = Estados.JOGANDO;
                                }

                                break;
                            case "SAIR":
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                //responder mensagem de erro: Não autorizado/ou inválida
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "MENSAGEM NÃO AUTORIZADA OU INVÁLIDA!");

                                break;
                        }
                        break;

                    case JOGANDO:
                        switch (operacao) {
                            case "RANKING":
                                try {
                                    String ranking = server.getRanking();
                                    reply.setParam("ranking", ranking);
                                    reply.setStatus(Status.OK);

                                } catch (Exception e) {
                                }
                                break;
                            case "STATUS":
                                String info= server.getStatus();
                                reply.setParam("pontuacao", this.getPontuação());
                                reply.setParam("info", info);
                                reply.setStatus(Status.OK);
                                break;
                            case "SAIR":
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "MENSAGEM NÃO AUTORIZADA OU INVÁLIDA!");
                                break;
                        }
                        break;
                    case VEZDEJOGAR:
                        switch (operacao) {
                            case "JOGADA":
                                int x = (int) m.getParam("x");
                                int y = (int) m.getParam("y");
                                //pegar as coordenadas da msg
                                reply.setStatus(Status.OK);
                                TiroEnum res = server.fazJogada(x, y,this);
                                reply.setParam("res", res);
                                if (res == TiroEnum.AGUA) {
                                    reply.setParam("resultado", "Perdeu a vez.");
                                    //perde vez de jogar
                                    estado= Estados.JOGANDO;
                                    server.removerFila();
                                    //sorteia o proximo
                                    server.addJogadorFila(this);
                                    server.sorteiaProximo();

                                    this.pontuação -= 1;
                                } else if (res == TiroEnum.FOGO) {
                                    this.pontuação += 1;
                                    reply.setParam("resultado", "Ganhou 1 ponto. Jogue de novo!");
                                } else if (res == TiroEnum.DESCOBERTA) {
                                    this.pontuação += 2;
                                    reply.setParam("resultado", "Ganhou 2 pontos. Jogue de novo!");
                                } else if (res == TiroEnum.AFUNDAR) {
                                    this.pontuação += 2;
                                    reply.setParam("resultado", "Ganhou 2 pontos. Jogue de novo!");
                                }

                                break;
                            case "STATUS":
                                String info= server.getStatus();                                
                                reply.setParam("pontuacao", this.getPontuação());
                                reply.setParam("info", info);
                                reply.setStatus(Status.OK);
                                break;
                            case "RANKING":
                                try {
                                    String ranking = server.getRanking();
                                    reply.setParam("ranking", ranking);
                                    reply.setStatus(Status.OK);
                                } catch (Exception e) {
                                }
                                break;
                            case "SAIR":
                                reply.setStatus(Status.ERROR);
                                reply.setParam("mensagem", "Você não pode sair na sua vez de jogar.");
                                break;
                        }

                        break;
                    case SAIR: //ESTADO
                        input.close();
                        output.close();
                        socket.close();
                        break;

                }
                
                output.writeObject(reply);
                output.flush();//cambio do rádio amador
                
                if (primeiro) {
                    estado = Estados.VEZDEJOGAR;
                    Mensagem avisa = new Mensagem("VEZDEJOGAR");
                    output.writeObject(avisa);
                    output.flush();
                    server.getStatus();
                    primeiro = false;
                }

                if(server.testaFim()){
                    server.fimJogo();
                }
            }
            //4.2 - Fechar streams de entrada e saída
            input.close();
            output.close();
        } catch (IOException e) {
            //tratamento de falhas
            System.out.println("Problema no tratamento da conexão com o cliente: " + socket.getInetAddress());
            System.out.println("Erro: " + e.getMessage());
            throw e;
        } finally {
            //final do tratamento do protocolo
            /*4.1 - Fechar socket de comunicação entre servidor/cliente*/
            fechaSocket(socket);
        }

    }

    public void proximoJogador() {
        estado = Estados.VEZDEJOGAR;
        Mensagem avisa = new Mensagem("VEZDEJOGAR"); 
            enviaMensagem(avisa);
   
    }

    public String getNome() {
        return nome;
    }

    public Integer getPontuação() {
        return pontuação;
    }

    private void fechaSocket(Socket s) throws IOException {
        s.close();
    }

    private Socket socket;
    private Server server;

    public TrataConexao(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        //inicializa a pontuação
        this.pontuação = 0;
    }
    public void enviaMensagem (Mensagem msg)
    {
   
        try {
            //output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(msg);
            output.flush();//cambio do rádio amador
        } catch (IOException ex) {
            Logger.getLogger(TrataConexao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
           
        }
   
    }
}

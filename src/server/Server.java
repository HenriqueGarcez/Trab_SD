/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import battleship.FilaJogadores;
import battleship.Tabuleiro;
import battleship.TiroEnum;
import util.Mensagem;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import util.Estados;
import util.Status;

/**
 *
 * @author elder
 */
public class Server {
    private ServerSocket serverSocket;
    private int cont;
    private List<Thread> threads;
    private List<TrataConexao> clientes;
    private Tabuleiro tabuleiro;
    private FilaJogadores<TrataConexao> fila;
    private String ultimoTiro;
    /*- Criar o servidor de conexões*/

    private void criarServerSocket(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
        cont = 0;
        threads = new ArrayList<>();
        clientes = new ArrayList<>();
        tabuleiro = new Tabuleiro();
        fila = new FilaJogadores<>();
    }
    

    /*2 -Esperar o um pedido de conexão;
     Outro processo*/
    private Socket esperaConexao() throws IOException {
        Socket socket = serverSocket.accept();
        return socket;
    }

    private void fechaSocket(Socket s) throws IOException {
        s.close();
    }

    private void enviaMsg(Object o, ObjectOutputStream out) throws IOException {
        out.writeObject(o);
        out.flush();
    }
    public synchronized void avisa()
    {
        System.out.println("Contador: " + ++cont);
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException {
        try {

            Server server = new Server();
            server.iniciaServidor();
            
        } catch (IOException e) {
            //trata exceção
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    private void iniciaServidor() throws IOException{
    
        this.criarServerSocket(5555);
            while (true) {
                System.out.println("Aguardando conexão...");
                Socket socket = this.esperaConexao();//protocolo
                System.out.println("Cliente conectado.");
                //Outro processo
                TrataConexao jogador = new TrataConexao( socket, this  );
                Thread th = new Thread( jogador );
                threads.add(th);
                clientes.add(jogador);
                
                th.start();
                              
                System.out.println("Tratando cliente conectado...");
            }
    
    }

    public String getRanking() {
       String ranking = "Ranking da partida \n\n";
       
       for( TrataConexao j: clientes)
       {       
           ranking +=  j.getNome() + " -----> " + j.getPontuação()+"\n";
       }
       
       return ranking;   
    }
    
    
     public synchronized void  addJogadorFila(TrataConexao jogador)
    {
        fila.enfilera(jogador); 
    }

    void sorteiaProximo() throws Exception {
        //pega o proximo da fila
        TrataConexao proximo = fila.proximo();
        //envia a ordem de vez de jogar
        proximo.proximoJogador();
        
    }

    TiroEnum fazJogada(int x, int y, TrataConexao quemJogou) throws Exception {
        
        TiroEnum tiro = tabuleiro.atirar(x, y);
        System.out.println("Resultado do tiro: " + tiro);
        System.out.println(tabuleiro.desenhaTabuleiro());
        //parte de mandar msg pra todos
        Mensagem m = new Mensagem("STATUS");
        m.setParam("tabuleiro", "\n"+tabuleiro.desenhaTabuleiro());
        m.setParam("mensagem","O jogador: "+ quemJogou.getNome() + " Jogou: X: " +x+ " Y: "+ y + " Resultado: " +tiro );
        for (TrataConexao cliente : clientes) {
            //percorre a lista
            this.enviaMsg(m, cliente.getOutput());
        }
        ultimoTiro=  "X: " +x+ " Y: "+ y + " Resultado: " +tiro;
        return tiro;
    }
    
    public Boolean testaFim(){
        Boolean teste= tabuleiro.fimDeJogo();
        return teste;
    }
    
    public void fimJogo() throws Exception{
        tabuleiro = new Tabuleiro();
        ultimoTiro= "";
        Mensagem m= new Mensagem("FIMDEJOGO");
        m.setParam("Info:", "O ultimo barco foi afundado e o jogo terminou, "
                + "um novo tabuleiro foi iniciado, as pontuações serão mantidas"
                + "e o ultimo jogador a ter a vez começará jogando.");
        for (TrataConexao cliente : clientes) {
            this.enviaMsg(m, cliente.getOutput());
        }
    }

    public String getStatus() {
                
       String res= "";
       res+= "Ultimo Tiro: "+ ultimoTiro;
       res += "\nLegenda: X = fogo; - = Afundado; ~ tiro n'água; espaço em branco: desconhecido";
       res += "\nTabuleiro:\n"+ tabuleiro.desenhaTabuleiro();
       
       return res;
    }

    
    boolean eprimeiro(TrataConexao jogador) {
        
        if (fila.proximo() == jogador)
            return true;
        else
            return false; 
        
    }

    synchronized void removerFila() throws Exception {
        fila.desenfilera();
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testes;

import battleship.FilaJogadores;
import battleship.Navio;
import battleship.Tabuleiro;
import battleship.TiroEnum;
import java.util.ArrayList;

/**
 *
 * @author elder
 */
public class Teste {
    
    public static void main(String[] args) throws Exception {
        Tabuleiro t = new Tabuleiro();
        
        System.out.println(t.desenhaTabuleiro());
        
        if(t.fimDeJogo())
        {
            //fim do jogo
            t= new Tabuleiro();
        }
           
     
       
       
        for( int i=0; i<10; i++ )
        {
            for(int j=0; j<10;j++)
            { 
                TiroEnum tiro = t.atirar(i, j);
                System.out.println("Resultado do tiro: " + tiro);
                System.out.println(t.desenhaTabuleiro());
                if(t.fimDeJogo()){
                    System.out.println("FIM");
                    j=10;
                    i=10;
                }
            }
        }
                
        System.out.println(t.desenhaTabuleiro() );
      /*  for (Navio n : t.getNavios()) {
            System.out.println(n.toString());
            
        }*/
        
        
        
    }
    
}

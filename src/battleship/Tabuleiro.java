/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elder
 */
public class Tabuleiro {

    ArrayList<Navio> navios;
    char tabView[][];

    public Tabuleiro() {
        navios = new ArrayList<>();
        tabView = new char[10][10];
        zeraView();
        geraNavios();
    }

    private void geraNavios() {
        colocaNavio(4);
        colocaNavio(3);
        colocaNavio(3);
        colocaNavio(2);
        colocaNavio(2);
        colocaNavio(2);
        colocaNavio(1);
        colocaNavio(1);
        colocaNavio(1);
        System.out.println(MostraPosicaoNavios());

        desenhaTabuleiro();

    }

    public void setNavios(ArrayList<Navio> navios) {
        this.navios = navios;
    }

    private void zeraView() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tabView[i][j] = ' ';
            }
        }
    }

    private Boolean colocaNavio(Integer tam) {
        try {
            Boolean ok = false;
            int x;
            int y;
            Thread.sleep(10);
            Random r = new Random((long) (System.currentTimeMillis()));
            while (!ok) {
                x = (int) (r.nextInt(10));
                y = (int) (r.nextInt(10));
                //System.out.println("X=" + x + " Y=" + y + " tam= " + tam);

                if ((y + x) % 2 == 0) {

                     ok = desenhaVertical( x, y, tam);
                    if (!ok) {
                        ok = desenhaHorizontal(x, y, tam);
                    }
                } else {
                    ok = desenhaHorizontal(x, y, tam);
                    if (!ok) {
                        ok = desenhaVertical( x, y, tam);
                    }
                }

            }
            
           // System.out.println(MostraPosicaoNavios());
            
            return ok;
        } catch (InterruptedException ex) {
            Logger.getLogger(Tabuleiro.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public TiroEnum atirar(Integer x, Integer y) throws Exception {
        if (x > 9 || y > 9 || x < 0 || y < 0) {
            throw new Exception("Posição fora do tabuleiro");
        }
        for (Navio n : navios) {
            TiroEnum tiro = n.atira(x, y);
            if (tiro != TiroEnum.AGUA) {
                System.out.println("Navio depois do tiro: " + n.toString());
                desenhaTabuleiro();
                return tiro;
            }

        }
        tabView[x][y] = '~';
        return TiroEnum.AGUA;
    }

    public String desenhaTabuleiro() {
        for (Navio n : navios) {
            int posicao = 0;
            for (int l = n.xFinal; l >= n.xInicial; l--) {
                for (int c = n.yFinal; c >= n.yInicial; c--) {
                    try {
                        tabView[l][c] = n.posicoes[posicao++];
                        //tabView[l][c] = 'X';
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("l: " + l + " C: " + c + " Navio: " + n.toString());
                        System.exit(0);
                    }
                }
            }
        }
        String s = "    0  1  2  3  4  5  6  7  8  9\n";
        for (int i = 0; i < 10; i++) {
            s += i + " ";
            for (int j = 0; j < 10; j++) {
                s += "| " + tabView[i][j];
            }
            s += "|\n";

        }
        return s;

    }

    public String MostraPosicaoNavios() {
        for (Navio n : navios) {
            int posicao = 0;
            for (int l = n.xFinal; l >= n.xInicial; l--) {
                for (int c = n.yFinal; c >= n.yInicial; c--) {
                    try {
                        //tabView[l][c] = n.posicoes[posicao++];
                        tabView[l][c] = 'N';
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("l: " + l + " C: " + c + " Navio: " + n.toString());
                        System.exit(0);
                    }
                }
            }
        }
        String s = "    0  1  2  3  4  5  6  7  8  9\n";
        for (int i = 0; i < 10; i++) {
            s += i + " ";
            for (int j = 0; j < 10; j++) {
                s += "| " + tabView[i][j];
            }
            s += "|\n";

        }
        return s;

    }

    public ArrayList<Navio> getNavios() {
        return navios;
    }

    public Boolean fimDeJogo() {
        for (Navio n : navios) {
            if (!n.estaAfundado()) {
                return false;
            }
        }
        return true;
    }

    private Boolean desenhaHorizontal( int x, int y, Integer tam) {
        //percorre y para ver se tem lugar
        if (y + tam > 10 || x >= 10) {
            return false;
        }
        for (int i = y; i < (y + tam); i++) {
           try{
               if (tabView[x][i] != ' ') {
                return false;
            }
           }catch(ArrayIndexOutOfBoundsException e)
           {
                System.out.println(e.getMessage());
                System.out.println("Hori X= " + x + " y= " + y);
                return false;
           }
        }
        Navio n = new Navio(tam, x, x, y, (y+tam)-1);
        navios.add(n);
        for (int i = n.xInicial; i < n.xFinal; i++) {
                for (int j = n.yInicial; j < n.yFinal; j++) {
                    tabView[i][j] = 'T';
                }
            }

        return true;
    }

    private Boolean desenhaVertical(int x, int y, Integer tam) {
        //percorre x para testar
        if (x + tam > 10 || y >= 10) {
            return false;
        }
        for (int i = x; i < (x + tam); i++) {
            try {
                if (tabView[i][y] != ' ') {
                    return false; 
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
                System.out.println("X= " + x + " y= " + y);
                return false;
            }
        }

        Navio n = new Navio(tam, x, (x+tam)-1, y, y);
        navios.add(n);
        for (int i = n.xInicial; i < n.xFinal; i++) {
                for (int j = n.yInicial; j < n.yFinal; j++) {
                    tabView[i][j] = 'T';
                }
            }
        return true;
    }

}
